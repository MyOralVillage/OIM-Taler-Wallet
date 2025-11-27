 package net.taler.wallet.oim.send.app

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.database.TranxHistory
import net.taler.database.data_models.Amount
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.Timestamp
import net.taler.database.data_models.TranxPurp
import net.taler.wallet.MainViewModel
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.balances.ScopeInfo
import net.taler.wallet.backend.TalerErrorInfo
import net.taler.wallet.oim.send.screens.PurposeScreen
import net.taler.wallet.oim.send.screens.QrScreen
import net.taler.wallet.oim.send.screens.SendScreen
import net.taler.wallet.peer.CheckFeeResult
import net.taler.wallet.transactions.TransactionAction
import net.taler.wallet.transactions.TransactionPeerPushDebit
import net.taler.wallet.peer.*

private enum class Screen { Send, Purpose, Qr }

/**
 * Top-level composable for the OIM Send flow.
 *
 * This function orchestrates the entire send-to-peer UX, handling:
 * - Account/balance selection
 * - Amount input and validation
 * - Purpose selection
 * - Peer push transaction initiation
 * - Throttling-aware retry logic
 * - QR code display for the Taler URI
 * - Test DB insertion for new transactions
 *
 * It ensures a consistent flow across state transitions, and is responsible for retrying the
 * payment handshake when encountering rate limits or transient errors.
 *
 * @param model Shared [MainViewModel] for UI state and business logic.
 * @param onHome Lambda invoked when the user navigates back to the OIM home.
 *
 * ### UI Screens:
 * - [Screen.Send]: User picks amount and optionally navigates to purpose selection.
 * - [Screen.Purpose]: User selects a transaction purpose and initiates transaction.
 * - [Screen.Qr]: Displays Taler URI via QR code once the push payment handshake succeeds.
 */
@Composable
fun OimSendApp(
    model: MainViewModel,
    onHome: () -> Unit = {},
) {
    // --- Basic context and coroutine scope for this composable ---
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Initialize test DB on first composition only ---
    LaunchedEffect(Unit) {
        // Initializes only the _test_ transaction history as requested
        TranxHistory.initTest(ctx)
    }

    // === State: Account and Balance ===
    val balanceState by model.balanceManager.state.observeAsState(BalanceState.None)
    val selectedScope by model.transactionManager.selectedScope.collectAsStateWithLifecycle(
        initialValue = null
    )

    /**
     * Active scope automatically resolves to the current selection if present;
     * otherwise falls back to the first account with KUDOS or TESTKUDOS.
     */
    val activeScope: ScopeInfo? = remember(balanceState, selectedScope) {
        selectedScope ?: (balanceState as? BalanceState.Success)?.let { bs ->
            bs.balances.firstOrNull { it.currency.equals("KUDOS", true) }?.scopeInfo
                ?: bs.balances.firstOrNull { it.currency.equals("TESTKUDOS", true) }?.scopeInfo
        }
    }

    // Amount entered by the user, defaulting to 0 in the active currency.
    var amount by remember(activeScope) {
        mutableStateOf(Amount.fromString(activeScope?.currency ?: "KUDOS", "0"))
    }

    // Current balance formatted for display.
    val balanceLabel: Amount = remember(balanceState, activeScope) {
        val success = balanceState as? BalanceState.Success
        val entry = success?.balances?.firstOrNull { it.scopeInfo == activeScope }
        Amount.fromString(
            activeScope?.currency ?: "KUDOS",
            entry?.available?.toString(showSymbol = false) ?: "0"
        )
    }

    // === UX State ===
    var screen by rememberSaveable { mutableStateOf(Screen.Send) }
    var chosenPurpose by rememberSaveable { mutableStateOf<TranxPurp?>(null) }
    var talerUri by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var anchorTxId by rememberSaveable { mutableStateOf<String?>(null) }

    // Retry and backoff
    var lastProgressTick by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var retries by rememberSaveable { mutableStateOf(0) }
    var retryInFlight by rememberSaveable { mutableStateOf(false) }

    // Track which transactions have already been recorded to the test DB.
    val recordedTxIds = remember { mutableStateListOf<String>() }

    // === Observing push-state and transactions ===
    val pushState by model.peerManager.pushState.collectAsStateWithLifecycle(
        net.taler.wallet.peer.OutgoingIntro
    )
    val selectedTx by model.transactionManager.selectedTransaction.collectAsStateWithLifecycle(
        initialValue = null
    )

    /**
     * Handle the creation of a new peer-push transaction.
     * Navigate to QR screen as soon as we get an [OutgoingResponse].
     */
    LaunchedEffect(pushState) {
        when (val s = pushState) {
            is net.taler.wallet.peer.OutgoingResponse -> {
                anchorTxId = s.transactionId
                talerUri = null
                creating = true
                retries = 0
                retryInFlight = false

                // Insert exactly once into test DB
                if (!recordedTxIds.contains(s.transactionId)) {
                    try {
                        TranxHistory.newTransaction(
                            tid = s.transactionId,
                            purp = chosenPurpose,
                            amt = amount,
                            dir = FilterableDirection.OUTGOING,
                            tms = Timestamp.now()
                        )
                        recordedTxIds.add(s.transactionId)
                    } catch (_: Exception) { /* ignore */ }
                }

                model.transactionManager.selectTransaction(s.transactionId)
                screen = Screen.Qr
            }
            is net.taler.wallet.peer.OutgoingError -> {
                creating = false
                Toast.makeText(ctx, s.info.userFacingMsg, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    /**
     * React to selected transaction updates:
     * - Set the Taler URI when available.
     * - Manage retry logic during purse creation if throttled.
     * - Stop if unrecoverable.
     */
    LaunchedEffect(selectedTx, anchorTxId) {
        val tx = selectedTx as? TransactionPeerPushDebit ?: return@LaunchedEffect
        if (tx.transactionId != anchorTxId) return@LaunchedEffect

        // Successful URI creation
        tx.talerUri?.takeIf { it.isNotBlank() }?.let { uri ->
            talerUri = uri
            creating = false
            lastProgressTick = System.currentTimeMillis()
            return@LaunchedEffect
        }

        // Retry logic for PENDING/CREATE_PURSE
        val isCreatePurse =
            tx.txState.major.name.equals("PENDING", true)
            && tx.txState.minor?.name?.equals("CREATE_PURSE", true) == true
        val canRetry = tx.txActions.contains(TransactionAction.Retry)
        val throttled = tx.error.isThrottled()

        if (
            screen == Screen.Qr
            && isCreatePurse
            && throttled
            && canRetry
            && !retryInFlight
            && retries < 6
            ) {
                retryInFlight = true
                val delayMs = (1200L * (retries + 1)).coerceAtMost(7000L)
                scope.launch {
                    delay(delayMs)
                    model.transactionManager.retryTransaction(tx.transactionId) { }
                    retries += 1
                    retryInFlight = false
                    lastProgressTick = System.currentTimeMillis()
                }
        }

        // Failed irrecoverably
        if (screen == Screen.Qr && !isCreatePurse && talerUri == null && !canRetry) {
            creating = false
            Toast.makeText(
                ctx,
                "Send could not be prepared. Please try again.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /** Watchdog to trigger soft retry if no progress after a timeout. */
    LaunchedEffect(screen, anchorTxId, talerUri) {
        if (screen != Screen.Qr || anchorTxId == null || talerUri != null) return@LaunchedEffect
        scope.launch {
            while (screen == Screen.Qr && talerUri == null && anchorTxId != null) {
                val idleFor = System.currentTimeMillis() - lastProgressTick
                if (idleFor > 12_000 && !retryInFlight && retries < 6) {
                    (selectedTx as? TransactionPeerPushDebit)?.let { tx ->
                        if (tx.txActions.contains(TransactionAction.Retry)) {
                            retryInFlight = true
                            model.transactionManager.retryTransaction(tx.transactionId) { }
                            retries += 1
                            retryInFlight = false
                            lastProgressTick = System.currentTimeMillis()
                        }
                    }
                }
                delay(1500)
            }
        }
    }

    /**
     * Reset flow state and invoke [onHome].
     */
    fun resetAndGoHome() {
        try {
            amount = Amount.zero(amount.currency)
            chosenPurpose = null
            talerUri = null
            anchorTxId = null
            creating = false
            retries = 0
            retryInFlight = false

            model.peerManager.resetPushPayment()
            model.transactionManager.selectTransaction(null)
        } finally {
            onHome()
        }
    }

    // === UI ===
    when (screen) {
        Screen.Send -> SendScreen(
            balance = balanceLabel,
            amount = amount,
            onAdd = { add ->
                if (add.currency == amount.currency) {
                    amount = runCatching { amount + add }.getOrElse { amount }
                } else {
                    Toast.makeText(
                        ctx,
                        "Wrong currency for this account",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            onRemoveLast = { last ->
                if (last.currency == amount.currency) {
                    amount = runCatching {
                        (amount - last).takeUnless { it.isZero() } ?: Amount.zero(amount.currency)
                    }.getOrElse { amount }
                }
            },
            onChoosePurpose = { screen = Screen.Purpose },
            onSend = { screen = Screen.Purpose },
            onChest = ::resetAndGoHome
        )

        Screen.Purpose -> PurposeScreen(
            balance = balanceLabel,
            onBack = { screen = Screen.Send },
            onDone = { picked ->
                if (creating) return@PurposeScreen
                chosenPurpose = picked

                val scopeInfo = activeScope
                when {
                    scopeInfo == null ->
                        Toast.makeText(
                            ctx,
                            "No KUDOS account selected",
                            Toast.LENGTH_SHORT
                        ).show()
                    amount.isZero() ->
                        Toast.makeText(
                            ctx,
                            "Choose an amount first",
                            Toast.LENGTH_SHORT
                        ).show()
                    amount.currency != scopeInfo.currency ->
                        Toast.makeText(
                            ctx,
                            "Currency mismatch with account",
                            Toast.LENGTH_SHORT
                        ).show()
                    else -> {
                        creating = true
                        scope.launch {
                            when (val check = model.peerManager.checkPeerPushFees(
                                amount = amount.toCommonAmount(),
                                exchangeBaseUrl = null,
                                restrictScope = scopeInfo
                            )) {
                                is CheckFeeResult.Success -> {
                                    model.peerManager.initiatePeerPushDebit(
                                        amount = amount.toCommonAmount(),
                                        summary = picked.cmp,
                                        expirationHours = 24L,
                                        restrictScope = scopeInfo
                                    )
                                }
                                is CheckFeeResult.InsufficientBalance -> {
                                    creating = false
                                    val max = check.maxAmountEffective
                                    val msg = if (max != null && !max.isZero())
                                        "Insufficient balance." +
                                        " Max now: ${max.amountStr} ${max.currency}"
                                    else "Insufficient balance."
                                    Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                                }
                                is CheckFeeResult.None -> {
                                    creating = false
                                    Toast.makeText(
                                        ctx,
                                        "Could not check balance/fees",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    }
                }
            },
            onHome = ::resetAndGoHome
        )

        Screen.Qr -> QrScreen(
            talerUri = talerUri,
            amount = amount,
            balance = balanceLabel,
            purpose = chosenPurpose,
            onBack = {
                amount = Amount.zero(amount.currency)
                chosenPurpose = null
                talerUri = null
                anchorTxId = null
                creating = false
                retries = 0
                retryInFlight = false
                model.peerManager.resetPushPayment()
                model.transactionManager.selectTransaction(null)
                screen = Screen.Send
            },
            onHome = ::resetAndGoHome
        )
    }
}

/** Converts a public [Amount] to the common Taler [Amount]. */
private fun Amount.toCommonAmount(): net.taler.common.Amount =
    net.taler.common.Amount.fromString(this.currency, this.amountStr)

/** Determines whether a [TalerErrorInfo] represents a throttling/rate limit condition. */
private fun TalerErrorInfo?.isThrottled(): Boolean {
    if (this == null) return false
    val text = buildString {
        hint?.let { append(it.lowercase()).append(' ') }
        message?.let { append(it.lowercase()).append(' ') }
    }
    return "throttl" in text || "rate limit" in text || "429" in text
}

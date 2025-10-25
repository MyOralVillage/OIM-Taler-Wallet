package net.taler.wallet.oim.send.app

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.database.data_models.Amount
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

private enum class Screen { Send, Purpose, Qr }

@Composable
fun OimSendApp(
    model: MainViewModel,
    onHome: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---- balance / scope ----
    val balanceState by model.balanceManager.state.observeAsState(BalanceState.None)
    val selectedScope by model.transactionManager.selectedScope.collectAsStateWithLifecycle(initialValue = null)

    val activeScope: ScopeInfo? = remember(balanceState, selectedScope) {
        selectedScope ?: (balanceState as? BalanceState.Success)?.let { bs ->
            bs.balances.firstOrNull { it.currency.equals("KUDOS", true) }?.scopeInfo
                ?: bs.balances.firstOrNull { it.currency.equals("TESTKUDOS", true) }?.scopeInfo
        }
    }

    var amount by remember(activeScope) {
        mutableStateOf(Amount.fromString(activeScope?.currency ?: "KUDOS", "0"))
    }

    val balanceLabel: Amount = remember(balanceState, activeScope) {
        val success = balanceState as? BalanceState.Success
        val entry = success?.balances?.firstOrNull { it.scopeInfo == activeScope }
        Amount.fromString(
            activeScope?.currency ?: "KUDOS",
            entry?.available?.toString(showSymbol = false) ?: "0"
        )
    }

    // ---- nav & state ----
    var screen by rememberSaveable { mutableStateOf(Screen.Send) }
    var chosenPurpose by rememberSaveable { mutableStateOf<TranxPurp?>(null) }
    var talerUri by rememberSaveable { mutableStateOf<String?>(null) }
    var creating by rememberSaveable { mutableStateOf(false) }
    var anchorTxId by rememberSaveable { mutableStateOf<String?>(null) }

    // retry/backoff guards
    var lastProgressTick by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    var retries by rememberSaveable { mutableStateOf(0) }
    var retryInFlight by rememberSaveable { mutableStateOf(false) }

    // follow wallet push-state to get tx id quickly
    val pushState: net.taler.wallet.peer.OutgoingState by model.peerManager.pushState.collectAsStateWithLifecycle(
        net.taler.wallet.peer.OutgoingIntro
    )
    val selectedTx by model.transactionManager.selectedTransaction.collectAsStateWithLifecycle(initialValue = null)

    // 1) New tx created -> go to QR immediately
    LaunchedEffect(pushState) {
        when (val s = pushState) {
            is net.taler.wallet.peer.OutgoingResponse -> {
                anchorTxId = s.transactionId
                talerUri = null
                creating = true
                retries = 0
                retryInFlight = false

                model.transactionManager.selectTransaction(s.transactionId)
                screen = Screen.Qr
            }
            is net.taler.wallet.peer.OutgoingError -> {
                creating = false
                Toast.makeText(ctx, s.info.userFacingMsg ?: "Send failed", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    // 2) React to tx updates: set real talerUri or retry if throttled
    LaunchedEffect(selectedTx, anchorTxId) {
        val tx = selectedTx as? TransactionPeerPushDebit ?: return@LaunchedEffect
        if (tx.transactionId != anchorTxId) return@LaunchedEffect

        tx.talerUri?.takeIf { it.isNotBlank() }?.let { uri ->
            talerUri = uri
            creating = false
            lastProgressTick = System.currentTimeMillis()
            return@LaunchedEffect
        }

        val isCreatePurse =
            tx.txState?.major?.name?.equals("PENDING", true) == true &&
                    tx.txState?.minor?.name?.equals("CREATE_PURSE", true) == true

        val canRetry = tx.txActions?.contains(TransactionAction.Retry) == true
        val throttled = tx.error.isThrottled()

        if (screen == Screen.Qr && isCreatePurse && throttled && canRetry && !retryInFlight && retries < 6) {
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

        if (screen == Screen.Qr && !isCreatePurse && talerUri == null && !canRetry) {
            creating = false
            Toast.makeText(ctx, "Send could not be prepared. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    // 3) Watchdog for soft retry
    LaunchedEffect(screen, anchorTxId, talerUri) {
        if (screen != Screen.Qr || anchorTxId == null || talerUri != null) return@LaunchedEffect
        scope.launch {
            while (screen == Screen.Qr && talerUri == null && anchorTxId != null) {
                val idleFor = System.currentTimeMillis() - lastProgressTick
                if (idleFor > 12_000 && !retryInFlight && retries < 6) {
                    (selectedTx as? TransactionPeerPushDebit)?.let { tx ->
                        if (tx.txActions?.contains(TransactionAction.Retry) == true) {
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

    /** Clean state and let the host navigate to OIM Home */
    fun resetAndGoHome() {
        amount = Amount.zero(amount.currency)
        chosenPurpose = null
        talerUri = null
        anchorTxId = null
        creating = false
        retries = 0
        retryInFlight = false
        model.peerManager.resetPushPayment()
        model.transactionManager.selectTransaction(null)
        onHome()
    }

    // --- UI ---
    when (screen) {
        Screen.Send -> SendScreen(
            balance = balanceLabel,
            amount = amount,
            onAdd = { add ->
                if (add.currency == amount.currency) {
                    amount = runCatching { amount + add }.getOrElse { amount }
                } else {
                    Toast.makeText(ctx, "Wrong currency for this account", Toast.LENGTH_SHORT).show()
                }
            },
            onRemoveLast = { last ->
                if (last.currency == amount.currency) {
                    amount = runCatching {
                        val r = amount - last
                        if (r.isZero()) Amount.zero(amount.currency) else r
                    }.getOrElse { amount }
                }
            },
            onChoosePurpose = { screen = Screen.Purpose },
            onSend = { screen = Screen.Purpose },
            onHome = ::resetAndGoHome
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
                        Toast.makeText(ctx, "No KUDOS account selected", Toast.LENGTH_SHORT).show()
                    amount.isZero() ->
                        Toast.makeText(ctx, "Choose an amount first", Toast.LENGTH_SHORT).show()
                    amount.currency != scopeInfo.currency ->
                        Toast.makeText(ctx, "Currency mismatch with account", Toast.LENGTH_SHORT).show()
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
                                        "Insufficient balance. Max now: ${max.amountStr} ${max.currency}"
                                    else "Insufficient balance."
                                    Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                                }
                                is CheckFeeResult.None -> {
                                    creating = false
                                    Toast.makeText(ctx, "Could not check balance/fees", Toast.LENGTH_LONG).show()
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

private fun Amount.toCommonAmount(): net.taler.common.Amount =
    net.taler.common.Amount.fromString(this.currency, this.amountStr)

private fun TalerErrorInfo?.isThrottled(): Boolean {
    if (this == null) return false
    val text = buildString {
        hint?.let { append(it.lowercase()).append(' ') }
        message?.let { append(it.lowercase()).append(' ') }
    }
    return "throttl" in text || "rate limit" in text || "429" in text
}

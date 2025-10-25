/**
 * ## OimSendApp
 *
 * Composable root of the OIM Send application flow, coordinating UI screens,
 * wallet-core interactions, and peer-to-peer push payment logic.
 *
 * ### Responsibilities
 * - Manages navigation between [SendScreen], [PurposeScreen], and [QrScreen].
 * - Interfaces with [MainViewModel] to:
 *   - Observe wallet balances and scope selection.
 *   - Initiate peer-to-peer debit transfers.
 *   - Track outgoing transaction progress and retry on throttled states.
 * - Maintains ephemeral UI state such as chosen amount, purpose, retry counters,
 *   and live Taler URI updates.
 * - Provides graceful recovery via automatic soft-retry and reset helpers.
 *
 * ### Internal Flow
 * 1. **SendScreen** → user builds an amount visually (animated banknotes).
 * 2. **PurposeScreen** → select transaction purpose.
 * 3. **QrScreen** → display payment QR until purse creation and URI ready.
 *
 * Includes bounded exponential back-off for rate-limited wallet responses
 * and clears state safely when returning home via [resetAndGoHome].
 *
 * @param model Shared [MainViewModel] providing access to balances, peer manager,
 * and transaction manager.
 * @param onHome Callback to navigate back to the wallet home without forcing screen reset.
 *
 * @see net.taler.wallet.peer.PeerManager
 * @see net.taler.wallet.transactions.TransactionManager
 * @see net.taler.wallet.oim.send.screens.SendScreen
 * @see net.taler.wallet.oim.send.screens.PurposeScreen
 * @see net.taler.wallet.oim.send.screens.QrScreen
 */

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
<<<<<<< HEAD
=======
<<<<<<< HEAD
>>>>>>> c4c1157 (got rid of bugs in send apk)
=======
import net.taler.wallet.balances.ScopeInfo
import net.taler.wallet.backend.TalerErrorInfo
>>>>>>> f82ba56 (UI changes and fix qr code loading for send)
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
import net.taler.wallet.oim.send.screens.PurposeScreen
import net.taler.wallet.oim.send.screens.QrScreen
import net.taler.wallet.oim.send.screens.SendScreen
<<<<<<< HEAD
<<<<<<< HEAD
import net.taler.wallet.peer.OutgoingIntro
import net.taler.wallet.peer.OutgoingState
<<<<<<< HEAD
<<<<<<< HEAD
import net.taler.wallet.BuildConfig.DEBUG
=======
>>>>>>> f512e18 (added backend integration and db transaction update)
=======
import net.taler.wallet.peer.OutgoingResponse
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
import net.taler.wallet.peer.*
>>>>>>> 321d128 (updated send to be more dynamic)
=======
import net.taler.wallet.peer.CheckFeeResult
import net.taler.wallet.peer.OutgoingError
import net.taler.wallet.peer.OutgoingIntro
import net.taler.wallet.peer.OutgoingResponse
import net.taler.wallet.peer.OutgoingState
<<<<<<< HEAD
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
>>>>>>> 9068d57 (got rid of bugs in send apk)
=======
import net.taler.wallet.transactions.TransactionAction
import net.taler.wallet.transactions.TransactionPeerPushDebit
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)

private enum class Screen { Send, Purpose, Qr }

@Composable
fun OimSendApp(
    model: MainViewModel,
    onHome: () -> Unit = {},
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        // Initialize transaction database
        if (DEBUG) TranxHistory.initTest(ctx)
        else TranxHistory.initTest(ctx)
        //  in future releases/builds, do TranxHistory.init(ctx)
=======
        // Initialize local TX DB once (TranxHistory is @RequiresApi 34)
        LaunchedEffect(Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
                !TranxHistory.isIniti) {
                TranxHistory.init(ctx)
            }
        }
>>>>>>> f512e18 (added backend integration and db transaction update)
=======
        // Local guard so we initialize the local TX DB at most once from this UI.
        var localTxDbInitialized by remember { mutableStateOf(false) }

        // Initialize local TX DB once (TranxHistory is @RequiresApi 34)
=======
        // Initialize local TX DB once
>>>>>>> 321d128 (updated send to be more dynamic)
        LaunchedEffect(Unit) {
            // TODO: change else to TranxHistory.init(ctx) for prod
            if (BuildConfig.DEBUG) runCatching { TranxHistory.initTest(ctx) }
            else runCatching { TranxHistory.initTest(ctx) }
        }
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)

        var screen by remember { mutableStateOf(Screen.Send) }

        // Use Amount API instead of raw integers
        var amount by remember { mutableStateOf(Amount.fromString("SLE", "0")) }
        val balance = Amount.fromString("SLE", "25") // TODO: Get from actual wallet balance

        var chosenPurpose by remember { mutableStateOf<TranxPurp?>(null) }
        var talerUri by remember { mutableStateOf<String?>(null) }

        // Observe backend push state
        val pushState: OutgoingState by model.peerManager.pushState
            .collectAsState(initial = OutgoingIntro)

<<<<<<< HEAD
        // When backend answers with a tx id, navigate to QR.
<<<<<<< HEAD
        // If  TransactionManager exposes the selected tx with a talerUri,
        //  replace the placeholder below with the real URI.
=======
        // If your TransactionManager exposes the selected tx with a talerUri,
        // you can replace the placeholder below with the real URI.
>>>>>>> f512e18 (added backend integration and db transaction update)
        LaunchedEffect(pushState) {
            if (pushState is net.taler.wallet.peer.OutgoingResponse) {
                val txId = (pushState as net.taler.wallet.peer.OutgoingResponse).transactionId

                // Optional: select tx so normal wallet screens can show details
                // model.transactionManager.selectTransaction(txId)

                // QUICK way to show a QR immediately. Many Taler frontends encode a
<<<<<<< HEAD
                // "pay-push" URI using the tx id. If  backend exposes the URI,
=======
                // "pay-push" URI using the tx id. If your backend exposes the URI,
>>>>>>> f512e18 (added backend integration and db transaction update)
                // swap this line to use that value instead.
=======
        // Navigate to QR when we receive a transaction id from the backend
        LaunchedEffect(pushState) {
            if (pushState is OutgoingResponse) {
                val txId = (pushState as OutgoingResponse).transactionId
<<<<<<< HEAD
                // If your backend exposes a full URI, use that here.
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
>>>>>>> 321d128 (updated send to be more dynamic)
                talerUri = "taler://pay-push/$txId"
                screen = Screen.Qr
            }
        }

        when (screen) {
            Screen.Send -> SendScreen(
                balance = balance,
                amount = amount,
                onAdd = { addedAmount ->
                    amount = runCatching { amount + addedAmount }.getOrElse { amount }
                },
                onRemoveLast = { removedAmount ->
                    amount = runCatching {
                        val result = amount - removedAmount
                        if (result.isZero()) Amount.zero(amount.currency) else result
                    }.getOrElse { amount }
                },
                onChoosePurpose = { screen = Screen.Purpose },
                onSend = { screen = Screen.Purpose }
            )

            Screen.Purpose -> PurposeScreen(
                balance = balance,
                onBack = { screen = Screen.Send },
                onDone = { pickedPurpose ->
<<<<<<< HEAD
<<<<<<< HEAD
                    chosenPurpose = pickedPurpose

<<<<<<< HEAD
                    // 1) Write to local history
                    // check if transaction history db is initialized
                    if (DEBUG) TranxHistory.initTest(ctx)
                    else TranxHistory.initTest(ctx)
                    //  in future releases/builds, do TranxHistory.init(ctx)

                    // construct new transaction
                    val amt = Amount.fromString(currencyCode, amount.toString())
                    val tid = "DEMO_TX_${System.currentTimeMillis()}"
                    val ts = Timestamp.now()
                    val dir = FilterableDirection.OUTGOING
                    val purp: TranxPurp? = mapPickedPurposeToTranxPurp(pickedPurpose)
                    TranxHistory.newTransaction(
                        tid = tid,
                        purp = purp,
                        amt = amt,
                        dir = dir,
                        tms = ts
                    )
=======
                    // 1) Write to your local history (kept as requested)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if (!localTxDbInitialized) {
                            runCatching { TranxHistory.init(ctx) }
                            localTxDbInitialized = true
                        }
                        val amt = Amount.fromString(currencyCode, amount.toString())
                        val tid = "DEMO_TX_${System.currentTimeMillis()}"
                        val ts = Timestamp.now()
                        val dir = FilterableDirection.OUTGOING
                        val purp: TranxPurp? = mapPickedPurposeToTranxPurp(pickedPurpose)
                        TranxHistory.newTransaction(
                            tid = tid, purp = purp, amt = amt, dir = dir, tms = ts
                        )
                    }
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
                    chosenPurpose = pickedPurpose

                    // 1) Write to local transaction history
                    // TODO: change else to TranxHistory.init(ctx) for prod
                    if (BuildConfig.DEBUG) runCatching { TranxHistory.initTest(ctx) }
                    else runCatching { TranxHistory.initTest(ctx) }

                    val tid = "DEMO_TX_${System.currentTimeMillis()}"
                    val ts = Timestamp.now()
                    val dir = FilterableDirection.OUTGOING

                    TranxHistory.newTransaction(
                        tid = tid,
                        purp = pickedPurpose,
                        amt = amount,
                        dir = dir,
                        tms = ts
                    )
>>>>>>> 321d128 (updated send to be more dynamic)

                    // 2) Kick off P2P push with the wallet backend
                    scope.launch {
                        model.peerManager.initiatePeerPushDebit(
                            amount = amount,
                            summary = pickedPurpose.cmp,
                            expirationHours = 24L,
                        )
                        // OutgoingResponse will trigger navigation to QR (handled above)
                    }
=======
                    // 1) Write to your local history (kept as requested)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        if (!TranxHistory.isIniti) TranxHistory.init(ctx)
                        val amt = Amount.fromString(currencyCode, amount.toString())
                        val tid = "DEMO_TX_${System.currentTimeMillis()}"
                        val ts = Timestamp.now()
                        val dir = FilterableDirection.OUTGOING
                        val purp: TranxPurp? = mapPickedPurposeToTranxPurp(pickedPurpose)
                        TranxHistory.newTransaction(tid = tid, purp = purp, amt = amt, dir = dir, tms = ts)
                    }

                    // 2) Kick off P2P push with the wallet backend
                    scope.launch {
                        val amt = Amount.fromString(currencyCode, amount.toString())
                        // Subject/purpose for contract terms = pickedPurpose
                        val hours = 24L // same as DEFAULT_EXPIRY if you want; can be UI-driven
                        model.peerManager.initiatePeerPushDebit(
                            amount = amt,
                            summary = pickedPurpose,
                            expirationHours = hours,
                        )
                        // we do NOT switch screen here; we wait for pushState to move to OutgoingResponse
                        // (handled in LaunchedEffect above) so we have txId for the QR.
                    }
>>>>>>> f512e18 (added backend integration and db transaction update)
                }
            )

            Screen.Qr -> QrScreen(
                talerUri = talerUri ?: "taler://invalid",
                amount = amount,
                purpose = chosenPurpose,
                onBack = {
                    // Reset state for next transaction
                    amount = Amount.zero(amount.currency)
                    chosenPurpose = null
                    talerUri = null
                    screen = Screen.Send
                }
            )
        }
    }
<<<<<<< HEAD
}

<<<<<<< HEAD
<<<<<<< HEAD
// TODO: needs complete refactoring, remove this and simplify things!
/** Try to resolve the UI label/cmp into one of the sealed TranxPurp objects. */
=======
/**
 * Try to resolve the UI label/cmp into one of the sealed TranxPurp objects.
 */
>>>>>>> f512e18 (added backend integration and db transaction update)
=======
/**
 * Try to resolve the UI label/cmp into one of the sealed TranxPurp objects.
 * We match against cmp, case-insensitive, and the common "pretty" label variant
 * where spaces are used instead of underscores.
 */
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
private fun mapPickedPurposeToTranxPurp(picked: String): TranxPurp? {
    // direct cmp
    tranxPurpLookup[picked]?.let { return it }
    // case-insensitive cmp
    tranxPurpLookup[picked.uppercase()]?.let { return it }
    // replace spaces with underscores, then uppercase
    tranxPurpLookup[picked.replace(' ', '_').uppercase()]?.let { return it }
<<<<<<< HEAD
<<<<<<< HEAD
    return tranxPurpLookup.values.firstOrNull { it.cmp.equals(picked, ignoreCase = true) }
=======
    return tranxPurpLookup.values.firstOrNull { it.assetLabel.equals(picked, ignoreCase = true) }
>>>>>>> f512e18 (added backend integration and db transaction update)
=======
    // no assetLabel comparison to avoid unresolved reference
    return null
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
}
=======
}
>>>>>>> 321d128 (updated send to be more dynamic)
=======
=======
    // ---- balance / scope ----
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
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
    val pushState: OutgoingState by model.peerManager.pushState.collectAsStateWithLifecycle(OutgoingIntro)
    // live selected transaction (we'll point selection at the tx once we get the id)
    val selectedTx by model.transactionManager.selectedTransaction.collectAsStateWithLifecycle(initialValue = null)

    // 1) New tx created -> go to QR immediately (spinner shown until URI is present)
    LaunchedEffect(pushState) {
        when (val s = pushState) {
            is OutgoingResponse -> {
                anchorTxId = s.transactionId
                talerUri = null
                creating = true
                retries = 0
                retryInFlight = false
                lastProgressTick = System.currentTimeMillis()

                model.transactionManager.selectTransaction(s.transactionId)
                screen = Screen.Qr
            }
            is OutgoingError -> {
                creating = false
                Toast.makeText(ctx, s.info.userFacingMsg ?: "Send failed", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    // 2) React to tx updates: set real talerUri or (if throttled) nudge with retry
    LaunchedEffect(selectedTx, anchorTxId) {
        val tx = selectedTx as? TransactionPeerPushDebit ?: return@LaunchedEffect
        if (tx.transactionId != anchorTxId) return@LaunchedEffect

        // Success — real URI is ready
        tx.talerUri?.takeIf { it.isNotBlank() }?.let { uri ->
            talerUri = uri
            creating = false
            lastProgressTick = System.currentTimeMillis()
            return@LaunchedEffect
        }

        // Still waiting in "create purse"?
        val isCreatePurse =
            tx.txState?.major?.name?.equals("PENDING", true) == true &&
                    tx.txState?.minor?.name?.equals("CREATE_PURSE", true) == true

        val canRetry = tx.txActions?.contains(TransactionAction.Retry) == true
        val throttled = tx.error.isThrottled()

        // Gentle bounded backoff when throttled
        if (screen == Screen.Qr && isCreatePurse && throttled && canRetry && !retryInFlight && retries < 6) {
            retryInFlight = true
            val delayMs = (1200L * (retries + 1)).coerceAtMost(7000L)
            scope.launch {
                delay(delayMs)
                model.transactionManager.retryTransaction(tx.transactionId) { /* ignore one-shot error */ }
                retries += 1
                retryInFlight = false
                lastProgressTick = System.currentTimeMillis()
            }
        }

        // If we left create-purse **without** an URI and can't retry, bail gracefully
        if (screen == Screen.Qr && !isCreatePurse && talerUri == null && !canRetry) {
            creating = false
            Toast.makeText(ctx, "Send could not be prepared. Please try again.", Toast.LENGTH_LONG).show()
        }
    }

    // 3) Watchdog: if we made no progress for a while, try a soft retry (prevents “forever spinner”)
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

    /** Clean state and let the host navigate to OIM Home (do *not* force `screen = Send`). */
    fun resetAndGoHome() {
        // clean UI + wallet-core selection
        amount = Amount.zero(amount.currency)
        chosenPurpose = null
        talerUri = null
        anchorTxId = null
        creating = false
        retries = 0
        retryInFlight = false
        model.peerManager.resetPushPayment()
        model.transactionManager.selectTransaction(null)

        // IMPORTANT: don't change `screen` here; the host performs navigation to OIM Home.
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
                if (creating) return@PurposeScreen // single-flight
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
                                    // kick off tx; QR screen will open as soon as we get OutgoingResponse
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
            talerUri = talerUri,   // null => spinner until purse & URI exist
            amount = amount,
            purpose = chosenPurpose,
            onBack = {
                // reset + return to Send
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
<<<<<<< HEAD
>>>>>>> 9068d57 (got rid of bugs in send apk)
=======

/** Detect throttling hints from wallet-core error (code 7004, “throttled”, “rate limit”, etc.). */
private fun TalerErrorInfo?.isThrottled(): Boolean {
    if (this == null) return false
    val text = buildString {
        hint?.let { append(it.lowercase()).append(' ') }
        message?.let { append(it.lowercase()).append(' ') }
    }
    return "throttl" in text || "rate limit" in text || "429" in text
}
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)

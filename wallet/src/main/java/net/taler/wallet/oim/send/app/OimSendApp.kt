package net.taler.wallet.oim.send.app

import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import net.taler.wallet.MainViewModel
import net.taler.wallet.balances.BalanceState
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
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
>>>>>>> 9068d57 (got rid of bugs in send apk)

private enum class Screen { Send, Purpose, Qr }

@Composable
fun OimSendApp(model: MainViewModel) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

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
    val balanceState by model.balanceManager.state.observeAsState(BalanceState.None)
    val selectedScope by model.transactionManager.selectedScope.collectAsStateWithLifecycle(initialValue = null)

    // Prefer KUDOS/TESTKUDOS when nothing is selected
    val activeScope = remember(balanceState, selectedScope) {
        selectedScope ?: (balanceState as? BalanceState.Success)?.let { bs ->
            bs.balances.firstOrNull { it.currency.equals("KUDOS", true) }?.scopeInfo
                ?: bs.balances.firstOrNull { it.currency.equals("TESTKUDOS", true) }?.scopeInfo
        }
    }

    // Amount in active scope currency
    var amount by remember(activeScope) {
        mutableStateOf(Amount.fromString(activeScope?.currency ?: "KUDOS", "0"))
    }

    // Balance label for top bar
    val balanceLabel: Amount = remember(balanceState, activeScope) {
        val success = balanceState as? BalanceState.Success
        val entry = success?.balances?.firstOrNull { it.scopeInfo == activeScope }
        Amount.fromString(
            activeScope?.currency ?: "KUDOS",
            entry?.available?.toString(showSymbol = false) ?: "0"
        )
    }

    var screen by remember { mutableStateOf(Screen.Send) }
    var chosenPurpose by remember { mutableStateOf<TranxPurp?>(null) }
    var talerUri by remember { mutableStateOf<String?>(null) }

    // Keep the exchange URL returned by the fee check
    var lastExchangeBaseUrl by remember { mutableStateOf<String?>(null) }

    // Observe backend progress
    val pushState: OutgoingState by model.peerManager.pushState.collectAsStateWithLifecycle(OutgoingIntro)

    LaunchedEffect(pushState) {
        when (val s = pushState) {
            is OutgoingResponse -> {
                // txId = "txn:peer-push-debit:<PURSE_ID>"
                val purseId = s.transactionId.substringAfterLast(':')
                val ex = lastExchangeBaseUrl
                talerUri = if (ex != null) {
                    // IMPORTANT: percent-encode the *full* exchange URL (scheme + host + optional slash)
                    val encoded = URLEncoder.encode(ex, StandardCharsets.UTF_8.toString())
                    // Receiver-friendly URI understood by HandleUriFragment → preparePeerPushCredit
                    "ext+taler://pay-push/$encoded/$purseId"
                } else {
                    // Fallback (not ideal, but shows something)
                    "ext+taler://pay-push/$purseId"
                }
                screen = Screen.Qr
            }
            is OutgoingError -> {
                Toast.makeText(ctx, s.info.userFacingMsg ?: "Send failed", Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

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
            onSend = { screen = Screen.Purpose }
        )

        Screen.Purpose -> PurposeScreen(
            balance = balanceLabel,
            onBack = { screen = Screen.Send },
            onDone = { picked ->
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
                        // 1) Check fees/balance constrained to this scope (gets us the exchange URL)
                        scope.launch {
                            when (val check = model.peerManager.checkPeerPushFees(
                                amount = amount.toCommonAmount(),
                                exchangeBaseUrl = null,
                                restrictScope = scopeInfo
                            )) {
                                is CheckFeeResult.Success -> {
                                    lastExchangeBaseUrl = check.exchangeBaseUrl // <-- save for URI
                                    // 2) Initiate
                                    model.peerManager.initiatePeerPushDebit(
                                        amount = amount.toCommonAmount(),
                                        summary = picked.cmp,
                                        expirationHours = 24L,
                                        restrictScope = scopeInfo
                                    )
                                }
                                is CheckFeeResult.InsufficientBalance -> {
                                    val max = check.maxAmountEffective
                                    val msg = if (max != null && !max.isZero())
                                        "Insufficient balance. Max now: ${max.amountStr} ${max.currency}"
                                    else
                                        "Insufficient balance. Get Test KUDOS or switch account."
                                    Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
                                }
                                is CheckFeeResult.None -> {
                                    Toast.makeText(ctx, "Could not check balance/fees", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                }
            }
        )

        Screen.Qr -> QrScreen(
            talerUri = talerUri ?: "taler://invalid",
            amount = amount,
            purpose = chosenPurpose,
            onBack = {
                amount = Amount.zero(amount.currency)
                chosenPurpose = null
                talerUri = null
                lastExchangeBaseUrl = null
                model.peerManager.resetPushPayment()
                screen = Screen.Send
            }
        )
    }
}

/* helpers */
private fun Amount.toCommonAmount(): net.taler.common.Amount =
    net.taler.common.Amount.fromString(this.currency, this.amountStr)
>>>>>>> 9068d57 (got rid of bugs in send apk)

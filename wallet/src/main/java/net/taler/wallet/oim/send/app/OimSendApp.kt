package net.taler.wallet.oim.send.app

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import net.taler.database.TranxHistory
import net.taler.database.data_models.Amount
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.Timestamp
import net.taler.database.data_models.TranxPurp
import net.taler.database.data_models.tranxPurpLookup
import net.taler.wallet.MainViewModel
import net.taler.wallet.oim.send.screens.PurposeScreen
import net.taler.wallet.oim.send.screens.QrScreen
import net.taler.wallet.oim.send.screens.SendScreen
import net.taler.wallet.peer.OutgoingIntro
import net.taler.wallet.peer.OutgoingState
<<<<<<< HEAD
import net.taler.wallet.BuildConfig.DEBUG
=======
>>>>>>> f512e18 (added backend integration and db transaction update)

private enum class Screen { Send, Purpose, Qr }

@Composable
fun OimSendApp(model: MainViewModel) {
    OimTheme {
        val ctx = LocalContext.current.applicationContext
        val scope = rememberCoroutineScope()

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

        var screen by remember { mutableStateOf(Screen.Send) }
        var amount by remember { mutableStateOf(0) }
        var chosenPurpose by remember { mutableStateOf<String?>(null) }

        // for QR page
        var talerUri by remember { mutableStateOf<String?>(null) }

        // observe backend push state (avoid `.value` error)
        val pushState: OutgoingState by model.peerManager.pushState
            .collectAsState(initial = OutgoingIntro)

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
                talerUri = "taler://pay-push/$txId"

                screen = Screen.Qr
            }
        }

        // Currency / labels (UI-only)
        val currencyCode = "SLE"
        val currencyLabel = "Leones"
        val balance = 25 // demo header

        when (screen) {
            Screen.Send -> SendScreen(
                balance = balance,
                amount = amount,
                onAdd = { amount += it },
                onRemoveLast = { removed -> amount = (amount - removed).coerceAtLeast(0) },
                onChoosePurpose = { screen = Screen.Purpose },
                onSend = { screen = Screen.Purpose }
            )

            Screen.Purpose -> PurposeScreen(
                balance = balance,
                onBack = { screen = Screen.Send },
                onDone = { pickedPurpose ->
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
                onBack = { screen = Screen.Send }
            )
        }
    }
}

<<<<<<< HEAD
// TODO: needs complete refactoring, remove this and simplify things!
/** Try to resolve the UI label/cmp into one of the sealed TranxPurp objects. */
=======
/**
 * Try to resolve the UI label/cmp into one of the sealed TranxPurp objects.
 */
>>>>>>> f512e18 (added backend integration and db transaction update)
private fun mapPickedPurposeToTranxPurp(picked: String): TranxPurp? {
    tranxPurpLookup[picked]?.let { return it }
    tranxPurpLookup[picked.uppercase()]?.let { return it }
    tranxPurpLookup[picked.replace(' ', '_').uppercase()]?.let { return it }
<<<<<<< HEAD
    return tranxPurpLookup.values.firstOrNull { it.cmp.equals(picked, ignoreCase = true) }
=======
    return tranxPurpLookup.values.firstOrNull { it.assetLabel.equals(picked, ignoreCase = true) }
>>>>>>> f512e18 (added backend integration and db transaction update)
}

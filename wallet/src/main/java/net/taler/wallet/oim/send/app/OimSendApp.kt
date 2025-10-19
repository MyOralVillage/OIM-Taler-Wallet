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
import net.taler.wallet.BuildConfig.DEBUG

private enum class Screen { Send, Purpose, Qr }

@Composable
fun OimSendApp(model: MainViewModel) {
    OimTheme {
        val ctx = LocalContext.current.applicationContext
        val scope = rememberCoroutineScope()

        // Initialize transaction database
        if (DEBUG) TranxHistory.initTest(ctx)
        else TranxHistory.initTest(ctx)
        //  in future releases/builds, do TranxHistory.init(ctx)

        var screen by remember { mutableStateOf(Screen.Send) }
        var amount by remember { mutableStateOf(0) }
        var chosenPurpose by remember { mutableStateOf<String?>(null) }

        // for QR page
        var talerUri by remember { mutableStateOf<String?>(null) }

        // observe backend push state (avoid `.value` error)
        val pushState: OutgoingState by model.peerManager.pushState
            .collectAsState(initial = OutgoingIntro)

        // When backend answers with a tx id, navigate to QR.
        // If  TransactionManager exposes the selected tx with a talerUri,
        //  replace the placeholder below with the real URI.
        LaunchedEffect(pushState) {
            if (pushState is net.taler.wallet.peer.OutgoingResponse) {
                val txId = (pushState as net.taler.wallet.peer.OutgoingResponse).transactionId

                // Optional: select tx so normal wallet screens can show details
                // model.transactionManager.selectTransaction(txId)

                // QUICK way to show a QR immediately. Many Taler frontends encode a
                // "pay-push" URI using the tx id. If  backend exposes the URI,
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
                }
            )

            Screen.Qr -> QrScreen(
                talerUri = talerUri ?: "taler://invalid",
                onBack = { screen = Screen.Send }
            )
        }
    }
}

// TODO: needs complete refactoring, remove this and simplify things!
/** Try to resolve the UI label/cmp into one of the sealed TranxPurp objects. */
private fun mapPickedPurposeToTranxPurp(picked: String): TranxPurp? {
    tranxPurpLookup[picked]?.let { return it }
    tranxPurpLookup[picked.uppercase()]?.let { return it }
    tranxPurpLookup[picked.replace(' ', '_').uppercase()]?.let { return it }
    return tranxPurpLookup.values.firstOrNull { it.cmp.equals(picked, ignoreCase = true) }
}

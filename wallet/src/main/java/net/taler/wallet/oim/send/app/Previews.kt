package net.taler.wallet.oim.send.app

// got bricked w/ the merge corruption :(


//import androidx.compose.runtime.*
//import androidx.compose.ui.tooling.preview.Preview
//import net.taler.database.data_models.Amount
//import net.taler.database.data_models.TranxPurp
//import net.taler.wallet.oim.send.screens.PurposeScreen
//import net.taler.wallet.oim.send.screens.QrScreen
//import net.taler.wallet.oim.send.screens.SendScreen
//
//// Preview flow enum
//private enum class PreviewScreen { Send, Purpose, Qr }
//
///**
// * ## Full OIM Send Flow – Interactive Preview
// *
// * Demonstrates a simulated send flow in Compose Preview:
// * 1. SendScreen: tap notes to increment the counter, choose purpose or send.
// * 2. PurposeScreen: select a transaction purpose, then go to QR screen.
// * 3. QrScreen: displays QR code with amount and purpose.
// *
// * Use the Preview panel ▶ button to enter Interactive mode.
// */
//@Preview(
//    name = "OIM Flow – Interactive (Landscape)",
//    device = "spec:width=960dp,height=480dp,orientation=landscape,dpi=440",
//    showBackground = true,
//    showSystemUi = false
//)
//@Composable
//fun OimSendAppPreview() {
//    OimTheme {
//        var screen by remember { mutableStateOf(PreviewScreen.Send) }
//        var amount by remember { mutableStateOf(Amount("SLE", 240L, 0)) }
//        var chosenPurpose by remember { mutableStateOf<TranxPurp?>(null) }
//
//        val currency = "SLE"
//        val balance = 700
//
//        when (screen) {
//            PreviewScreen.Send -> SendScreen(
//                balance = balance,
//                amount = amount,
//                onAdd = { amount += it },
//                onRemoveLast = { removed -> amount = (amount - removed).coerceAtLeast(0) },
//                onChoosePurpose = { screen = PreviewScreen.Purpose },
//                onSend = { screen = PreviewScreen.Purpose }
//            )
//            PreviewScreen.Purpose -> PurposeScreen(
//                balance = balance,
//                onBack = { screen = PreviewScreen.Send },
//                onDone = { picked ->
//                    chosenPurpose = picked
//                    screen = PreviewScreen.Qr
//                }
//            )
//            PreviewScreen.Qr -> QrScreen(
//                talerUri = "taler://pay-push?amount=SLE:$amount&summary=Groceries",
//                amount = Amount.fromString(currency, amount.toString()),
//                purpose = chosenPurpose,
//                onBack = { screen = PreviewScreen.Send }
//            )
//        }
//    }
//}
//
///**
// * Focused previews for each individual screen (interactive)
// */
//
//@Preview(name = "Send Screen – Interactive")
//@Composable
//fun SendScreenPreviewInteractive() {
//    var amount by remember { mutableStateOf(Amount.zero("SLE")) }
//    SendScreen(
//        balance = Amount("SLE", 240L, 0),
//        amount = amount,
//        onAdd = { amount += it },
//        onRemoveLast = { removed -> amount = (amount - removed).coerceAtLeast(Amount.zero("SLE")) },
//        onChoosePurpose = {},
//        onSend = {}
//    )
//}
//
//@Preview(name = "Purpose Screen – Interactive")
//@Composable
//fun PurposeScreenPreviewInteractive() {
//    PurposeScreen(
//        balance = Amount("SLE", 240L, 0),
//        onBack = {},
//        onDone = {}
//    )
//}
//
//@Preview(name = "QR Screen Preview")
//@Composable
//fun QrScreenPreview() {
//    QrScreen(
//        talerUri = "taler://pay-push?amount=SLE:3&summary=Groceries",
//        amount = Amount.fromString("SLE", "3"),
//        purpose = null,
//        onBack = {}
//    )
//}

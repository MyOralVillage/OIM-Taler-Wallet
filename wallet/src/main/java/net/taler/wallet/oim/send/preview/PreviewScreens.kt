/**
 * ## PreviewScreens
 *
 * Provides Compose design-time previews for key OIM Send flow screens —
 * [SendScreen], [QrScreen], and [PurposeScreen] — rendered within [OimTheme].
 *
 * These previews simulate realistic input data (amounts, currencies, and
 * purposes) to allow layout validation and visual testing directly in Android
 * Studio’s preview pane without requiring a running wallet instance.
 *
 * ### Includes:
 * - `QrScreenPreview_WithSeparateFields()` – QR code view showing encoded Taler URI.
 * - `PurposeScreenPreview()` – purpose selection grid with back navigation.
 * - `SendScreenPreview()` – send interface with balance display and animated note setup.
 *
 * @see net.taler.wallet.oim.send.screens.SendScreen
 * @see net.taler.wallet.oim.send.screens.QrScreen
 * @see net.taler.wallet.oim.send.screens.PurposeScreen
 */


package net.taler.wallet.oim.send.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.taler.database.data_models.*
import net.taler.wallet.oim.send.app.OimTheme
import net.taler.wallet.oim.send.screens.PurposeScreen
import net.taler.wallet.oim.send.screens.QrScreen
import net.taler.wallet.oim.send.screens.SendScreen

/** Preview of [QrScreen] with separate display fields for amount, currency, label, and purpose. */
@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun QrScreenPreview_WithSeparateFields() {
    OimTheme {
        QrScreen(
            talerUri = "taler://pay-push?amount=SLE:3&summary=Groceries",
            amount = Amount("XOF", 10000L, 0),
            purpose = UTIL_WATR,
            onBack = {}
        )
    }
}

/** Preview of [PurposeScreen] allowing selection of a purpose or entering a custom purpose. */
@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun PurposeScreenPreview() {
    OimTheme {
        PurposeScreen(
            balance = Amount("CHF", 100L, 53),
            onBack = {},
            onDone = {}
        )
    }
}

/** Preview of [SendScreen] showing the send UI with balance and amount management.*/
@Preview(showBackground = true, widthDp = 1280, heightDp = 720)
@Composable
fun SendScreenPreview() {
    OimTheme {
        SendScreen(
            balance = Amount("EUR", 23L, 24),
            amount = Amount("EUR", 10L, 32),
            onAdd = {},
            onRemoveLast = {},
            onChoosePurpose = {},
            onSend = {}
        )
    }
}
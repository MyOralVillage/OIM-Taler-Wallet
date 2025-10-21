/*
 * This file is part of GNU Taler
 * (C) 2025 Taler Systems S.A.
 *
 * GNU Taler is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * GNU Taler is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>
 */

/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.app

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import net.taler.wallet.oim.send.screens.PurposeScreen
import net.taler.wallet.oim.send.screens.QrScreen
import net.taler.wallet.oim.send.screens.SendScreen

// Use the same enum as the app for consistency
private enum class PreviewScreen { Send, Purpose, Qr }

/**
 * Full-flow interactive preview:
 * - Tap notes: they fly up and increment the counter
 * - Tap Send (top-right) or "Choose purpose": go to Purpose
 * - Tap a purpose: go to QR
 * - Use the Preview panel ▶ button to enter Interactive mode
 */
@Preview(
    name = "OIM Flow – Interactive (Landscape)",
    // Force a landscape canvas with no system bars/app bar
    device = "spec:width=960dp,height=480dp,orientation=landscape,dpi=440",
    showBackground = true,
    showSystemUi = false
)
@Composable
fun OimSendAppPreview() {
    // Optional: wrap in your app theme so typography/colors match SendScreenPreview
    OimTheme {
        var screen by remember { mutableStateOf(PreviewScreen.Send) }
        var amount by remember { mutableStateOf(0) }
        var chosenPurpose by remember { mutableStateOf<String?>(null) }

        val currencyCode = "SLE"
        val currencyLabel = "Leones"
<<<<<<< HEAD
        val balance = 700
=======
        val balance = 25
>>>>>>> 5c7011a (fixed preview animations)

        when (screen) {
            PreviewScreen.Send -> SendScreen(
                balance = balance,
                amount = amount,
                onAdd = { amount += it },
                onRemoveLast = { removed -> amount = (amount - removed).coerceAtLeast(0) },
                onChoosePurpose = { screen = PreviewScreen.Purpose },
                onSend = { screen = PreviewScreen.Purpose }
            )
            PreviewScreen.Purpose -> PurposeScreen(
                balance = balance,
                onBack = { screen = PreviewScreen.Send },
                onDone = { picked ->
                    chosenPurpose = picked
                    screen = PreviewScreen.Qr
                }
            )
            PreviewScreen.Qr -> QrScreen(
<<<<<<< HEAD
                talerUri = "taler://pay-push?amount=SLE:3&summary=Groceries",
                amountText = amount.toString(),
=======
                amount = amount,
>>>>>>> 5c7011a (fixed preview animations)
                currencyCode = currencyCode,
                displayLabel = currencyLabel,
                purpose = chosenPurpose.orEmpty(),
                onBack = { screen = PreviewScreen.Send }
            )
        }
    }
}


/**
<<<<<<< HEAD
=======
 * If you want focused previews per-screen (also interactive):
>>>>>>> 5c7011a (fixed preview animations)

@Preview(name = "Send Screen – Interactive")
@Composable
fun SendScreenPreviewInteractive() {
    var amount by remember { mutableStateOf(0) }
    SendScreen(
        balance = 25,
        amount = amount,
        onAdd = { amount += it },
        onRemoveLast = { removed -> amount = (amount - removed).coerceAtLeast(0) },
        onChoosePurpose = {},
        onSend = {}
    )
}

@Preview(name = "Purpose Screen – Interactive")
@Composable
fun PurposeScreenPreviewInteractive() {
    PurposeScreen(
        balance = 25,
        onBack = {},
        onDone = {}
    )
}

@Preview(name = "QR Screen")
@Composable
fun QrScreenPreview() {
    QrScreen(
        amount = 3,
        currencyCode = "SLE",
        displayLabel = "Leones",
        purpose = "Groceries",
        onBack = {}
    )
}
 */
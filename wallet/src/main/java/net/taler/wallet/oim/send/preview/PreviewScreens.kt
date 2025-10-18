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

package net.taler.wallet.oim.send.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
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
            amountText = "3",
            currencyCode = "SLE",
            displayLabel = "Leones",
            purpose = "Groceries",
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
            balance = 25,
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
            balance = 25,
            amount = 3,
            onAdd = {},
            onRemoveLast = {},
            onChoosePurpose = {},
            onSend = {}
        )
    }
}
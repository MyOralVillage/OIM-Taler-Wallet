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

package net.taler.database.data_models

<<<<<<< HEAD:transaction-database/src/main/java/net/taler/database/data_models/CurrencyDivisor.kt
/**
 * Currency precision divisor used by both [Amount] and database storage.
 *
 * For an indexed database value `amount_i`:
 * ```
 * Amount.value    = floor(amount_i / CURRENCY_DIVISOR).toLong()
 * Amount.fraction = (amount_i % CURRENCY_DIVISOR).toInt()
 * ```
 *
 * **WARNING:** Modifying this constant will break entire database!
 */
const val CURRENCY_DIVISOR = 1e8
=======
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import net.taler.wallet.oim.send.app.OimTheme
import net.taler.wallet.oim.send.screens.QrScreen

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

>>>>>>> f512e18 (added backend integration and db transaction update):wallet/src/main/java/net/taler/wallet/oim/send/preview/QrScreenPreview.kt

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

package net.taler.wallet.oim.res_mapping_extensions

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import net.taler.common.R.drawable.*

/**
Maps button names to their corresponding image resources.
@property buttonName The name of the button (case-insensitive).
 */
internal class Buttons(val buttonName: String) {
    @Composable
    @DrawableRes
    fun resourceMapper() : Int =
        when (this.buttonName.lowercase()) {

            // chest buttons
            "chest_sl_open"     -> chest_sl_open
            "chest_sl_closed"   -> chest_sl_closed
            "chest_ci_open"     -> chest_ci_open
            "chest_ci_closed"   -> chest_ci_closed
            "chest_de_open"     -> chest_de_open
            "chest_de_closed"   -> chest_de_closed
            "chest_eu_open"     -> chest_eu_open
            "chest_eu_closed"   -> chest_eu_closed
            "chest_ch_open"     -> chest_ch_open
            "chest_ch_closed"   -> chest_ch_closed
            "chest_open"        -> chest_open
            "chest_closed"      -> chest_closed

            // actions
            "send"              -> send
            "receive"           -> receive
            "tranx_hist"        -> transaction_history
            "deposit"           -> deposit
            "withdraw"          -> withdraw_og
            "filter"            -> filter

            else ->
                throw IllegalArgumentException("Invalid button: $buttonName")
        }
}
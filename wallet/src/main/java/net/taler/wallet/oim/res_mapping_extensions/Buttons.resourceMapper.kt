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

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import net.taler.common.R.drawable.*

/**
Maps button names to their corresponding image resources.
@property buttonName The name of the button (case-insensitive).
 */
internal class Buttons(val buttonName: String) {
    @Composable
    fun resourceMapper() : ImageBitmap =
        when (this.buttonName.lowercase()) {

            // chest buttons
            "chest_sl_open"     -> ImageBitmap.imageResource(chest_sl_open)
            "chest_sl_closed"   -> ImageBitmap.imageResource(chest_sl_closed)
            "chest_ci_open"     -> ImageBitmap.imageResource(chest_ci_open)
            "chest_ci_closed"   -> ImageBitmap.imageResource(chest_ci_closed)
            "chest_de_open"     -> ImageBitmap.imageResource(chest_de_open)
            "chest_de_closed"   -> ImageBitmap.imageResource(chest_de_closed)
            "chest_eu_open"     -> ImageBitmap.imageResource(chest_eu_open)
            "chest_eu_closed"   -> ImageBitmap.imageResource(chest_eu_closed)
            "chest_ch_open"     -> ImageBitmap.imageResource(chest_ch_open)
            "chest_ch_closed"   -> ImageBitmap.imageResource(chest_ch_closed)
            "chest_open"        -> ImageBitmap.imageResource(chest_open)
            "chest_closed"      -> ImageBitmap.imageResource(chest_closed)

            // actions
            "send"              -> ImageBitmap.imageResource(send)
            "receive"           -> ImageBitmap.imageResource(receive)
            "tranx_hist"        -> ImageBitmap.imageResource(transaction_history)
            "deposit"           -> ImageBitmap.imageResource(deposit)
            "withdraw"          -> ImageBitmap.imageResource(withdrawal)
            "filter"            -> ImageBitmap.imageResource(filter)

            else ->
                throw IllegalArgumentException("Invalid button: " + buttonName)
        }
}
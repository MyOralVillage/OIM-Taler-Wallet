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
import net.taler.database.data_models.*

/**
 * Returns an [ImageBitmap] for this [TranxPurp].
 *
 * @receiver The [TranxPurp] to get a preview image for.
 * @return The [ImageBitmap] representing this purpose.
 */
@Composable
fun FilterableDirection.resourceMapper() : ImageBitmap =
    when (this) {
        FilterableDirection.OUTGOING ->
            ImageBitmap.imageResource(outgoing_transaction)
        FilterableDirection.INCOMING ->
            ImageBitmap.imageResource(incoming_transaction)
    }


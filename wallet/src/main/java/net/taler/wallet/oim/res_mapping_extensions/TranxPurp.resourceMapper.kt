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
fun TranxPurp.resourceMapper() : ImageBitmap =
    when (this) {
        EDUC_CLTH -> ImageBitmap.imageResource(school_uniforms)
        EDUC_SCHL -> ImageBitmap.imageResource(schooling)
        EDUC_SUPL -> ImageBitmap.imageResource(school_supplies)
        EXPN_CELL -> ImageBitmap.imageResource(phone)
        EXPN_DEBT -> ImageBitmap.imageResource(loan)
        EXPN_FARM -> ImageBitmap.imageResource(farming)
        EXPN_GRCR -> ImageBitmap.imageResource(groceries)
        EXP_MRKT -> ImageBitmap.imageResource(market_stall)
        EXPN_PTRL -> ImageBitmap.imageResource(gas)
        EXPN_RENT -> ImageBitmap.imageResource(housing)
        EXPN_TOOL -> ImageBitmap.imageResource(tools)
        EXPN_TRPT -> ImageBitmap.imageResource(transportation)
        HLTH_DOCT -> ImageBitmap.imageResource(doctor_appointment)
        HLTH_MEDS -> ImageBitmap.imageResource(medicine)
        TRNS_RECV -> ImageBitmap.imageResource(receive)
        TRNS_SEND -> ImageBitmap.imageResource(send)
        UTIL_ELEC -> ImageBitmap.imageResource(electricity)
        UTIL_WATR -> ImageBitmap.imageResource(water)
    }


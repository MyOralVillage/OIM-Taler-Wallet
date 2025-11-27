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

package net.taler.wallet.oim.resourceMappers

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import net.taler.common.R.drawable.*
import net.taler.database.data_models.*

/**
 * Returns the [DrawableRes] for this [TranxPurp].
 *
 * @receiver The [TranxPurp] to get a preview image for.
 * @return The [DrawableRes] representing this purpose.
 */
@Composable
@DrawableRes
internal fun TranxPurp.resourceMapper() : Int =
    when (this) {
        EDUC_CLTH -> school_uniforms
        EDUC_SCHL -> schooling
        EDUC_SUPL -> school_supplies
        EXPN_CELL -> phone
        EXPN_DEBT -> loan
        EXPN_FARM -> farming
        EXPN_GRCR -> groceries
        EXP_MRKT -> market_stall
        EXPN_PTRL -> gas
        EXPN_RENT -> housing
        EXPN_TOOL -> tools
        EXPN_TRPT -> transportation
        HLTH_DOCT -> doctor_appointment
        HLTH_MEDS -> medicine
        TRNS_RECV -> receive
        TRNS_SEND -> send
        UTIL_ELEC -> electricity
        UTIL_WATR -> water
    }


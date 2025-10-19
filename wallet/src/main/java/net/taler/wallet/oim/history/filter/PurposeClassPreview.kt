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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.taler.database.data_models.*
import net.taler.database.filter.PurposeFilter
import net.taler.wallet.oim.history.filter.PurposeGrid
import net.taler.common.R.drawable.*

/**
 * Statically maps purp res -> drawable
 */
@Composable
internal fun TranxPurp.previewMapper() : ImageBitmap =
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


@Preview(showBackground = true, name = "Purpose Grid - No Selection", heightDp = 800)
@Composable
private fun PurposeGridPreview() {
    var purposeFilter by remember { mutableStateOf<PurposeFilter?>(null) }

    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PurposeGrid(
                purposeFilter = purposeFilter,
                onPurposeSelected = { purposeFilter = it },
                purposeMap = tranxPurpLookup,
                isPreview = true
            )
        }
    }
}

@Preview(showBackground = true, name = "Purpose Grid - Single Selection", heightDp = 800)
@Composable
private fun PurposeGridPreviewSingleSelect() {
    var purposeFilter by remember {
        mutableStateOf<PurposeFilter?>(PurposeFilter.Exact(HLTH_DOCT))
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PurposeGrid(
                purposeFilter = purposeFilter,
                onPurposeSelected = { purposeFilter = it },
                purposeMap = tranxPurpLookup,
                isPreview = true
            )
        }
    }
}

@Preview(showBackground = true, name = "Purpose Grid - Multiple Selection", heightDp = 800)
@Composable
private fun PurposeGridPreviewMultiSelect() {
    var purposeFilter by remember {
        mutableStateOf<PurposeFilter?>(
            PurposeFilter.OneOrMoreOf(setOf(HLTH_DOCT, EDUC_SCHL, EXPN_GRCR, UTIL_WATR))
        )
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PurposeGrid(
                purposeFilter = purposeFilter,
                onPurposeSelected = { purposeFilter = it },
                purposeMap = tranxPurpLookup,
                isPreview = true
            )
        }
    }
}

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

package net.taler.wallet.oim.history.filter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import net.taler.database.TranxHistory
import net.taler.database.filter.*
import net.taler.database.data_models.*
import net.taler.wallet.BuildConfig.DEBUG

/**
 * Main screen for filtering transaction history.
 * Provides intuitive visual controls for users to filter by date, amount, purpose, and direction.
 *
 * @param onApplyFilter Callback invoked when user applies the filter with the constructed TranxFilter
 * @param onDismiss Callback invoked when user closes the filter screen
 */
@Composable
fun FilterScreen (
    onApplyFilter:  (TranxFilter) -> Unit,
    onDismiss:      () -> Unit
){

    val ctx = LocalContext.current.applicationContext

    // if database is not initialized, initialize it
    // Initialize transaction database
    if (DEBUG) TranxHistory.initTest(ctx)
    else TranxHistory.initTest(ctx)
    //  in future releases/builds, do TranxHistory.init(ctx)

    // get maximum / minimum values
    val minDtm = TranxHistory.miniDtm
    val maxiDtm = TranxHistory.maxiDtm
    val miniAmt = TranxHistory.miniAmt
    val maxiAmt = TranxHistory.maxiAmt

    // get default filter state


}
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
package net.taler.wallet.oim.history.app

/**
 * HISTORY MODULE – COMPOSABLE APP SHELL
 *
 * This file defines `HistoryApp`, the top-level composable that wires the
 * transaction history UX to the rest of the wallet:
 *
 *  - Loads transaction data from the local database via `TranxHistory`
 *    (initTest in DEBUG builds, init in release).
 *  - Holds the current list of `Tranx` in Compose state.
 *  - Manages the view-mode toggle (RiverSceneCanvas vs TransactionsList)
 *    using a `showRiver` boolean saved with `rememberSaveable`.
 *  - Forwards balance + navigation callbacks into
 *    `OimTransactionHistoryScreen`, which renders the actual UI.
 *
 * IMPORT / DATA CONNECTIONS:
 *  - Uses `MainViewModel` only as a dependency passed in from the hosting
 *    Activity; business logic and balances are computed there.
 *  - Reads transaction history through `TranxHistory` from the database layer.
 *  - Delegates all rendering and interaction to
 *    `net.taler.wallet.oim.history.screens.OimTransactionHistoryScreen`.
 *
 * USAGE:
 *  - Called from `HistoryActivity` and `HistoryHostActivity` as the single
 *    Composable root for the OIM transaction history flow.
 *  - Supports `previewTransactions` so designers/devs can render the UI
 *    without hitting the real database.
 */

import android.annotation.SuppressLint
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import kotlinx.serialization.InternalSerializationApi
import net.taler.database.TranxHistory
import net.taler.database.data_models.Amount
import net.taler.database.data_models.Tranx
import net.taler.wallet.BuildConfig
import net.taler.wallet.MainViewModel
import net.taler.wallet.oim.history.screens.OimTransactionHistoryScreen

/**
 * Top-level composable for the OIM transaction history flow.
 *
 * This function orchestrates the transaction history UX, handling:
 * - Loading transaction history from the database
 * - View mode switching (river vs list)
 * - Transaction selection and detail display
 * - Navigation to send/receive flows
 *
 * @param model Shared [MainViewModel] for UI state and business logic
 * @param balanceAmount Current account balance to display
 * @param onHome Lambda invoked when user navigates back
 * @param onSendClick Lambda invoked when user taps send button
 * @param onReceiveClick Lambda invoked when user taps receive button
 * @param previewTransactions Optional transactions for preview/testing
 */
@OptIn(InternalSerializationApi::class)
@Composable
fun HistoryApp(
    model: MainViewModel,
    balanceAmount: Amount,
    onHome: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onReceiveClick: () -> Unit = {},
    previewTransactions: List<Tranx>? = null,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Load transactions from database
    var txns by remember { mutableStateOf(previewTransactions ?: emptyList()) }

    LaunchedEffect(previewTransactions) {
        if (previewTransactions == null) {
            if (BuildConfig.DEBUG) TranxHistory.initTest(context)
            else TranxHistory.init(context)
            txns = TranxHistory.getHistory()
        }
    }

    // View state
    var showRiver by rememberSaveable { mutableStateOf(true) }

    OimTransactionHistoryScreen(
        transactions = txns,
        balanceAmount = balanceAmount,
        showRiver = showRiver,
        onToggleView = { showRiver = !showRiver },
        onHome = onHome,
        onSendClick = onSendClick,
        onReceiveClick = onReceiveClick,
        modifier = modifier
    )
}
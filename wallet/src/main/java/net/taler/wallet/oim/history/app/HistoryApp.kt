package net.taler.wallet.oim.history.app

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
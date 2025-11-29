package net.taler.wallet.oim.history.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import kotlinx.serialization.InternalSerializationApi
import net.taler.database.data_models.Amount
import net.taler.wallet.MainViewModel
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.oim.history.app.HistoryApp


/**
 * Activity responsible for displaying transaction history.
 *
 * This activity initializes a [MainViewModel] and sets up the Compose UI
 * using [HistoryApp]. Navigation callbacks control screen transitions.
 */
class HistoryActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    @OptIn(InternalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val balanceState by model.balanceManager.state.observeAsState(BalanceState.None)

            val balanceAmount = remember(balanceState) {
                val success = balanceState as? BalanceState.Success
                val entry = success?.balances?.firstOrNull()
                Amount.fromString(
                    entry?.currency ?: "KUDOS",
                    entry?.available?.toString(showSymbol = false) ?: "0"
                )
            }

            HistoryApp(
                model = model,
                balanceAmount = balanceAmount,
                onHome = { finish() },
                onSendClick = { finish() }, // MainFragment will handle navigation
                onReceiveClick = { finish() } // MainFragment will handle navigation
            )
        }
    }
}


/**
 * Host activity that embeds transaction history within another feature.
 *
 * Identical to [HistoryActivity] but intended for use when history needs
 * to be hosted within a different navigation context.
 */
class HistoryHostActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    @OptIn(InternalSerializationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val balanceState by model.balanceManager.state.observeAsState(BalanceState.None)

            val balanceAmount = remember(balanceState) {
                val success = balanceState as? BalanceState.Success
                val entry = success?.balances?.firstOrNull()
                Amount.fromString(
                    entry?.currency ?: "KUDOS",
                    entry?.available?.toString(showSymbol = false) ?: "0"
                )
            }

            HistoryApp(
                model = model,
                balanceAmount = balanceAmount,
                onHome = { finish() },
                onSendClick = { finish() },
                onReceiveClick = { finish() }
            )
        }
    }
}
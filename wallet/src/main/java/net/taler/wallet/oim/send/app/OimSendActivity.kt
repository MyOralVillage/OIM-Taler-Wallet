/**
 * ## OimSendActivity
 *
 * Entry-point `ComponentActivity` for launching the OIM Send flow inside the GNU Taler Wallet.
 * Hosts the composable hierarchy defined in [OimSendApp] and connects it with
 * the shared [MainViewModel] to access wallet state, balances, and peer transactions.
 *
 * This lightweight activity acts as a dedicated surface for the send experience,
 * keeping navigation isolated from the rest of the app while still reusing global
 * wallet logic through the injected [MainViewModel].
 *
 * @see OimSendApp
 * @see net.taler.wallet.MainViewModel
 */

package net.taler.wallet.oim.send.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import net.taler.wallet.MainViewModel

class OimSendActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { OimSendApp(model = model) }
    }
}

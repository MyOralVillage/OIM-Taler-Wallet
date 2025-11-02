/**
 * ## OimSendHostActivity
 *
 * Alternative host `ComponentActivity` for embedding the OIM Send flow,
 * functionally equivalent to [OimSendActivity] but reserved for cases where
 * the wallet shell or navigation framework launches the send module as an
 * independent host container.
 *
 * This class exists to support multiple entry configurations or intents
 * without duplicating composable logic — both activities simply invoke
 * [OimSendApp] with the shared [MainViewModel].
 *
 * @see OimSendActivity
 * @see OimSendApp
 */

package net.taler.wallet.oim.send.app


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import net.taler.wallet.MainViewModel


class OimSendHostActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OimSendApp(
                model = model,
                onHome = { finish() }   // ← same behavior here
            )
        }
    }
}

package net.taler.wallet.oim.send.app



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import net.taler.wallet.MainViewModel
import kotlin.getValue

/**
 * Activity responsible for managing the default OIM send flow.
 *
 * This activity initializes a [MainViewModel] using the `viewModels()` Kotlin property delegate,
 * and sets up the Compose UI using [SendApp]. When `onHome` is invoked, the activity exits
 * by calling [finish].
 *
 * This is typically launched when the user performs a send operation directly
 * within the main navigation of the app.
 */
class OimSendActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SendApp(
                model = model,
                onHome = { finish() }
            )
        }
    }
}

/**
 * Host activity that embeds the OIM send flow inside another feature or module.
 *
 * The functionality is identical to [OimSendActivity], but this class is intended
 * to be used in cases where the sending flow needs to be hosted within a different
 * navigation context or launched independently (e.g., from a deep link or plugin).
 *
 * It also initializes a local [MainViewModel] and provides an `onHome` handler
 * that closes the activity via [finish].
 */
class SendActivity : ComponentActivity() {
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SendApp(
                model = model,
                onHome = { finish() } // Same behavior as OimSendActivity for now
            )
        }
    }
}

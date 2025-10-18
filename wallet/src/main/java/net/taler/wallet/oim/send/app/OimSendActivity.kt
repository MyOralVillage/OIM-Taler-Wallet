package net.taler.wallet.oim.send.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import net.taler.wallet.MainViewModel

class OimSendActivity : ComponentActivity() {

    // Get the AndroidViewModel (requires the app context; default factory handles it)
    private val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OimSendApp(model = model)
        }
    }
}

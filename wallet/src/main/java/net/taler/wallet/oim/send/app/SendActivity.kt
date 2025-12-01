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
package net.taler.wallet.oim.send.app

/**
 * SEND MODULE – ANDROID ENTRY ACTIVITIES
 *
 * This file declares the Android `ComponentActivity` hosts for the OIM send
 * flow:
 *
 *  - OimSendActivity:
 *      • Default entry point when the user starts a send operation from the
 *        main wallet UI.
 *      • Instantiates a shared [MainViewModel] via `viewModels()`.
 *      • Sets the Compose content to [SendApp], wiring `onHome` to `finish()`
 *        so returning from the flow simply closes the activity.
 *
 *  - SendActivity:
 *      • Alternate host for the same [SendApp] UI, intended for cases where
 *        the send flow is launched from a different navigation context
 *        (e.g., deep links, plugins, or embedded modules).
 *      • Currently mirrors OimSendActivity’s behaviour, also finishing the
 *        activity when `onHome` is invoked.
 *
 * INTEGRATION:
 *  - Both activities delegate all business logic and UI state management to
 *    [MainViewModel] and the composable shell in `SendApp.kt`.
 *  - Navigation back to the rest of the app is handled externally once these
 *    activities call `finish()`.
 */

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

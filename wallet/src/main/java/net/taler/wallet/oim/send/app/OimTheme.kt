/**
 * ## OimTheme
 *
 * Minimal Material 3 theme wrapper for the OIM Send UI.
 * Provides consistent typography, color, and shape defaults across
 * composables such as [SendScreen], [PurposeScreen], and [QrScreen].
 *
 * This lightweight theme currently delegates directly to
 * [MaterialTheme] but exists as an extension point for future
 * customization (e.g. Taler-specific color palettes or dark-mode tuning).
 *
 * @param content The composable hierarchy to be styled within the theme.
 *
 * @see androidx.compose.material3.MaterialTheme
 */


package net.taler.wallet.oim.send.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun OimTheme(content: @Composable () -> Unit) { MaterialTheme(content = content) }

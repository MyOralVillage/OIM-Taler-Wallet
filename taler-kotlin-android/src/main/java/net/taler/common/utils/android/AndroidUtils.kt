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

package net.taler.utils.android

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.content.Intent
import android.content.Intent.EXTRA_INITIAL_INTENTS
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.os.Build.VERSION.SDK_INT
import android.os.Looper
import android.text.format.DateUtils.*
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresPermission
import androidx.annotation.StringRes
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import net.taler.common.R
import net.taler.common.utils.model.Version
import net.taler.lib.android.ErrorBottomSheet
import androidx.core.view.isInvisible
import androidx.core.view.isVisible

/**
 * Fades the view in using an alpha animation.
 * @param endAction optional lambda to invoke when animation ends
 */
fun View.fadeIn(endAction: () -> Unit = {}) {
    if (isVisible && alpha == 1f) return
    alpha = 0f
    visibility = VISIBLE
    animate().alpha(1f).withEndAction {
        if (context != null) endAction()
    }.start()
}

/**
 * Fades the view out using an alpha animation.
 * @param endAction optional lambda to invoke when animation ends
 */
fun View.fadeOut(endAction: () -> Unit = {}) {
    if (isInvisible) return
    animate().alpha(0f).withEndAction {
        if (context == null) return@withEndAction
        visibility = INVISIBLE
        alpha = 1f
        endAction()
    }.start()
}

/** Hides the soft keyboard from the view. */
fun View.hideKeyboard() {
    context.getSystemService<InputMethodManager>()
        ?.hideSoftInputFromWindow(windowToken, 0)
}

/** Ensures the current thread is the main (UI) thread. */
fun assertUiThread() {
    check(Looper.getMainLooper().thread == Thread.currentThread())
}

/**
 * Marks an expression as exhaustive for 'when' usage.
 * Useful to force handling all branches of a sealed class or enum.
 */
val <T> T.exhaustive: T
    get() = this

/**
 * Checks if the device is online (has internet connectivity).
 * Requires [ACCESS_NETWORK_STATE] permission.
 */
@RequiresPermission(ACCESS_NETWORK_STATE)
fun Context.isOnline(): Boolean {
    val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    return if (SDK_INT < 29) {
        @Suppress("DEPRECATION")
        cm.activeNetworkInfo?.isConnected == true
    } else {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        capabilities.hasCapability(NET_CAPABILITY_INTERNET)
    }
}

/**
 * Shows an error using [ErrorBottomSheet].
 */
fun FragmentActivity.showError(mainText: String, detailText: String = "") =
    ErrorBottomSheet.newInstance(mainText, detailText)
        .show(supportFragmentManager, "ERROR_BOTTOM_SHEET")

fun FragmentActivity.showError(@StringRes mainId: Int, detailText: String = "") =
    showError(getString(mainId), detailText)

fun Fragment.showError(mainText: String, detailText: String = "") =
    ErrorBottomSheet.newInstance(mainText, detailText)
        .show(parentFragmentManager, "ERROR_BOTTOM_SHEET")

fun Fragment.showError(@StringRes mainId: Int, detailText: String = "") =
    showError(getString(mainId), detailText)

/** Safely starts an activity, catching [ActivityNotFoundException]. */
fun Context.startActivitySafe(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Log.e("taler-kotlin-android", "Error starting $intent", e)
    }
}

/** Returns true if any app can handle the given URI. */
fun Context.canAppHandleUri(uri: String): Boolean {
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
    return packageManager.queryIntentActivities(intent, 0).any {
        it.activityInfo.packageName != packageName
    }
}

/**
 * Opens a URI in a safe manner using available apps.
 * @param uri the URI to open
 * @param title chooser dialog title
 * @param excludeOwn if true, excludes this app from the chooser
 */
fun Context.openUri(uri: String, title: String, excludeOwn: Boolean = true) {
    val intent = Intent(Intent.ACTION_VIEW, uri.toUri())
    if (excludeOwn) {
        val possibleIntents = packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .map { possible ->
                Intent(intent).apply { `package` = possible.activityInfo.packageName }
            }
        if (possibleIntents.isEmpty()) return

        val defaultResolveInfo = packageManager.resolveActivity(intent, 0)
        val chooser = if (defaultResolveInfo == null || !possibleIntents.any { it.`package` == defaultResolveInfo.activityInfo.packageName }) {
            Intent.createChooser(possibleIntents[0], title).apply {
                putExtra(EXTRA_INITIAL_INTENTS, possibleIntents.drop(1).toTypedArray())
            }
        } else {
            intent
        }
        startActivitySafe(chooser)
    } else {
        startActivitySafe(Intent.createChooser(intent, title))
    }
}

/** Shares a text string via available apps. */
fun Context.shareText(text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, text)
        type = "text/plain"
    }
    startActivitySafe(Intent.createChooser(intent, null))
}

/** Navigates using NavDirections from a Fragment. */
fun Fragment.navigate(directions: NavDirections) = findNavController().navigate(directions)

/**
 * Converts a timestamp to relative time for UI display.
 * e.g., "5 minutes ago" or formatted date if older than 2 days.
 */
fun Long.toRelativeTime(context: Context): CharSequence {
    val now = System.currentTimeMillis()
    return if (now - this > DAY_IN_MILLIS * 2) {
        val flags = FORMAT_SHOW_TIME or FORMAT_SHOW_DATE or FORMAT_ABBREV_MONTH or FORMAT_NO_YEAR
        formatDateTime(context, this, flags)
    } else {
        getRelativeTimeSpanString(this, now, MINUTE_IN_MILLIS, FORMAT_ABBREV_RELATIVE)
    }
}

/** Converts a timestamp to an absolute time string. */
fun Long.toAbsoluteTime(context: Context): CharSequence {
    val flags = FORMAT_SHOW_TIME or FORMAT_SHOW_DATE or FORMAT_SHOW_YEAR
    return formatDateTime(context, this, flags)
}

/** Converts a timestamp to a short date string. */
fun Long.toShortDate(context: Context): CharSequence {
    val flags = FORMAT_SHOW_DATE or FORMAT_SHOW_YEAR or FORMAT_ABBREV_ALL
    return formatDateTime(context, this, flags)
}

/**
 * Returns a user-friendly message if the [otherVersion] is incompatible with this version.
 * Returns null if compatible.
 */
fun Version.getIncompatibleStringOrNull(context: Context, otherVersion: String): String? {
    val other = Version.parse(otherVersion) ?: return context.getString(R.string.version_invalid)
    val match = compare(other) ?: return context.getString(R.string.version_invalid)
    if (match.compatible) return null
    if (match.currentCmp < 0) return context.getString(R.string.version_too_old)
    if (match.currentCmp > 0) return context.getString(R.string.version_too_new)
    throw AssertionError("$this == $other")
}

/** Copies a string to the clipboard. */
fun copyToClipBoard(context: Context, label: String, str: String) {
    val clipboard = context.getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(label, str)
    clipboard?.setPrimaryClip(clip)
}

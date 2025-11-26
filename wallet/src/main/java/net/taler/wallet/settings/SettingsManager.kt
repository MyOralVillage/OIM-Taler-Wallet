/*
 * This file is part of GNU Taler
 * (C) 2022 Taler Systems S.A.
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

package net.taler.wallet.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.taler.wallet.R
import net.taler.wallet.backend.WalletBackendApi
import net.taler.wallet.backend.WalletResponse.Error
import net.taler.wallet.backend.WalletResponse.Success
import net.taler.wallet.balances.BalanceManager
import net.taler.wallet.balances.ScopeInfo
import org.json.JSONObject

/**
 * SettingsManager centralizes all reading/writing of user settings stored in the
 * protobuf-backed DataStore (user_prefs.proto), as well as certain maintenance
 * and export/import utilities for logs and the wallet database.
 *
 * This class wraps all preference persistence in typed helper functions.
 *
 * @param context Application context used for DataStore and resource access.
 * @param api WalletBackendApi used for import/export/clearing the database.
 * @param scope CoroutineScope where all async operations are launched.
 *              Typically this is a ViewModel or lifecycle scope.
 * @param balanceManager Manager used to refresh/reset balances after DB import/clear.
 */
class SettingsManager(
    private val context: Context,
    private val api: WalletBackendApi,
    private val scope: CoroutineScope,
    private val balanceManager: BalanceManager,
) {

    /**
     * Reads the currently selected scope (PrefsScopeInfo) from the DataStore.
     *
     * - Returns a Flow<ScopeInfo?> that emits whenever the preferences change.
     * - If the stored proto has a selectedScope field, it is wrapped in ScopeInfo.
     * - If not present, returns null.
     *
     * @param c A Context with access to userPreferencesDataStore.
     */
    fun getSelectedScope(c: Context) =
        c.userPreferencesDataStore.data.map { prefs ->
            if (prefs.hasSelectedScope()) {
                ScopeInfo.fromPrefs(prefs.selectedScope)
            } else {
                null
            }
        }

    /**
     * Saves (or clears) the currently selected scope inside the protobuf DataStore.
     *
     * - If scopeInfo != null → serializes and stores it.
     * - If scopeInfo == null → clears the field.
     *
     * This function launches its update inside the provided CoroutineScope.
     *
     * @param c Context providing the DataStore.
     * @param scopeInfo The new scope to save, or null to remove it.
     */
    fun saveSelectedScope(c: Context, scopeInfo: ScopeInfo?) =
        scope.launch {
            c.userPreferencesDataStore.updateData { current ->
                if (scopeInfo != null) {
                    current.toBuilder()
                        .setSelectedScope(scopeInfo.toPrefs())
                        .build()
                } else {
                    current.toBuilder()
                        .clearSelectedScope()
                        .build()
                }
            }
        }

    /**
     * Returns a Flow<Boolean> indicating whether the action button has been used.
     *
     * - Defaults to false if the proto field is not present.
     *
     * @param c Context used for DataStore access.
     */
    fun getActionButtonUsed(c: Context) =
        c.userPreferencesDataStore.data.map { prefs ->
            if (prefs.hasActionButtonUsed()) {
                prefs.actionButtonUsed
            } else {
                false
            }
        }

    /**
     * Marks the action button as used (sets the proto field to true).
     *
     * This is a one-way toggle; it never sets the field back to false.
     * Launched in the provided coroutine scope.
     *
     * @param c Context providing the DataStore.
     */
    fun saveActionButtonUsed(c: Context) = scope.launch {
        c.userPreferencesDataStore.updateData { current ->
            current.toBuilder()
                .setActionButtonUsed(true)
                .build()
        }
    }

    /**
     * Reads whether developer mode is enabled.
     *
     * - Returns a Flow<Boolean>.
     * - Defaults to false if field is unset.
     *
     * @param c Context providing the DataStore.
     */
    fun getDevModeEnabled(c: Context) =
        c.userPreferencesDataStore.data.map { prefs ->
            if (prefs.hasDevModeEnabled()) {
                prefs.devModeEnabled
            } else {
                false
            }
        }

    /**
     * Enables or disables developer mode in preferences.
     *
     * @param c Context providing the DataStore.
     * @param enabled Whether dev mode should be enabled.
     */
    fun setDevModeEnabled(c: Context, enabled: Boolean) =
        scope.launch {
            c.userPreferencesDataStore.updateData { current ->
                current.toBuilder()
                    .setDevModeEnabled(enabled)
                    .build()
            }
        }

    /**
     * Returns whether biometric lock is enabled.
     *
     * - Emits a Flow<Boolean>.
     * - Defaults to false if field not present.
     *
     * @param c Context providing the DataStore.
     */
    fun getBiometricLockEnabled(c: Context) =
        c.userPreferencesDataStore.data.map { prefs ->
            if (prefs.hasBiometricLockEnabled()) {
                prefs.biometricLockEnabled
            } else {
                false
            }
        }

    /**
     * Saves the biometric lock preference.
     *
     * @param c Context providing the DataStore.
     * @param enabled Whether biometric locking should be active.
     */
    fun setBiometricLockEnabled(c: Context, enabled: Boolean) =
        scope.launch {
            c.userPreferencesDataStore.updateData { current ->
                current.toBuilder()
                    .setBiometricLockEnabled(enabled)
                    .build()
            }
        }

    /**
     * Exports the device's logcat output to a user-selected URI.
     *
     * - If URI is null, shows a user-visible error message.
     * - If valid, runs "logcat -d" in an IO coroutine and writes the output.
     * - Displays a Toast on success or error.
     *
     * @param uri URI returned from Android's Storage Access Framework.
     */
    fun exportLogcat(uri: Uri?) {
        if (uri == null) {
            onLogExportError()
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                    val command = arrayOf("logcat", "-d", "*:V")
                    val proc = Runtime.getRuntime().exec(command)
                    proc.inputStream.copyTo(outputStream)
                } ?: onLogExportError()
            } catch (e: Exception) {
                Log.e(SettingsManager::class.simpleName, "Error exporting log: ", e)
                onLogExportError()
                return@launch
            }
            withContext(Dispatchers.Main) {
                Toast.makeText(context, R.string.settings_logcat_success, LENGTH_LONG).show()
            }
        }
    }

    /** Shows a Toast error for log export failures. */
    private fun onLogExportError() {
        Toast.makeText(context, R.string.settings_logcat_error, LENGTH_LONG).show()
    }

    /**
     * Exports the wallet database to the given URI.
     *
     * - Calls the backend API: rawRequest("exportDb")
     * - On success, JSON-encodes the result and writes it to the output stream.
     * - On error, shows a toast.
     *
     * @param uri User-selected output file URI.
     */
    fun exportDb(uri: Uri?) {
        if (uri == null) {
            onDbExportError()
            return
        }

        scope.launch(Dispatchers.IO) {
            when (val response = api.rawRequest("exportDb")) {
                is Success -> {
                    try {
                        context
                        .contentResolver.openOutputStream(uri, "wt")?.use { outputStream ->
                            val data = Json.encodeToString(response.result)
                            val writer = outputStream.bufferedWriter()
                            writer.write(data)
                            writer.close()
                        }
                    } catch(e: Exception) {
                        Log.e(SettingsManager::class.simpleName, "Error exporting db: ", e)
                        withContext(Dispatchers.Main) {
                            onDbExportError()
                        }
                        return@launch
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            R.string.settings_db_export_success,
                            LENGTH_LONG
                        ).show()
                    }
                }
                is Error -> {
                    Log.e(
                        SettingsManager::class.simpleName,
                        "Error exporting db: ${response.error}"
                    )
                    withContext(Dispatchers.Main) {
                        onDbExportError()
                    }
                    return@launch
                }
            }
        }
    }

    /** Shows a Toast error for DB export failures. */
    private fun onDbExportError() {
        Toast.makeText(context, R.string.settings_db_export_error, LENGTH_LONG).show()
    }

    /**
     * Imports a database dump from a user-selected URI.
     *
     * - Reads JSON from the input stream.
     * - Sends it to backend API: rawRequest("importDb") { put("dump", jsonData) }
     * - On success, reloads balances.
     * - On failure, shows an error toast.
     *
     * @param uri Input file URI chosen by the user.
     */
    fun importDb(uri: Uri?) {
        if (uri == null) {
            onDbImportError()
            return
        }

        scope.launch(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use {  inputStream ->
                try {
                    val reader = inputStream.bufferedReader()
                    val strData = reader.readText()
                    reader.close()
                    val jsonData = JSONObject(strData)
                    when (val response = api.rawRequest("importDb") {
                        put("dump", jsonData)
                    }) {
                        is Success -> {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    R.string.settings_db_import_success,
                                    LENGTH_LONG
                                ).show()
                                balanceManager.loadBalances()
                            }
                        }
                        is Error -> {
                            Log.e(
                                SettingsManager::class.simpleName,
                                "Error importing db: ${response.error}"
                            )
                            withContext(Dispatchers.Main) {
                                onDbImportError()
                            }
                            return@launch
                        }
                    }
                } catch (e: Exception) {
                    Log.e(SettingsManager::class.simpleName, "Error importing db: ", e)
                    withContext(Dispatchers.Main) {
                        onDbImportError()
                    }
                    return@launch
                }
            }
        }
    }

    /** Shows a Toast if DB import fails. */
    private fun onDbImportError() {
        Toast.makeText(context, R.string.settings_db_import_error, LENGTH_LONG).show()
    }

    /**
     * Clears the wallet database by calling backend request "clearDb".
     *
     * - Calls the callback `onSuccess` on success.
     * - Resets the balance manager.
     * - Logs and notifies user on error.
     *
     * @param onSuccess Callback that executes after a successful DB clear.
     */
    fun clearDb(onSuccess: () -> Unit) {
        scope.launch {
            when (val response = api.rawRequest("clearDb")) {
                is Success -> {
                    onSuccess()
                    balanceManager.resetBalances()
                }
                is Error -> {
                    Log.e(
                        SettingsManager::class.simpleName,
                        "Error cleaning db: ${response.error}"
                    )
                    onDbClearError()
                }
            }
        }
    }

    /** Shows a Toast for DB clear errors. */
    private fun onDbClearError() {
        Toast.makeText(context, R.string.settings_db_clear_error, LENGTH_LONG).show()
    }
}

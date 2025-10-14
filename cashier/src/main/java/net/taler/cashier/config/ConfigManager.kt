/*
 * This file is part of GNU Taler
 * (C) 2020 Taler Systems S.A.
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

package net.taler.cashier.config

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
import androidx.security.crypto.MasterKeys
import androidx.security.crypto.MasterKeys.AES256_GCM_SPEC
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders.Authorization
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.taler.cashier.Response
import net.taler.cashier.Response.Companion.response
import net.taler.common.utils.model.Version
import net.taler.utils.android.getIncompatibleStringOrNull
import androidx.core.content.edit

// TODO: fix gradle ffs
val VERSION_BANK = Version.parse("\"4:0:0\"")!!
private const val PREF_NAME = "net.taler.cashier.prefs"
private const val PREF_KEY_BANK_URL = "bankUrl"
private const val PREF_KEY_USERNAME = "username"
private const val PREF_KEY_PASSWORD = "password"
private const val PREF_KEY_CURRENCY = "currency"
/**
 * Manager class responsible for handling and securely storing the Taler merchant configuration.
 * It manages encrypted preferences, verifies remote configuration validity, and exposes observable
 * configuration results and currency updates to the UI layer.
 *
 * @property app the application context used to access encrypted shared preferences
 * @property scope the [CoroutineScope] used for asynchronous operations such as network checks
 * @property httpClient the [HttpClient] instance used for making network requests to validate configuration
 */
class ConfigManager(
    private val app: Application,
    private val scope: CoroutineScope,
    private val httpClient: HttpClient,
) {

    /**
     * Constant tag for logging operations within this class.
     */
    private val TAG = ConfigManager::class.java.simpleName

    /**
     * Navigation destination reference to the configuration fragment.
     */
    val configDestination = ConfigFragmentDirections.actionGlobalConfigFragment()

    /**
     * The master key alias used for creating and managing [EncryptedSharedPreferences].
     */
    private val masterKeyAlias = MasterKeys.getOrCreate(AES256_GCM_SPEC)

    /**
     * Secure storage for configuration data, backed by Android’s encrypted shared preferences.
     */
    private val prefs = EncryptedSharedPreferences.create(
        PREF_NAME, masterKeyAlias, app, AES256_SIV, AES256_GCM
    )

    /**
     * Current active [Config] instance loaded from preferences.
     * Contains bank URL, username, and password credentials.
     */
    internal var config = Config(
        bankUrl = prefs.getString(PREF_KEY_BANK_URL, "")!!,
        username = prefs.getString(PREF_KEY_USERNAME, "")!!,
        password = prefs.getString(PREF_KEY_PASSWORD, "")!!
    )

    /**
     * Backing property for the currently active currency as a [LiveData].
     * Observers can react to currency changes when configuration is validated.
     */
    private val mCurrency = MutableLiveData<String>(
        prefs.getString(PREF_KEY_CURRENCY, null)
    )
    internal val currency: LiveData<String> = mCurrency

    /**
     * Backing property for the result of the configuration check operation.
     * Observers can track async outcomes such as success, offline, or error.
     */
    private val mConfigResult = MutableLiveData<ConfigResult?>()
    val configResult: LiveData<ConfigResult?> = mConfigResult

    /**
     * Checks whether the current configuration is complete.
     *
     * @return true if all required fields (bank URL, username, password) are non-empty.
     */
    fun hasConfig() = config.bankUrl.isNotEmpty()
            && config.username.isNotEmpty()
            && config.password.isNotEmpty()

    /**
     * Launches a coroutine to validate and persist the given [config].
     * Updates [configResult] asynchronously based on the outcome.
     *
     * @param config the configuration instance to validate and save
     *
     * @see ConfigResult for possible result values
     * @see VERSION_BANK for version compatibility checks
     */
    @UiThread
    fun checkAndSaveConfig(config: Config) = scope.launch {
        mConfigResult.value = null
        checkConfig(config).onError { failure ->
            val result = if (failure.isOffline(app)) {
                ConfigResult.Offline
            } else {
                ConfigResult.Error(failure.statusCode == Unauthorized, failure.msg)
            }
            mConfigResult.postValue(result)
        }.onSuccess { response ->
            val versionIncompatible =
                VERSION_BANK.getIncompatibleStringOrNull(app, response.version)
            val result = if (versionIncompatible != null) {
                ConfigResult.Error(false, versionIncompatible)
            } else {
                mCurrency.postValue(response.currency)
                prefs.edit { putString(PREF_KEY_CURRENCY, response.currency) }
                // Save config to encrypted storage
                saveConfig(config)
                ConfigResult.Success
            }
            mConfigResult.postValue(result)
        }
    }

    /**
     * Performs backend checks for the provided [config], ensuring both
     * connectivity and credential validity.
     *
     * @param config the configuration to test
     * @return a [Response] indicating success or failure with details
     */
    private suspend fun checkConfig(config: Config) = withContext(Dispatchers.IO) {
        val url = "${config.bankUrl}/config"
        Log.d(TAG, "Checking config: $url")
        val configResponse = response {
            httpClient.get(url).body<ConfigResponse>()
        }
        if (configResponse.isFailure) {
            configResponse
        } else {
            // Validate authentication credentials
            val balanceResponse = response {
                val authUrl = "${config.bankUrl}/accounts/${config.username}"
                Log.d(TAG, "Checking auth: $authUrl")
                httpClient.get(authUrl) {
                    header(Authorization, config.basicAuth)
                }
            }
            @Suppress("UNCHECKED_CAST")  // The type doesn't matter for failures
            if (balanceResponse.isFailure) balanceResponse as Response<ConfigResponse>
            else configResponse
        }
    }

    /**
     * Persists the given [config] into encrypted preferences.
     *
     * This operation commits synchronously for reliability during configuration changes.
     *
     * @param config the configuration to save securely
     */
    @WorkerThread
    @SuppressLint("ApplySharedPref")
    internal fun saveConfig(config: Config) {
        this.config = config
        prefs.edit(commit = true) {
            putString(PREF_KEY_BANK_URL, config.bankUrl)
                .putString(PREF_KEY_USERNAME, config.username)
                .putString(PREF_KEY_PASSWORD, config.password)
        }
    }

    /**
     * Locks the current configuration by clearing the stored password.
     * Useful when temporarily disabling access without removing all settings.
     */
    fun lock() {
        saveConfig(config.copy(password = ""))
    }

}

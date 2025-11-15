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

package net.taler.wallet

import android.app.Application
import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import net.taler.common.Amount
import net.taler.common.AmountParserException
import net.taler.common.Event
import net.taler.common.toEvent
import net.taler.wallet.accounts.AccountManager
import net.taler.wallet.backend.BackendManager
import net.taler.wallet.backend.NotificationPayload
import net.taler.wallet.backend.NotificationReceiver
import net.taler.wallet.backend.TalerErrorInfo
import net.taler.wallet.backend.VersionReceiver
import net.taler.wallet.backend.WalletBackendApi
import net.taler.wallet.backend.WalletCoreVersion
import net.taler.wallet.backend.WalletRunConfig
import net.taler.wallet.backend.WalletRunConfig.Testing
import net.taler.wallet.balances.BalanceManager
import net.taler.wallet.balances.ScopeInfo
import net.taler.wallet.deposit.DepositManager
import net.taler.wallet.events.ObservabilityEvent
import net.taler.wallet.exchanges.ExchangeManager
import net.taler.wallet.payment.PaymentManager
import net.taler.wallet.peer.PeerManager
import net.taler.wallet.refund.RefundManager
import net.taler.wallet.transactions.TransactionManager
import net.taler.wallet.transactions.TransactionStateFilter
import net.taler.wallet.withdraw.WithdrawManager
import androidx.core.net.toUri
import net.taler.wallet.settings.SettingsManager

const val TAG = "taler-wallet"

/** Maximum number of observability events to keep in memory. */
const val OBSERVABILITY_LIMIT = 100

/** Notification types that trigger transaction updates. */
private val transactionNotifications = listOf(
    "transaction-state-transition",
)

/** Notification types that generate observability events. */
private val observabilityNotifications = listOf(
    "task-observability-event",
    "request-observability-event",
)

/** URI actions that represent sending money. */
private val sendUriActions = listOf(
    "pay",
    "tip",
    "pay-pull",
    "pay-template",
)

/** URI actions that represent receiving money. */
private val receiveUriActions = listOf(
    "withdraw",
    "refund",
    "pay-push",
)

/**
 * Main application ViewModel that coordinates all wallet operations and manages
 * the wallet backend lifecycle.
 *
 * This ViewModel:
 * - Initializes and manages the wallet backend API
 * - Coordinates all feature-specific managers (payments, transactions, etc.)
 * - Handles notifications from wallet-core
 * - Manages authentication state
 * - Provides developer mode functionality
 *
 * @param app The application context.
 */
class MainViewModel(
    app: Application,
) : AndroidViewModel(app), VersionReceiver, NotificationReceiver {

    /**
     * LiveData indicating whether developer mode is enabled.
     * Defaults to the BuildConfig.DEBUG value.
     */
    private val mDevMode = MutableLiveData<Boolean>(BuildConfig.DEBUG)
    val devMode: LiveData<Boolean> = mDevMode

    /**
     * LiveData controlling the visibility of the global progress bar.
     * Set to true when long-running operations are in progress.
     */
    val showProgressBar = MutableLiveData<Boolean>()

    /**
     * The semantic version string of the wallet-core implementation.
     * Null until version information is received from the backend.
     */
    var walletVersion: String? = null
        private set

    /**
     * Git commit hash of the wallet-core implementation.
     * Null until version information is received from the backend.
     */
    var walletVersionHash: String? = null
        private set

    /**
     * Protocol version supported by exchanges.
     * Null until version information is received from the backend.
     */
    var exchangeVersion: String? = null
        private set

    /**
     * Protocol version supported by merchants.
     * Null until version information is received from the backend.
     */
    var merchantVersion: String? = null
        private set


    /**
     * Current wallet runtime configuration.
     * Thread-safe access via synchronized setter.
     */
    @set:Synchronized
    private var walletConfig = WalletRunConfig(
        testing = Testing(
            emitObservabilityEvents = true,
            devModeActive = devMode.value == true,
        ),
        logLevel = if (devMode.value == true) "TRACE" else "INFO",
    )

    /** API interface for communicating with the wallet-core backend. */
    private val api = WalletBackendApi(app, walletConfig, this, this)

    /** Manages network connectivity state and notifications. */
    val networkManager =
        NetworkManager(app.applicationContext)

    /** Manages exchange discovery, selection, and information retrieval. */
    val exchangeManager: ExchangeManager =
        ExchangeManager(api, viewModelScope)

    /** Manages wallet balance retrieval and scope-based balance filtering. */
    val balanceManager =
        BalanceManager(api, viewModelScope, exchangeManager)

    /** Manages payment operations including preparation and confirmation. */
    val paymentManager =
        PaymentManager(api, viewModelScope, exchangeManager)

    /** Manages transaction history, filtering, and individual transaction details. */
    val transactionManager: TransactionManager =
        TransactionManager(api, viewModelScope)

    /** Manages refund operations and refund status tracking. */
    val refundManager =
        RefundManager(api, viewModelScope)

    /** Manages withdrawal operations from banks and exchanges. */
    val withdrawManager =
        WithdrawManager(api, viewModelScope, exchangeManager, transactionManager)

    /** Manages peer-to-peer payment operations (push/pull). */
    val peerManager: PeerManager =
        PeerManager(api, exchangeManager, viewModelScope)

    /** Manages user preferences, database operations, and log exports. */
    val settingsManager: SettingsManager =
        SettingsManager(app.applicationContext, api, viewModelScope, balanceManager)

    /** Manages bank account configuration and linking. */
    val accountManager: AccountManager =
        AccountManager(api, viewModelScope)

    /** Manages deposit operations to bank accounts. */
    val depositManager: DepositManager =
        DepositManager(api, viewModelScope, balanceManager)

    /**
     * StateFlow indicating whether the wallet is currently authenticated.
     * When false, biometric authentication is required before accessing wallet features.
     */
    private val mAuthenticated = MutableStateFlow(false)
    val authenticated: StateFlow<Boolean> = mAuthenticated

    /**
     * LiveData event that triggers navigation to the transactions screen
     * for a specific scope.
     */
    private val mTransactionsEvent = MutableLiveData<Event<ScopeInfo>>()
    val transactionsEvent: LiveData<Event<ScopeInfo>> = mTransactionsEvent

    /**
     * StateFlow containing the recent observability events from wallet-core.
     * Limited to the most recent [OBSERVABILITY_LIMIT] events.
     */
    private val mObservabilityLog = MutableStateFlow<List<ObservabilityEvent>>(emptyList())
    val observabilityLog: StateFlow<List<ObservabilityEvent>> = mObservabilityLog

    /**
     * LiveData event that triggers the QR code scanner.
     * The boolean value is always true when triggered.
     */
    private val mScanCodeEvent = MutableLiveData<Event<Boolean>>()
    val scanCodeEvent: LiveData<Event<Boolean>> = mScanCodeEvent

    /**
     * Current context for QR code scanning, used to validate scanned URIs.
     * Thread-safe access via synchronized setter.
     */
    @set:Synchronized
    private var scanQrContext = ScanQrContext.Unknown

    /**
     * Starts the wallet-core backend service.
     * This must be called before any wallet operations can be performed.
     */
    fun startWallet() { api.startWallet() }

    /**
     * Stops the wallet-core backend service.
     * Should be called when the app is being destroyed to clean up resources.
     */
    fun stopWallet() { api.stopWallet() }

    /**
     * Callback invoked when version information is received from wallet-core.
     *
     * @param versionInfo Version information from the wallet backend.
     */
    override fun onVersionReceived(versionInfo: WalletCoreVersion) {
        walletVersion =  "36:2:8"           //old: versionInfo.implementationSemver
        walletVersionHash = versionInfo.implementationGitHash
        exchangeVersion = versionInfo.exchange
        merchantVersion = versionInfo.merchant
    }

    /**
     * Callback invoked when a notification is received from wallet-core.
     *
     * Handles various notification types including:
     * - Balance changes (triggers balance reload)
     * - Transaction state transitions (updates transaction lists)
     * - Observability events (adds to log)
     *
     * @param payload The notification payload from wallet-core.
     */
    override fun onNotificationReceived(payload: NotificationPayload) {
        if (payload.type == "waiting-for-retry") return // ignore ping)

        val str = BackendManager.json.encodeToString(payload)
        Log.i(TAG, "Received notification from wallet-core: $str")

        // Only update balances when we're told they changed
        if (payload.type == "balance-change")
            viewModelScope.launch(Dispatchers.Main) {
                balanceManager.loadBalances()
            }

        if (payload.type in observabilityNotifications && payload.event != null) {
            mObservabilityLog.getAndUpdate { logs ->
                logs.takeLast(OBSERVABILITY_LIMIT)
                    .toMutableList().apply {
                        add(payload.event)
                    }
            }
        }

        if (payload.type in transactionNotifications)
            viewModelScope.launch(Dispatchers.Main) {
                payload.transactionId?.let { id ->
                    // update currently selected transaction
                    transactionManager.updateTransactionIfSelected(id)
                    // update currently selected transaction list
                    if (payload.type == "transaction-state-transition") {
                        transactionManager.getTransactionById(id)?.let { tx ->
                            if (transactionManager.selectedScope.value in tx.scopes) {
                                transactionManager.loadTransactions()
                            }
                        }
                    }
                }
            }
    }

    /**
     * Locks the wallet, requiring biometric authentication to access.
     * Must be called on the UI thread.
     */
    @UiThread
    fun lockWallet() {
        mAuthenticated.value = false
    }

    /**
     * Unlocks the wallet after successful biometric authentication.
     * Must be called on the UI thread.
     */
    @UiThread
    fun unlockWallet() {
        mAuthenticated.value = true
    }

    /**
     * Navigates to the transaction list for the specified scope.
     * Only takes effect when MainFragment is shown.
     * Must be called on the UI thread.
     *
     * @param scopeInfo The scope to display transactions for.
     * @param stateFilter Optional filter to show only transactions in specific states.
     */
    @UiThread
    fun showTransactions(scopeInfo: ScopeInfo, stateFilter: TransactionStateFilter? = null) {
        Log.d(TAG, "selectedScope should change to $scopeInfo")
        transactionManager.selectScope(scopeInfo, stateFilter)
    }

    /**
     * Parses an amount string and validates it against the wallet's balance.
     * Must be called on the UI thread.
     *
     * @param amountText The numeric amount as a string.
     * @param currency The currency code (e.g., "KUDOS", "EUR").
     * @param incoming Whether this is an incoming amount (skips balance check).
     * @return Result indicating success, invalid format, or insufficient balance.
     */
    @UiThread
    fun createAmount(
        amountText: String,
        currency: String,
        incoming: Boolean = false): AmountResult {
        val amount = try {
            Amount.fromString(currency, amountText)
        } catch (e: AmountParserException) {
            return AmountResult.InvalidAmount
        }
        if (incoming || balanceManager.hasSufficientBalance(amount))
            return AmountResult.Success(amount)
        else
            return AmountResult.InsufficientBalance(amount)
    }

    /**
     * Resets all test withdrawal state and balance caches.
     * **DANGEROUS**: Only use in development/testing environments.
     * Must be called on the UI thread.
     */
    @UiThread
    fun dangerouslyReset() {
        withdrawManager.resetTestWithdrawal()
        balanceManager.resetBalances()
    }

    /**
     * Triggers the QR code scanner with an optional context for validation.
     * Must be called on the UI thread.
     *
     * @param context The context defining what type of URI is expected (send/receive/any).
     */
    @UiThread
    fun scanCode(context: ScanQrContext = ScanQrContext.Unknown) {
        scanQrContext = context
        mScanCodeEvent.value = true.toEvent()
    }

    /**
     * Returns the current QR code scanning context.
     *
     * @return The context set by the last call to [scanCode].
     */
    fun getScanQrContext() = scanQrContext

    /**
     * Validates whether a scanned URI matches the expected context.
     *
     * @param uri The Taler URI that was scanned.
     * @return True if the URI action matches the scan context, false otherwise.
     */
    fun checkScanQrContext(uri: String): Boolean {
        val parsed = uri.toUri()
        val action = parsed.host
        return when (scanQrContext) {
            ScanQrContext.Send -> action in sendUriActions
            ScanQrContext.Receive -> action in receiveUriActions
            else -> true
        }
    }

    /**
     * Enables or disables developer mode, which affects logging verbosity
     * and enables observability events.
     *
     * @param enabled Whether developer mode should be active.
     * @param onError Callback invoked if the configuration update fails.
     */
    fun setDevMode(enabled: Boolean, onError: (error: TalerErrorInfo) -> Unit) {
        mDevMode.postValue(enabled)
        viewModelScope.launch {
            val config = walletConfig.copy(
                testing = walletConfig.testing?.copy(
                    devModeActive = enabled,
                ) ?: Testing(
                    devModeActive = enabled,
                ),
                logLevel = if (enabled) "TRACE" else "INFO",
            )

            api.setWalletConfig(config)
                .onSuccess {
                    walletConfig = config
                }.onError(onError)
        }
    }

    /**
     * Notifies wallet-core about network connectivity changes.
     * This allows the backend to pause/resume network operations appropriately.
     *
     * @param isAvailable True if network is available, false otherwise.
     */
    fun hintNetworkAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            api.request<Unit>("hintNetworkAvailability") {
                put("isNetworkAvailable", isAvailable)
            }
        }
    }

    /**
     * Runs the built-in integration test suite against the demo environment.
     * Performs a full cycle of withdrawal, payment, and verification.
     *
     * @param onError Callback invoked if the test fails.
     */
    fun runIntegrationTest(onError: (error: TalerErrorInfo) -> Unit) {
        viewModelScope.launch {
            api.request<Unit>("runIntegrationTestV2") {
                put("amountToWithdraw", "KUDOS:42")
                put("amountToSpend", "KUDOS:23")
                put("corebankApiBaseUrl", "https://bank.demo.taler.net/")
                put("exchangeBaseUrl", "https://exchange.demo.taler.net/")
                put("merchantBaseUrl", "https://backend.demo.taler.net/instances/sandbox/")
                put("merchantAuthToken", "secret-token:sandbox")
            }.onError(onError)
        }
    }

    /**
     * Applies a developer experiment URI to modify wallet behavior for testing.
     * Only available in developer mode.
     *
     * @param uri The dev-experiment:// URI to apply.
     * @param onError Callback invoked if applying the experiment fails.
     */
    fun applyDevExperiment(uri: String, onError: (error: TalerErrorInfo) -> Unit) {
        viewModelScope.launch {
            api.request<Unit>("applyDevExperiment") {
                put("devExperimentUri", uri)
            }.onError(onError)
        }
    }
}

/**
 * Context for QR code scanning that defines what type of Taler URI is expected.
 */
enum class ScanQrContext {
    /** Expecting a URI for sending money (pay, tip, etc.). */
    Send,

    /** Expecting a URI for receiving money (withdraw, refund, etc.). */
    Receive,

    /** No specific context - accept any valid Taler URI. */
    Unknown,
}

/**
 * Result of attempting to create and validate an amount.
 */
sealed class AmountResult {
    /**
     * Amount was successfully parsed and validated.
     *
     * @property amount The validated amount.
     */
    data class Success(val amount: Amount) : AmountResult()

    /**
     * Amount was parsed but exceeds available balance.
     *
     * @property amount The parsed amount that exceeds balance.
     */
    data class InsufficientBalance(val amount: Amount) : AmountResult()

    /**
     * Amount string could not be parsed as a valid amount.
     */
    data object InvalidAmount : AmountResult()
}
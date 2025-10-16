package net.taler.wallet

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.annotation.UiThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.taler.common.liveData.Event
import net.taler.common.liveData.toEvent
import net.taler.database.data_models.Amount
import net.taler.database.data_models.AmountParserException
import net.taler.qtart.BuildConfig
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
import net.taler.wallet.settings.SettingsManager
import net.taler.wallet.transactions.TransactionManager
import net.taler.wallet.withdraw.WithdrawManager
import org.json.JSONObject
import androidx.core.net.toUri

const val TAG = "taler-wallet"
const val OBSERVABILITY_LIMIT = 100

private val transactionNotifications = listOf(
    "transaction-state-transition",
)

private val observabilityNotifications = listOf(
    "task-observability-event",
    "request-observability-event",
)

private val sendUriActions = listOf(
    "pay",
    "tip",
    "pay-pull",
    "pay-template",
)

private val receiveUriActions = listOf(
    "withdraw",
    "refund",
    "pay-push",
)

/**
 * Main ViewModel for the Taler wallet application.
 *
 * This ViewModel manages the core wallet state, coordinates between different managers
 * (balance, transaction, payment, etc.), and handles communication with the wallet backend.
 */
class MainViewModel(
    app: Application,
) : AndroidViewModel(app), VersionReceiver, NotificationReceiver {

    // TODO: need to fix gradle
    private val mDevMode = MutableLiveData(BuildConfig.DEBUG)
    val devMode: LiveData<Boolean> = mDevMode

    val showProgressBar = MutableLiveData<Boolean>()

    /** Wallet core implementation version (semver format) */
    var walletVersion: String? = null
        private set

    /** Git hash of the wallet core implementation */
    var walletVersionHash: String? = null
        private set

    /** Supported exchange protocol version */
    var exchangeVersion: String? = null
        private set

    /** Supported merchant protocol version */
    var merchantVersion: String? = null
        private set

    @set:Synchronized
    private var walletConfig = WalletRunConfig(
        testing = Testing(
            emitObservabilityEvents = true,
            devModeActive = devMode.value ?: false,
        )
    )

    private val api = WalletBackendApi(app, walletConfig, this, this)

    val networkManager = NetworkManager(app.applicationContext)
    val paymentManager = PaymentManager(api, viewModelScope)
    val transactionManager: TransactionManager = TransactionManager(api, viewModelScope)
    val refundManager = RefundManager(api, viewModelScope)
    val balanceManager = BalanceManager(api, viewModelScope)
    val exchangeManager: ExchangeManager = ExchangeManager(api, viewModelScope)
    val withdrawManager = WithdrawManager(api, viewModelScope, exchangeManager)
    val peerManager: PeerManager = PeerManager(api, exchangeManager, viewModelScope)
    val settingsManager: SettingsManager = SettingsManager(app.applicationContext, api, viewModelScope, balanceManager)
    val accountManager: AccountManager = AccountManager(api, viewModelScope)
    val depositManager: DepositManager = DepositManager(api, viewModelScope)

    private val mTransactionsEvent = MutableLiveData<Event<ScopeInfo>>()
    val transactionsEvent: LiveData<Event<ScopeInfo>> = mTransactionsEvent

    private val mObservabilityLog = MutableStateFlow<List<ObservabilityEvent>>(emptyList())
    val observabilityLog: StateFlow<List<ObservabilityEvent>> = mObservabilityLog

    private val mScanCodeEvent = MutableLiveData<Event<Boolean>>()
    val scanCodeEvent: LiveData<Event<Boolean>> = mScanCodeEvent

    @set:Synchronized
    private var scanQrContext = ScanQrContext.Unknown

    // ============================================================================
    // TEMPORARY HARDCODED USER PREFERENCES
    // ============================================================================
    // TODO: Replace with proper DataStore/Protobuf persistence
    // Currently storing preferences in memory only - will be lost on app restart
    // This is a temporary workaround to bypass protobuf compilation issues

    /**
     * TEMPORARY: In-memory storage for the selected scope.
     * Normally this would be persisted using DataStore with protobuf.
     * Will be lost when the app is closed or ViewModel is cleared.
     */
    private var selectedScope: ScopeInfo? = null

    /**
     * TEMPORARY: In-memory storage for whether the action button has been used.
     * Normally this would be persisted using DataStore with protobuf.
     * Will be lost when the app is closed or ViewModel is cleared.
     */
    private var actionButtonUsed = false
    // ============================================================================

    override fun onVersionReceived(versionInfo: WalletCoreVersion) {
        walletVersion =  "36:2:8"           //old: versionInfo.implementationSemver
        walletVersionHash = versionInfo.implementationGitHash
        exchangeVersion = "36:2:8"         // old: versionInfo.exchange
        merchantVersion = versionInfo.merchant
    }

    override fun onNotificationReceived(payload: NotificationPayload) {
        if (payload.type == "waiting-for-retry") return // ignore ping

        val str = BackendManager.json.encodeToString(payload)
        Log.i(TAG, "Received notification from wallet-core: $str")

        // Only update balances when we're told they changed
        if (payload.type == "balance-change") viewModelScope.launch(Dispatchers.Main) {
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

        if (payload.type in transactionNotifications) viewModelScope.launch(Dispatchers.Main) {
            // TODO notification API should give us a currency to update
            // update currently selected transaction
            payload.transactionId?.let { transactionManager.updateTransactionIfSelected(it) }
            // update currently selected transaction list
            transactionManager.loadTransactions()
        }
    }

    /**
     * Navigates to the given scope info's transaction list, when [MainFragment] is shown.
     *
     * @param scopeInfo The scope (currency/exchange/auditor) to show transactions for
     */
    @UiThread
    fun showTransactions(scopeInfo: ScopeInfo) {
        Log.d(TAG, "selectedScope should change to $scopeInfo")
        transactionManager.selectScope(scopeInfo)
    }

    /**
     * Creates and validates an Amount from the given text and currency.
     *
     * @param amountText The numeric amount as a string
     * @param currency The currency code (e.g., "EUR", "USD")
     * @param incoming If true, skips balance check (for receiving money)
     * @return AmountResult indicating success, invalid format, or insufficient balance
     */
    @UiThread
    fun createAmount(amountText: String, currency: String, incoming: Boolean = false): AmountResult {
        val amount = try {
            Amount.fromString(currency, amountText)
        } catch (e: AmountParserException) {
            return AmountResult.InvalidAmount
        }
        if (incoming || balanceManager.hasSufficientBalance(amount)) return AmountResult.Success(amount)
        return AmountResult.InsufficientBalance(amount)
    }

    /**
     * Resets test data. Only for development/testing purposes.
     * WARNING: This will clear wallet state.
     */
    @UiThread
    fun dangerouslyReset() {
        withdrawManager.resetTestWithdrawal()
        balanceManager.resetBalances()
    }

    /** Starts the built-in network tunnel (for testing) */
    fun startTunnel() {
        viewModelScope.launch {
            api.sendRequest("startTunnel")
        }
    }

    /** Stops the built-in network tunnel (for testing) */
    fun stopTunnel() {
        viewModelScope.launch {
            api.sendRequest("stopTunnel")
        }
    }

    /**
     * Sends a response through the network tunnel.
     * @param resp JSON response string to send
     */
    fun tunnelResponse(resp: String) {
        viewModelScope.launch {
            api.sendRequest("tunnelResponse", JSONObject(resp))
        }
    }

    /**
     * Triggers the QR code scanner with optional context about what's being scanned.
     *
     * @param context The context for scanning (Send/Receive/Unknown)
     */
    @UiThread
    fun scanCode(context: ScanQrContext = ScanQrContext.Unknown) {
        scanQrContext = context
        mScanCodeEvent.value = true.toEvent()
    }

    /** Returns the current QR scanning context */
    fun getScanQrContext() = scanQrContext

    /**
     * Checks if a scanned URI matches the expected context.
     *
     * @param uri The scanned Taler URI
     * @return true if the URI matches the scanning context, false otherwise
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
     * Enables or disables developer mode.
     *
     * @param enabled Whether to enable dev mode
     * @param onError Callback invoked if the config change fails
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
            )

            api.setWalletConfig(config)
                .onSuccess {
                    walletConfig = config
                }.onError(onError)
        }
    }

    /**
     * Hints to the wallet core about network availability changes.
     *
     * @param isAvailable Whether the network is currently available
     */
    fun hintNetworkAvailability(isAvailable: Boolean) {
        viewModelScope.launch {
            api.request<Unit>("hintNetworkAvailability") {
                put("isNetworkAvailable", isAvailable)
            }
        }
    }

    /**
     * Runs the built-in integration test suite against demo services.
     * Only available in development builds.
     *
     * @param onError Callback invoked if the test fails
     */
    fun runIntegrationTest(onError: (error: TalerErrorInfo) -> Unit) {
        viewModelScope.launch {
            api.request<Unit>("runIntegrationTestV2") {
                put("amountToWithdraw", "KUDOS:42")
                put("amountToSpend", "KUDOS:23")
                put("corebankApiBaseUrl", "https://bank.demo.taler.net/")
                put("exchangeBaseUrl", "https://exchange.demo.taler.net/")
                put("merchantBaseUrl", "https://backend.demo.taler.net/")
                put("merchantAuthToken", "secret-token:sandbox")
            }.onError(onError)
        }
    }

    /**
     * Applies a developer experiment URI (for testing experimental features).
     *
     * @param uri The experiment URI to apply
     * @param onError Callback invoked if applying the experiment fails
     */
    fun applyDevExperiment(uri: String, onError: (error: TalerErrorInfo) -> Unit) {
        viewModelScope.launch {
            api.request<Unit>("applyDevExperiment") {
                put("devExperimentUri", uri)
            }.onError(onError)
        }
    }

    /**
     * TEMPORARY HARDCODED VERSION
     *
     * Gets the currently selected scope (currency/exchange/auditor filter).
     *
     * **WARNING**: This is stored in memory only and will be lost on app restart!
     *
     * TODO: Replace with proper DataStore implementation once protobuf issues are resolved.
     * The proper implementation should read from:
     * `c.userPreferencesDataStore.data.map { prefs -> ... }`
     *
     * @param c Context (currently unused, kept for API compatibility)
     * @return Flow emitting the selected ScopeInfo, or null if none selected
     */
    fun getSelectedScope(c: Context): Flow<ScopeInfo?> {
        return flowOf(selectedScope)
    }

    /**
     * TEMPORARY HARDCODED VERSION
     *
     * Saves the selected scope (currency/exchange/auditor filter).
     *
     * **WARNING**: This is stored in memory only and will be lost on app restart!
     *
     * TODO: Replace with proper DataStore implementation once protobuf issues are resolved.
     * The proper implementation should write to:
     * `c.userPreferencesDataStore.updateData { ... }`
     *
     * @param c Context (currently unused, kept for API compatibility)
     * @param scopeInfo The scope to save, or null to clear the selection
     */
    fun saveSelectedScope(c: Context, scopeInfo: ScopeInfo?) = viewModelScope.launch {
        selectedScope = scopeInfo
    }

    /**
     * TEMPORARY HARDCODED VERSION
     *
     * Gets whether the action button has been used (for UI onboarding hints).
     *
     * **WARNING**: This is stored in memory only and will be lost on app restart!
     *
     * TODO: Replace with proper DataStore implementation once protobuf issues are resolved.
     * The proper implementation should read from:
     * `c.userPreferencesDataStore.data.map { prefs -> ... }`
     *
     * @param c Context (currently unused, kept for API compatibility)
     * @return Flow emitting true if the action button was used, false otherwise
     */
    fun getActionButtonUsed(c: Context): Flow<Boolean> {
        return flowOf(actionButtonUsed)
    }

    /**
     * TEMPORARY HARDCODED VERSION
     *
     * Marks the action button as used (dismisses onboarding hints).
     *
     * **WARNING**: This is stored in memory only and will be lost on app restart!
     *
     * TODO: Replace with proper DataStore implementation once protobuf issues are resolved.
     * The proper implementation should write to:
     * `c.userPreferencesDataStore.updateData { ... }`
     *
     * @param c Context (currently unused, kept for API compatibility)
     */
    fun saveActionButtonUsed(c: Context) = viewModelScope.launch {
        actionButtonUsed = true
    }
}

/**
 * Context for QR code scanning to validate scanned content matches user intent.
 */
enum class ScanQrContext {
    /** Scanning to send money (pay, tip, etc.) */
    Send,

    /** Scanning to receive money (withdraw, refund, etc.) */
    Receive,

    /** No specific context - accept any valid Taler URI */
    Unknown,
}

/**
 * Result of amount creation and validation.
 */
sealed class AmountResult {
    /** Amount was successfully created and wallet has sufficient balance */
    data class Success(val amount: Amount) : AmountResult()

    /** Amount was created but wallet doesn't have sufficient balance */
    data class InsufficientBalance(val amount: Amount) : AmountResult()

    /** The amount string couldn't be parsed (invalid format) */
    data object InvalidAmount : AmountResult()
}

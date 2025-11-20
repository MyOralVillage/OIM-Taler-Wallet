/**
 * ## SendPeerViewModel
 *
 * ViewModel responsible for handling **peer-to-peer “send” transactions**
 * in the OIM flow. Acts as a thin abstraction layer over the wallet-core
 * peer APIs exposed through [WalletBackendApi] and [PeerManager].
 *
 * ### Responsibilities
 * - Starts a *peer-push debit* transaction via [PeerManager.initiatePeerPushDebit].
 * - Observes [PeerManager.pushState] until an [OutgoingResponse] is received.
 * - Queries wallet-core through raw API calls to retrieve the generated
 *   `talerUri` once the transaction is created.
 * - Exposes [createdTalerUri] as a [StateFlow] for composables such as
 *   [QrScreen] to display.
 * - Provides [reset] to clear state and cancel the current peer push session.
 *
 * This class mirrors the internal flow used by the main wallet to support
 * preview or standalone “Send OIM” use cases.
 *
 * @property api Low-level backend interface to the wallet-core service.
 * @property exchangeManager Exchange manager used to resolve base URLs and scopes.
 *
 * @see net.taler.wallet.peer.PeerManager
 * @see net.taler.wallet.peer.OutgoingResponse
 * @see net.taler.wallet.backend.WalletBackendApi
 */

package net.taler.wallet.oim.send.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import net.taler.database.data_models.Amount
import net.taler.wallet.backend.WalletBackendApi
import net.taler.wallet.backend.WalletResponse
import net.taler.wallet.exchanges.ExchangeManager
import net.taler.wallet.peer.OutgoingResponse
import net.taler.wallet.peer.PeerManager

/**
 * ViewModel responsible for managing peer-to-peer push payments using wallet-core (Taler) logic.
 *
 * This ViewModel offers the following capabilities:
 * - Creating a peer-push transaction for a given amount, summary, and expiry window.
 * - Tracking the creation flow until a complete `talerUri` is available for sharing.
 * - Resetting the state of the current push operation.
 *
 * @property app The [Application] context for use with [AndroidViewModel].
 * @property api Backend API facade for wallet-core communication (e.g., RPC calls).
 * @property exchangeManager Manager for exchange selection and currency operations.
 */
class SendPeerViewModel(
    app: Application,
    private val api: WalletBackendApi,
    private val exchangeManager: ExchangeManager,
) : AndroidViewModel(app) {

    /**
     * Manager responsible for handling peer push operations.
     * This instance is stateful and tied to the [viewModelScope].
     */
    val peerManager = PeerManager(
        api = api,
        exchangeManager = exchangeManager,
        scope = viewModelScope
    )

    private val _createdTalerUri = MutableStateFlow<String?>(null)

    /**
     * Public read-only view of the created Taler URI.
     * Becomes non-null when a push operation successfully returns a URI.
     */
    val createdTalerUri: StateFlow<String?> = _createdTalerUri

    /**
     * Initiates a peer push payment and resolves the resulting `talerUri`.
     *
     * This function is tailored to be consumed directly by an `OutgoingPushComposable`.
     * Steps:
     * 1. Request wallet-core to initiate the push payment.
     * 2. Collect the push state until a transaction ID becomes available.
     * 3. Query backend APIs with the transaction ID to fetch the Taler URI.
     *
     * @param amount The [Amount] object representing how much to send.
     * @param summary Human-readable description of the payment (used as subject).
     * @param hours Expiration for the payment, specified in hours.
     */
    fun onSend(amount: Amount, summary: String, hours: Long) {
        viewModelScope.launch {
            // 1) Ask wallet-core to create a peer-push payment
            peerManager.initiatePeerPushDebit(amount, summary, hours)

            // 2) Wait until we get the transaction ID back
            val txId = peerManager.pushState
                .filterIsInstance<OutgoingResponse>()
                .first()
                .transactionId

            // 3) Query the backend for the transaction and extract talerUri
            _createdTalerUri.value = fetchPeerPushTxUri(txId)
        }
    }

    /**
     * Clears any active state, including peer-push progress and generated URI.
     * Can be used after the operation has been completed or discarded.
     */
    fun reset() {
        peerManager.resetPushPayment()
        _createdTalerUri.value = null
    }

    /**
     * Attempts to retrieve the `talerUri` for a peer push transaction using
     * a multi-step fallback strategy:
     *
     * a. Try `getTransactionById`
     * b. Try `exportTransaction`
     * c. Fetch all transactions and filter by ID
     *
     * Returns null if no URI could be resolved.
     *
     * @param transactionId The unique ID for a peer push transaction.
     * @return A Taler URI as a [String] for sharing, or null if unavailable.
     */
    private suspend fun fetchPeerPushTxUri(transactionId: String): String? {
        // a) getTransactionById
        when (val r =
            api.rawRequest("getTransactionById") { put("transactionId", transactionId) }
        ) {
            is WalletResponse.Success -> {
                (r.result["transaction"] as? JsonObject)?.let { tx ->
                    tx["talerUri"]?.jsonPrimitive?.content?.let { return it }
                }
            }
            is WalletResponse.Error -> Unit
        }
        // b) exportTransaction
        when (val r=
            api.rawRequest("exportTransaction") {put("transactionId", transactionId) }
        ) {
            is WalletResponse.Success -> {
                r.result["talerUri"]?.jsonPrimitive?.content?.let { return it }
            }
            is WalletResponse.Error -> Unit
        }
        // c) list all and pick by id (fallback)
        when (val r = api.rawRequest("getTransactions")) {
            is WalletResponse.Success -> {
                val arr = r.result["transactions"]?.jsonArray ?: return null
                val tx = arr.firstOrNull {
                    (it as? JsonObject)
                        ?.get("transactionId")
                        ?.jsonPrimitive?.content == transactionId
                } as? JsonObject
                tx?.get("talerUri")?.jsonPrimitive?.content?.let { return it }
            }
            is WalletResponse.Error -> Unit
        }
        return null
    }
}

/**
 * Factory for creating a [SendPeerViewModel] with custom arguments.
 *
 * Use this when you need to pass in dependencies not supported by the default
 * `ViewModelProvider`, such as an API or manager implementation.
 */
class SendPeerViewModelFactory(
    private val app: Application,
    private val api: WalletBackendApi,
    private val exchangeManager: ExchangeManager,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SendPeerViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return SendPeerViewModel(app, api, exchangeManager) as T
    }
}

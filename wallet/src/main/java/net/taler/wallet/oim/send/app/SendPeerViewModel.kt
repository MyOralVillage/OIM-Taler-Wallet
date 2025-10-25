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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
<<<<<<< HEAD
=======
import kotlinx.serialization.json.jsonObject
>>>>>>> f512e18 (added backend integration and db transaction update)
import kotlinx.serialization.json.jsonPrimitive
import net.taler.database.data_models.Amount
import net.taler.wallet.backend.WalletBackendApi
import net.taler.wallet.backend.WalletResponse
import net.taler.wallet.exchanges.ExchangeManager
import net.taler.wallet.peer.OutgoingResponse
import net.taler.wallet.peer.PeerManager

class SendPeerViewModel(
    app: Application,
    private val api: WalletBackendApi,
    private val exchangeManager: ExchangeManager,
) : AndroidViewModel(app) {

    val peerManager = PeerManager(
        api = api,
        exchangeManager = exchangeManager,
        scope = viewModelScope
    )

    private val _createdTalerUri = MutableStateFlow<String?>(null)
    val createdTalerUri: StateFlow<String?> = _createdTalerUri

    /** EXACT signature consumed by OutgoingPushComposable */
    fun onSend(amount: Amount, summary: String, hours: Long) {
        viewModelScope.launch {
            // 1) Ask wallet-core to create a peer-push payment
            peerManager.initiatePeerPushDebit(amount, summary, hours)

            // 2) Wait until we get the transaction id back
            val txId = peerManager.pushState
                .filterIsInstance<OutgoingResponse>()
                .first()
                .transactionId

            // 3) Query the backend for the transaction and extract talerUri
            _createdTalerUri.value = fetchPeerPushTxUri(txId)
        }
    }

    fun reset() {
        peerManager.resetPushPayment()
        _createdTalerUri.value = null
    }

    private suspend fun fetchPeerPushTxUri(transactionId: String): String? {
        // a) getTransactionById
        when (val r = api.rawRequest("getTransactionById") { put("transactionId", transactionId) }) {
            is WalletResponse.Success -> {
                (r.result["transaction"] as? JsonObject)?.let { tx ->
                    tx["talerUri"]?.jsonPrimitive?.content?.let { return it }
                }
            }
            is WalletResponse.Error -> Unit
        }
        // b) exportTransaction
        when (val r = api.rawRequest("exportTransaction") { put("transactionId", transactionId) }) {
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

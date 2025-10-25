package net.taler.wallet.oim.send.app

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.taler.wallet.backend.WalletBackendApi
import net.taler.wallet.exchanges.ExchangeManager

class SendPeerViewModelFactory(
    private val app: Application,
    private val api: WalletBackendApi,
    private val exchangeManager: ExchangeManager,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(SendPeerViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return SendPeerViewModel(app, api, exchangeManager) as T
    }
}

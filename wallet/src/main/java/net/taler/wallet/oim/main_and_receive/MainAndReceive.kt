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

package net.taler.wallet.oim.main_and_receive

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.zxing.client.android.Intents.Scan.MIXED_SCAN
import com.google.zxing.client.android.Intents.Scan.SCAN_TYPE
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanOptions.QR_CODE
import net.taler.database.TranxHistory
import net.taler.database.data_models.Amount
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.Timestamp
import net.taler.wallet.BuildConfig
import net.taler.wallet.MainViewModel
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.balances.ScopeInfo
import net.taler.wallet.compose.collectAsStateLifecycleAware
import net.taler.wallet.oim.main_and_receive.screens.OimClosedChestScreen
import net.taler.wallet.oim.main_and_receive.screens.OimMainScreen
import net.taler.wallet.oim.main_and_receive.screens.OimReceiveScreen
import net.taler.wallet.peer.IncomingAccepted
import net.taler.wallet.peer.IncomingAccepting
import net.taler.wallet.peer.IncomingError
import net.taler.wallet.peer.IncomingTerms
import net.taler.wallet.peer.IncomingTosReview

private const val TAG = "OIMCompose"

@Stable
data class OimReceiveFlowState(
    val launchReceiveScan: () -> Unit,
    val dialogTerms: IncomingTerms?,
    val isScanningOrLoading: Boolean,
    val confirmTerms: (IncomingTerms) -> Unit,
    val rejectTerms: (IncomingTerms) -> Unit,
)

@Composable
internal fun rememberOimReceiveFlowState(
    model: MainViewModel,
    onReviewTos: ((String) -> Unit)? = null,
    showDevToasts: Boolean = model.devMode.value == true,
): OimReceiveFlowState {
    val context = LocalContext.current
    val peerManager = model.peerManager

    var isScanningOrLoading by remember { mutableStateOf(false) }

    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result == null || result.contents == null) {
            isScanningOrLoading = false
            return@rememberLauncherForActivityResult
        }
        val scannedUri = result.contents
        Log.d(TAG, "Scanned URI: $scannedUri")
        peerManager.preparePeerPushCredit(scannedUri)
    }

    var lastTerms: IncomingTerms? by remember { mutableStateOf(null) }
    val paymentState by peerManager.incomingPushState.collectAsStateLifecycleAware()

    val exchanges by model.exchangeManager.exchanges.observeAsState(emptyList())
    LaunchedEffect(exchanges) {
        model.peerManager.refreshPeerPushCreditTos(exchanges)
    }

    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is IncomingTerms -> if (state !is IncomingAccepting) {
                lastTerms = state
                if (showDevToasts) {
                    Toast.makeText(
                        context, "Terms ready: ${state.amountEffective}",
                        Toast.LENGTH_SHORT).show()
                }
            }
            is IncomingTosReview -> {
                Toast.makeText(context, "Exchange ToS need review", Toast.LENGTH_SHORT).show()
            }
            is IncomingAccepted -> {
                isScanningOrLoading = false
                Toast.makeText(
                    context,
                    "Payment received successfully!",
                    Toast.LENGTH_LONG
                ).show()
                lastTerms?.let { terms ->
                    val appCtx = context.applicationContext
                    runCatching {
                        if (BuildConfig.DEBUG)
                            TranxHistory.initTest(appCtx)
                        else TranxHistory.initTest(appCtx)
                    }

                    runCatching {
                        TranxHistory.newTransaction(
                            tid = "RECEIVED_${System.currentTimeMillis()}",
                            purp = null,
                            amt = terms.amountEffective,
                            dir = FilterableDirection.INCOMING,
                            tms = Timestamp.now(),
                        )
                    }.onFailure { e ->
                        Log.e(TAG, "Local DB log failed (will retry after init): ${e.message}")
                        runCatching {
                            if (BuildConfig.DEBUG)
                                TranxHistory.initTest(appCtx)
                            else TranxHistory.initTest(appCtx)
                            TranxHistory.newTransaction(
                                tid = "RECEIVED_${System.currentTimeMillis()}",
                                purp = null,
                                amt = terms.amountEffective,
                                dir = FilterableDirection.INCOMING,
                                tms = Timestamp.now(),
                            )
                        }.onFailure { ex ->
                            Log.e(TAG, "Local DB log failed after init: ${ex.message}", ex)
                        }
                    }
                }
            }
            is IncomingError -> {
                isScanningOrLoading = false
                val msg = if (showDevToasts) state.info.toString() else state.info.userFacingMsg
                Toast.makeText(context, "Payment error: $msg", Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    val dialogTerms = when (val state = paymentState) {
        is IncomingTerms -> if (state is IncomingAccepting) null else state
        else -> null
    }

    val launchReceiveScan = remember(barcodeLauncher) {
        {
            val scanOptions = ScanOptions().apply {
                setPrompt("")
                setBeepEnabled(false)
                setOrientationLocked(false)
                setDesiredBarcodeFormats(QR_CODE)
                addExtra(SCAN_TYPE, MIXED_SCAN)
            }
            isScanningOrLoading = true
            barcodeLauncher.launch(scanOptions)
        }
    }

    val confirmTerms = remember(onReviewTos, peerManager) {
        { terms: IncomingTerms ->
            if (terms is IncomingTosReview) {
                onReviewTos?.invoke(terms.exchangeBaseUrl) ?: Unit
            } else {
                peerManager.confirmPeerPushCredit(terms)
                isScanningOrLoading = false
            }
        }
    }

    val rejectTerms = remember(peerManager, context) {
        { terms: IncomingTerms ->
            Toast.makeText(context, "Payment rejected", Toast.LENGTH_SHORT).show()
            peerManager.rejectPeerPushCredit(terms)
            isScanningOrLoading = false
        }
    }

    return OimReceiveFlowState(
        launchReceiveScan = launchReceiveScan,
        dialogTerms = dialogTerms,
        isScanningOrLoading = isScanningOrLoading,
        confirmTerms = confirmTerms,
        rejectTerms = rejectTerms,
    )
}

/**
 * Entry point for rendering the OIM home experience inside the main Compose navigator.
 *
 * @param model shared [MainViewModel] exposing peer, balance, and exchange managers.
 * @param onNavigateToChest callback invoked when the user opens their chest.
 * @param onBackToTaler callback that returns the user to the classic wallet experience.
 * @param onReviewTos optional handler used when an exchange requires terms review.
 * @param modifier host modifier for positioning within the parent layout.
 * @param showDevToasts toggles additional debugging toasts for development builds.
 */
@Composable
fun OIMHomeScreen(
    model: MainViewModel,
    onNavigateToChest: () -> Unit,
    onBackToTaler: () -> Unit,
    onReviewTos: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    showDevToasts: Boolean = model.devMode.value == true,
) {
    val receiveFlow = rememberOimReceiveFlowState(
        model = model,
        onReviewTos = onReviewTos,
        showDevToasts = showDevToasts,
    )

    // UI with dialog overlay when terms are available (not accepting)
    Box(modifier = modifier.fillMaxSize()) {
        OimClosedChestScreen(
            modifier = Modifier
                .fillMaxSize(),
            onScanQrClick = receiveFlow.launchReceiveScan,
            onChestClick = onNavigateToChest,
            onBackToTalerClick = onBackToTaler,
        )
        // === State: Account and Balance ===
        val balanceState by model.balanceManager.state.observeAsState(BalanceState.None)
        val selectedScope by model.transactionManager.selectedScope.collectAsStateWithLifecycle(
            initialValue = null
        )

        /**
         * Active scope automatically resolves to the current selection if present;
         * otherwise falls back to the first account with KUDOS or TESTKUDOS.
         */
        val activeScope: ScopeInfo? = remember(balanceState, selectedScope) {
            selectedScope ?: (balanceState as? BalanceState.Success)?.let { bs ->
                bs.balances.firstOrNull { it.currency.equals("KUDOS", true) }?.scopeInfo
                    ?: bs.balances.firstOrNull { it.currency.equals("TESTKUDOS", true) }?.scopeInfo
            }
        }

        // Amount entered by the user, defaulting to 0 in the active currency.
        var amount by remember(activeScope) {
            mutableStateOf(Amount.fromString(activeScope?.currency ?: "KUDOS", "0"))
        }

        // Current balance formatted for display.
        val balanceLabel: Amount = remember(balanceState, activeScope) {
            val success = balanceState as? BalanceState.Success
            val entry = success?.balances?.firstOrNull { it.scopeInfo == activeScope }
            Amount.fromString(
                activeScope?.currency ?: "KUDOS",
                entry?.available?.toString(showSymbol = false) ?: "0"
            )
        }
        val terms = receiveFlow.dialogTerms
        if (receiveFlow.isScanningOrLoading || terms != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black), // Opaque background for full screen feel
                contentAlignment = Alignment.Center,
            ) {
                OimReceiveScreen(
                    terms = terms,
                    onAccept = { terms?.let { receiveFlow.confirmTerms(it) } },
                    onReject = { terms?.let { receiveFlow.rejectTerms(it) } },
                    balance = balanceLabel
                )
            }
        }
    }
}

/**
 * Entry point for the OIM chest screen that wires the view model state to the
 * stateless [OimClosedChestScreen].
 *
 * @param model shared [MainViewModel] providing balances and withdraw actions.
 * @param onBackClick callback for the central chest button.
 * @param onSendClick callback for the send shortcut.
 * @param onRequestClick callback for the receive shortcut.
 * @param onTransactionHistoryClick opens the transaction history view.
 * @param onWithdrawTestKudosClick optional dev helper for minting test kudos.
 * @param modifier root modifier supplied by the host.
 */
@Composable
fun OIMChestScreen(
    model: MainViewModel,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onRequestClick: () -> Unit,
    onTransactionHistoryClick: () -> Unit,
    onWithdrawTestKudosClick: () -> Unit = { model.withdrawManager.withdrawTestBalance() },
    modifier: Modifier = Modifier,
) {
    MaterialTheme {
        val balanceState by model.balanceManager.state.observeAsState(BalanceState.None)
        OimMainScreen(
            modifier = modifier,
            onBackClick = onBackClick,
            onSendClick = onSendClick,
            onRequestClick = onRequestClick,
            onTransactionHistoryClick = onTransactionHistoryClick,
            onWithdrawTestKudosClick = onWithdrawTestKudosClick,
            balanceState = balanceState
        )
    }
}

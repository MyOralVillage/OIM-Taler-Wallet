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

package net.taler.wallet.oim.main

import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.zxing.client.android.Intents.Scan.MIXED_SCAN
import com.google.zxing.client.android.Intents.Scan.SCAN_TYPE
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanOptions.QR_CODE
import net.taler.database.TranxHistory
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.Timestamp
import net.taler.wallet.MainViewModel
import net.taler.wallet.balances.BalanceState
//import net.taler.wallet.oim.receive.ui_compose.OIMPaymentDialog
import net.taler.wallet.peer.IncomingAccepted
import net.taler.wallet.peer.IncomingAccepting
import net.taler.wallet.peer.IncomingTerms
import net.taler.wallet.systemBarsPaddingBottom
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import net.taler.wallet.compose.collectAsStateLifecycleAware
import net.taler.wallet.BuildConfig
import net.taler.wallet.peer.IncomingError
import net.taler.wallet.peer.IncomingTosReview

private const val TAG = "OIMCompose"

/**
 * OIM Home screen rendered as a composable inside the existing Compose flow.
 * Preserves previous OIM features: QR scan, payment dialog overlay, and navigation to chest.
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
    val context = LocalContext.current
    val peerManager = model.peerManager

    // Scanner launcher (keeps OIM-specific scan path separate from MainActivity's global scanner)
    val barcodeLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        if (result == null || result.contents == null) return@rememberLauncherForActivityResult
        val scannedUri = result.contents
        Log.d(TAG, "Scanned URI: $scannedUri")
        // Delegate validation to wallet-core via PeerManager; it will emit IncomingError/TosReview/Terms
        peerManager.preparePeerPushCredit(scannedUri)
    }

    // Local copy of last terms to log to DB when accepted (replicates activity behavior)
    var lastTerms: IncomingTerms? by remember { mutableStateOf(null) }
    val paymentState by peerManager.incomingPushState.collectAsStateLifecycleAware()

    // Observe exchanges to refresh ToS-dependent states after user accepts ToS
    val exchanges by model.exchangeManager.exchanges.observeAsState(emptyList())
    LaunchedEffect(exchanges) {
        model.peerManager.refreshPeerPushCreditTos(exchanges)
    }

    // Side-effects on payment state changes (toast/log + optional DB log)
    LaunchedEffect(paymentState) {
        when (val state = paymentState) {
            is IncomingTerms -> if (state !is IncomingAccepting) {
                lastTerms = state
                if (showDevToasts) {
                    Toast.makeText(context, "Terms ready: ${state.amountEffective}", Toast.LENGTH_SHORT).show()
                }
            }
            is IncomingTosReview -> {
                // Keep UI responsive: show toast, overlay will offer Accept → ToS
                Toast.makeText(context, "Exchange ToS need review", Toast.LENGTH_SHORT).show()
            }
            is IncomingAccepted -> {
                Toast.makeText(context, "Payment received successfully!", Toast.LENGTH_LONG).show()
                // Ensure local DB is initialized, then log like normal
                lastTerms?.let { terms ->
                    val appCtx = context.applicationContext
                    // For now, initialize test DB in both debug and release (provider's guidance)
                    // TODO: switch release branch to TranxHistory.init(appCtx) for production
                    runCatching {
                        if (BuildConfig.DEBUG) TranxHistory.initTest(appCtx) else TranxHistory.initTest(appCtx)
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
                        // If logging failed due to init race, try once more after forcing init
                        Log.e(TAG, "Local DB log failed (will retry after init): ${e.message}")
                        runCatching {
                            if (BuildConfig.DEBUG) TranxHistory.initTest(appCtx) else TranxHistory.initTest(appCtx)
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
                val msg = if (showDevToasts) state.info.toString() else state.info.userFacingMsg
                Toast.makeText(context, "Payment error: $msg", Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    // UI with dialog overlay when terms are available (not accepting)
    Box(modifier = modifier.fillMaxSize()) {
        OIMHomeScreenContent(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPaddingBottom(),
            onScanQrClick = {
                val scanOptions = ScanOptions().apply {
                    setPrompt("")
                    setBeepEnabled(true)
                    setOrientationLocked(false)
                    setDesiredBarcodeFormats(QR_CODE)
                    addExtra(SCAN_TYPE, MIXED_SCAN)
                }
                barcodeLauncher.launch(scanOptions)
            },
            onChestClick = onNavigateToChest,
            onBackToTalerClick = onBackToTaler,
        )

        val state = paymentState
//        if (state is IncomingTerms && state !is IncomingAccepting) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f)),
//                contentAlignment = Alignment.Center,
//            ) {
//                OIMPaymentDialog(
//                    terms = state,
//                    onAccept = {
//                        if (state is net.taler.wallet.peer.IncomingTosReview) {
//                            onReviewTos?.invoke(state.exchangeBaseUrl)
//                        } else {
//                            peerManager.confirmPeerPushCredit(state)
//                        }
//                    },
//                    onReject = {
//                        Toast.makeText(context, "Payment rejected", Toast.LENGTH_SHORT).show()
//                        // (Intentional: replicate previous behavior without explicit reset)
//                    },
//                )
//            }
//        }
    }
}

/**
 * OIM Chest screen as a composable.
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
        OIMChestScreenContent(
            modifier = modifier,
            onBackClick = onBackClick,
            onSendClick = onSendClick,
            onRequestClick = onRequestClick,
            onTransactionHistoryClick = onTransactionHistoryClick,
            onWithdrawTestKudosClick = onWithdrawTestKudosClick,
            balanceState = balanceState,
        )
    }
}

// --- Helpers ---

private fun validateIncomingPushUri(uri: String): Boolean {
    return try {
        val normalized = uri.trim().lowercase()
        if (normalized.startsWith("payto://")) return false
        val schemeLen = when {
            normalized.startsWith("taler://") -> "taler://".length
            normalized.startsWith("ext+taler://") -> "ext+taler://".length
            normalized.startsWith("taler+http://") -> return false
            else -> return false
        }
        val action = normalized.substring(schemeLen)
        action.startsWith("pay-push/")
    } catch (e: Exception) {
        false
    }
}

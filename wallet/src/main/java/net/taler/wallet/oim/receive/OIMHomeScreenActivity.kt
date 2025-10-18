/*
 * This file is part of GNU Taler
 * (C) 2024 Taler Systems S.A.
 *
 * GNU Taler is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * GNU Taler is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.taler.wallet.OIM

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import net.taler.wallet.BuildConfig
import net.taler.wallet.MainViewModel
import net.taler.wallet.OIM.UICompose.OIMPaymentDialog
import net.taler.wallet.R
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.peer.*
import net.taler.wallet.systemBarsPaddingBottom
import net.taler.wallet.peer.IncomingAccepting

/**
 * Activity that hosts the OIM Home Screen with integrated QR scanning and payment confirmation.
 * 
 * This activity:
 * - Displays the OIM home screen UI
 * - Launches OIMQrScannerActivity when QR scan is requested
 * - Handles the result from QR scanning for incoming push payments
 * - Shows payment confirmation dialog on the home screen
 * - Processes accept/reject payment actions
 */
class OIMHomeScreenActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "OIMHomeScreenActivity"
    }
    
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var oimPaymentManager: OIMPaymentManager
    
    // Activity result launcher for QR scanning
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            OIMQrScannerActivity.RESULT_SCAN_SUCCESS -> {
                val scannedUri = result.data?.getStringExtra(OIMQrScannerActivity.EXTRA_SCANNED_URI)
                if (scannedUri != null) {
                    Log.d(TAG, "QR scan successful: $scannedUri")
                    handleScannedUri(scannedUri)
                } else {
                    Log.w(TAG, "QR scan success but no URI in result")
                    showErrorToast("QR scan completed but no URI received")
                }
            }
            OIMQrScannerActivity.RESULT_SCAN_ERROR -> {
                val errorMessage = result.data?.getStringExtra(OIMQrScannerActivity.EXTRA_ERROR_MESSAGE)
                Log.d(TAG, "QR scan error: $errorMessage")
                showErrorToast(errorMessage ?: "QR scan failed")
            }
            OIMQrScannerActivity.RESULT_SCAN_CANCELLED -> {
                Log.d(TAG, "QR scan cancelled by user")
                // No need to show error for user cancellation
            }
            else -> {
                Log.w(TAG, "Unknown QR scan result code: ${result.resultCode}")
                showErrorToast("Unknown QR scan result")
            }
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize OIMPaymentManager
        oimPaymentManager = OIMPaymentManager(
            peerManager = mainViewModel.peerManager,
            context = applicationContext,
            scope = lifecycleScope
        )
        
        setContent {
            MaterialTheme {
                OIMHomeScreenWithDialog()
            }
        }
        
        // Observe payment state changes
        observePaymentState()
    }
    
    /**
     * Main composable with payment dialog overlay
     */
    @RequiresApi(Build.VERSION_CODES.BAKLAVA)
    @Composable
    private fun OIMHomeScreenWithDialog() {
        val paymentState by oimPaymentManager.incomingPushState.collectAsState()
        
        Box(modifier = Modifier.fillMaxSize()) {
            // Background home screen
            OIMHomeScreenContent(
                onScanQrClick = { launchQrScanner() },
                onChestClick = {
                    val intent = Intent(this@OIMHomeScreenActivity, OIMChestScreenActivity::class.java)
                    startActivity(intent)
                }
            )
            
            // Payment dialog overlay (shown when terms are ready)
            if (paymentState is IncomingTerms && paymentState !is IncomingAccepting) {
                val terms = paymentState as IncomingTerms
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    OIMPaymentDialog(
                        terms = terms,
                        onAccept = {
                            oimPaymentManager.confirmPeerPushCredit(terms)
                        },
                        onReject = {
                            Log.d(TAG, "Payment rejected by user")
                            showErrorToast("Payment rejected")
                            // Reset state by preparing with empty URI (will fail gracefully)
                            // This clears the dialog
                        }
                    )
                }
            }
        }
    }
    
    /**
     * Observes payment state changes and handles different states
     */
    private fun observePaymentState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                oimPaymentManager.incomingPushState.collect { state ->
                    when (state) {
                        is IncomingChecking -> {
                            Log.d(TAG, "Validating payment...")
                        }
                        
                        is IncomingTerms -> {
                            if (state !is IncomingAccepting) {
                                Log.d(TAG, "Payment terms ready: ${state.amountEffective}")
                            }
                        }
                        
                        is IncomingTosReview -> {
                            // Exchange ToS needs review - show message
                            showErrorToast("Exchange Terms of Service need review")
                            Log.w(TAG, "Exchange ToS review required: ${state.exchangeBaseUrl}")
                        }
                        
                        is IncomingAccepted -> {
                            // Payment accepted successfully
                            showSuccessToast("Payment received successfully!")
                            Log.d(TAG, "Payment accepted successfully")
                        }
                        
                        is IncomingError -> {
                            // Show error message
                            val errorMsg = if (BuildConfig.DEBUG) {
                                state.info.toString()
                            } else {
                                state.info.userFacingMsg
                            }
                            showErrorToast("Payment error: $errorMsg")
                            Log.e(TAG, "Payment error: ${state.info}")
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Launches the OIM QR scanner activity.
     */
    private fun launchQrScanner() {
        val intent = Intent(this, OIMQrScannerActivity::class.java)
        qrScannerLauncher.launch(intent)
    }
    
    /**
     * Handles a successfully scanned URI for incoming push payments.
     * Now processes payment directly in OIM UI instead of redirecting to MainActivity.
     * 
     * @param uri The scanned Taler pay-push URI
     */
    private fun handleScannedUri(uri: String) {
        Log.d(TAG, "Handling scanned URI in OIM UI: $uri")
        
        // Process payment directly using OIMPaymentManager
        oimPaymentManager.preparePeerPushCredit(uri)
    }
    
    /**
     * Shows an error toast to the user.
     * 
     * @param message The error message to display
     */
    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Shows a success toast to the user.
     * 
     * @param message The success message to display
     */
    private fun showSuccessToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}

/**
 * The OIM Home Screen content composable.
 * 
 * This is the stateless UI component that can be reused in different contexts.
 */
@Composable
fun OIMHomeScreenContent(
    modifier: Modifier = Modifier,
    onScanQrClick: () -> Unit,
    onChestClick: () -> Unit,
) {
    TalerSurface {
        Box(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPaddingBottom()
                .background(Color(0xFF8B4513))
        ) {
            Image(
                painter = painterResource(id = R.drawable.woodbackground),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White.copy(alpha = 0.9f))
                    .clickable(onClick = onScanQrClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.qrcode),
                    contentDescription = stringResource(R.string.button_scan_qr_code),
                    modifier = Modifier.size(32.dp)
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(120.dp)
                    .clickable(onClick = onChestClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chestslclosed),
                    contentDescription = "Chest",
                    modifier = Modifier.size(100.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

/**
 * Preview composable for the OIM Home Screen.
 */
@Preview(showSystemUi = true)
@Composable
fun OIMHomeScreenPreview() {
    MaterialTheme {
        OIMHomeScreenContent(
            onScanQrClick = { /* No-op for preview */ },
            onChestClick = { /* No-op for preview */ }
        )
    }
}

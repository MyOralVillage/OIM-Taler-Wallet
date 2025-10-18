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
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.client.android.Intents.Scan.MIXED_SCAN
import com.google.zxing.client.android.Intents.Scan.SCAN_TYPE
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanOptions.QR_CODE
import net.taler.wallet.R
import java.util.Locale
import androidx.core.net.toUri

/**
 * Standalone activity for QR scanning specific to OIM UI.
 * 
 * This activity:
 * - Launches barcode scanner using ScanContract()
 * - Parses and validates the scanned URI (check that it is INCOMING PUSH only)
 * - Returns result to OIMHomeScreen via activity result API
 * - Shows error toast if invalid URI (quick and easy feedback)
 * - Uses the same barcode scanner library as MainActivity
 */
class OIMQrScannerActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "OIMQrScannerActivity"
        
        // Activity result constants
        const val RESULT_SCAN_SUCCESS = Activity.RESULT_OK
        const val RESULT_SCAN_CANCELLED = Activity.RESULT_CANCELED
        const val RESULT_SCAN_ERROR = Activity.RESULT_FIRST_USER
        
        // Intent extras
        const val EXTRA_SCANNED_URI = "scanned_uri"
        const val EXTRA_ERROR_MESSAGE = "error_message"
        
        // Valid incoming push URI actions for OIM UI (matching HandleUriFragment logic)
        private val incomingPushActions = listOf("pay-push")
    }
    
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result == null || result.contents == null) {
            Log.d(TAG, "QR scan cancelled or no content")
            setResult(RESULT_SCAN_CANCELLED)
            finish()
            return@registerForActivityResult
        }
        
        val scannedUri = result.contents
        Log.d(TAG, "Scanned URI: $scannedUri")
        
        if (validateIncomingPushUri(scannedUri)) {
            // Valid incoming push URI for OIM UI - return success
            val resultIntent = Intent().apply {
                putExtra(EXTRA_SCANNED_URI, scannedUri)
            }
            setResult(RESULT_SCAN_SUCCESS, resultIntent)
            finish()
        } else {
            // Invalid URI - show error and return error result
            val errorMessage = "Invalid incoming payment URI. Please scan a valid pay-push QR code for OIM UI."
            showErrorToast(errorMessage)
            
            val resultIntent = Intent().apply {
                putExtra(EXTRA_ERROR_MESSAGE, errorMessage)
            }
            setResult(RESULT_SCAN_ERROR, resultIntent)
            finish()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Launch QR scanner immediately when activity starts
        launchQrScanner()
    }
    
    /**
     * Launches the QR code scanner with appropriate settings for OIM UI.
     */
    private fun launchQrScanner() {
        val scanOptions = ScanOptions().apply {
            setPrompt("Scan a pay-push QR code to receive money in OIM UI")
            setBeepEnabled(true)
            setOrientationLocked(false)
            setDesiredBarcodeFormats(QR_CODE)
            addExtra(SCAN_TYPE, MIXED_SCAN)
        }
        
        barcodeLauncher.launch(scanOptions)
    }
    
    /**
     * Validates that the scanned URI is a valid incoming push payment URI for OIM UI.
     * Uses the same parsing logic as HandleUriFragment to ensure consistency.
     * 
     * @param uri The scanned URI string
     * @return true if the URI is a valid incoming push payment, false otherwise
     */
    private fun validateIncomingPushUri(uri: String): Boolean {
        try {
            // First check if it's a payto URI (not supported for OIM UI)
            if (uri.startsWith("payto://", ignoreCase = true)) {
                Log.d(TAG, "URI is a payto URI, not supported for OIM UI: $uri")
                return false
            }
            
            // Use the same normalization logic as HandleUriFragment
            val normalizedURL = uri.lowercase(Locale.ROOT)
            val action = normalizedURL.substring(
                if (normalizedURL.startsWith("taler://", ignoreCase = true)) {
                    "taler://".length
                } else if (normalizedURL.startsWith("ext+taler://", ignoreCase = true)) {
                    "ext+taler://".length
                } else if (normalizedURL.startsWith("taler+http://", ignoreCase = true)) {
                    // Only allow in dev mode (matching HandleUriFragment logic)
                    Log.d(TAG, "URI uses taler+http scheme, not supported for OIM UI: $uri")
                    return false
                } else {
                    // Not a recognized Taler URI scheme
                    Log.d(TAG, "URI is not a recognized Taler URI scheme: $uri")
                    return false
                }
            )
            
            // Check if it's an incoming push action for OIM UI (matching HandleUriFragment logic)
            val isValidIncomingPush = action.startsWith("pay-push/", ignoreCase = true)
            
            if (!isValidIncomingPush) {
                Log.d(TAG, "URI is not an incoming push payment for OIM UI: $uri (action: $action)")
                return false
            }
            
            Log.d(TAG, "Valid incoming push URI for OIM UI: $uri")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Error validating URI: $uri", e)
            return false
        }
    }
    
    /**
     * Shows a quick error toast to the user.
     * 
     * @param message The error message to display
     */
    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

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
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>
 */

package net.taler.lib.android

import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter.getDefaultAdapter
import android.nfc.cardemulation.CardEmulation
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.math.BigInteger

/**
 * NFC Host Card Emulation (HCE) service that exposes a Taler URI via NFC.
 *
 * This service implements the APDU command flow to emulate an NDEF tag according to
 * NFC Forum specifications. It responds to NFC readers with a URI payload.
 */
class TalerNfcService : HostApduService() {

    /** The URI that will be exposed via NFC */
    private var uri: String? = null

    /** NDEF message wrapping the URI */
    private val ndefMessage: NdefMessage?
        get() = uri?.let { NdefMessage(createUriRecord(it)) }

    /** NDEF message as bytes */
    private val ndefUriBytes: ByteArray?
        get() = ndefMessage?.toByteArray()

    /** Length of the NDEF message in a 2-byte array */
    private val ndefUriLen: ByteArray?
        get() = ndefUriBytes?.size?.toLong()?.let { size ->
            fillByteArrayToFixedDimension(BigInteger.valueOf(size).toByteArray(), 2)
        }

    /** Tracks whether the capability container was read */
    private var readCapabilityContainerCheck = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getStringExtra("uri")?.let { uri = it }
        Log.d(TAG, "onStartCommand() | URI: $uri")
        return Service.START_STICKY
    }

    override fun processCommandApdu(
        commandApdu: ByteArray?,
        extras: Bundle?
    ): ByteArray {

        Log.d(TAG, "Processing command APDU")

        if (commandApdu == null) return A_ERROR

        val message = ndefMessage ?: return A_ERROR

        Log.d(TAG, "processCommandApdu() | incoming commandApdu: " + commandApdu.toHex())

        // Handle APDU commands following NFC Forum Version 2.0 Command Flow
        return when {
            APDU_SELECT.contentEquals(commandApdu) -> A_OKAY
            CAPABILITY_CONTAINER_OK.contentEquals(commandApdu) -> A_OKAY
            READ_CAPABILITY_CONTAINER.contentEquals(commandApdu)
            && !readCapabilityContainerCheck -> {
                readCapabilityContainerCheck = true
                READ_CAPABILITY_CONTAINER_RESPONSE
            }
            NDEF_SELECT_OK.contentEquals(commandApdu) -> A_OKAY
            NDEF_READ_BINARY_NLEN.contentEquals(commandApdu) -> {
                val response = ByteArray(ndefUriLen!!.size + A_OKAY.size)
                System.arraycopy(ndefUriLen!!, 0, response, 0, ndefUriLen!!.size)
                System.arraycopy(A_OKAY, 0, response, ndefUriLen!!.size, A_OKAY.size)
                readCapabilityContainerCheck = false
                response
            }
            commandApdu.sliceArray(0..1).contentEquals(NDEF_READ_BINARY) -> {
                val offset = commandApdu.sliceArray(2..3).toHex().toInt(16)
                val length = commandApdu[4].toInt()
                val fullResponse = ByteArray(ndefUriLen!!.size + ndefUriBytes!!.size)
                System.arraycopy(ndefUriLen!!, 0, fullResponse, 0, ndefUriLen!!.size)
                System.arraycopy(ndefUriBytes!!,
                    0, fullResponse, ndefUriLen!!.size, ndefUriBytes!!.size)
                val slicedResponse = fullResponse.sliceArray(offset until fullResponse.size)
                val realLength = minOf(slicedResponse.size, length)
                val response = ByteArray(realLength + A_OKAY.size)
                System.arraycopy(slicedResponse, 0, response, 0, realLength)
                System.arraycopy(A_OKAY, 0, response, realLength, A_OKAY.size)
                readCapabilityContainerCheck = false
                response
            }
            else -> A_ERROR
        }
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "onDeactivated() Fired! Reason: $reason")
    }

    /** Converts byte array to a hex string for logging */
    private fun ByteArray.toHex(): String {
        val result = StringBuffer()
        forEach {
            val octet = it.toInt()
            val firstIndex = (octet and 0xF0).ushr(4)
            val secondIndex = octet and 0x0F
            result.append(HEX_CHARS[firstIndex])
            result.append(HEX_CHARS[secondIndex])
        }
        return result.toString()
    }

    /** Creates an NDEF URI record from a string */
    private fun createUriRecord(uri: String) = NdefRecord.createUri(uri)

    /**
     * Ensures a byte array has the exact length [fixedSize], padding with 0x00 if necessary.
     */
    private fun fillByteArrayToFixedDimension(array: ByteArray, fixedSize: Int): ByteArray {
        if (array.size == fixedSize) return array
        val start = byteArrayOf(0x00.toByte())
        val filledArray = ByteArray(start.size + array.size)
        System.arraycopy(start, 0, filledArray, 0, start.size)
        System.arraycopy(array, 0, filledArray, start.size, array.size)
        return fillByteArrayToFixedDimension(filledArray, fixedSize)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() NFC service")
        uri = null
    }

    companion object {
        private const val TAG = "taler-wallet-hce"
        private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

        /** APDU command bytes and responses (truncated for brevity) */
        private val APDU_SELECT = byteArrayOf(/* ... */)
        private val CAPABILITY_CONTAINER_OK = byteArrayOf(/* ... */)
        private val READ_CAPABILITY_CONTAINER = byteArrayOf(/* ... */)
        private val READ_CAPABILITY_CONTAINER_RESPONSE = byteArrayOf(/* ... */)
        private val NDEF_SELECT_OK = byteArrayOf(/* ... */)
        private val NDEF_READ_BINARY = byteArrayOf(/* ... */)
        private val NDEF_READ_BINARY_NLEN = byteArrayOf(/* ... */)
        private val A_OKAY = byteArrayOf(0x90.toByte(), 0x00)
        private val A_ERROR = byteArrayOf(0x6A.toByte(), 0x82.toByte())

        /**
         * Checks if the device supports NFC.
         */
        fun hasNfc(context: Context): Boolean = getDefaultAdapter(context) != null

        /**
         * Sets this service as the default HCE handler.
         */
        fun setDefaultHandler(activity: Activity) {
            val adapter = getDefaultAdapter(activity) ?: return
            val emulation = CardEmulation.getInstance(adapter)
            try {
                val cn = ComponentName(activity, TalerNfcService::class.java)
                emulation.setPreferredService(activity, cn)
            } catch (e: NullPointerException) {
                Log.d(TAG, "Not setting this app as the preferred NFC handler!")
            }
        }

        /** Unsets the default HCE handler. */
        fun unsetDefaultHandler(activity: Activity) {
            val adapter = getDefaultAdapter(activity) ?: return
            val emulation = CardEmulation.getInstance(adapter)
            emulation.unsetPreferredService(activity)
        }

        /** Sets the URI to be shared over NFC. */
        fun setUri(activity: Activity, uri: String) {
            if (!hasNfc(activity)) return
            val intent = Intent(activity, TalerNfcService::class.java)
            intent.putExtra("uri", uri)
            activity.startService(intent)
        }

        /** Clears the currently shared URI. */
        fun clearUri(activity: Activity) {
            if (!hasNfc(activity)) return
            val intent = Intent(activity, TalerNfcService::class.java)
            activity.stopService(intent)
        }
    }
}

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

package net.taler.common.utils.network

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.RGB_565
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import com.google.zxing.BarcodeFormat.QR_CODE
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

/**
 * Utility object for generating QR codes as [Bitmap] images.
 */
object QrCodeManager {

    /**
     * Generates a QR code bitmap for the given text.
     *
     * Uses ZXing's [QRCodeWriter] to encode the text into a QR code.
     *
     * @param text The string to encode into the QR code.
     * @param size The width and height of the resulting bitmap in pixels (default 256).
     * @return A [Bitmap] containing the QR code, with black modules on a white background.
     */
    fun makeQrCode(text: String, size: Int = 256): Bitmap {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, QR_CODE, size, size)
        val height = bitMatrix.height
        val width = bitMatrix.width
        val bmp = createBitmap(width, height, RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp[x, y] = if (bitMatrix.get(x, y)) BLACK else WHITE
            }
        }

        return bmp
    }
}


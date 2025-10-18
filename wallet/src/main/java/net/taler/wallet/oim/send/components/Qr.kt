/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap

@SuppressLint("UseKtx")
fun generateQrBitmap(text: String, size: Int): Bitmap {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    return createBitmap(size, size).apply {
        for (x in 0 until size)
            for (y in 0 until size)
                setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
    }
}

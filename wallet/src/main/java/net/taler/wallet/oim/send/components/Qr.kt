/**
 * ## generateQrBitmap
 *
 * Utility function to generate a QR code bitmap for the given text string.
 * Used in the OIM Send flow to encode payment URIs or peer-to-peer transfer
 * identifiers.
 *
 * Internally uses ZXing’s [QRCodeWriter] to create a monochrome square bitmap
 * of the specified size.
 *
 * @param text Text content to encode into the QR code.
 * @param size Width and height of the generated bitmap in pixels.
 * @return A [Bitmap] representing the generated QR code.
 *
 * @see com.google.zxing.qrcode.QRCodeWriter
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

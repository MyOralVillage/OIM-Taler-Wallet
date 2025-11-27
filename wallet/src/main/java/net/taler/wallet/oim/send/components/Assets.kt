
package net.taler.wallet.oim.send.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.*
import net.taler.wallet.oim.resourceMappers.Background

/**
 * ## Asser loader - WoodTableBackground
 *
 * Composable utility for rendering the wooden table texture beneath
 * send screen in the OIM Send flow. Uses the `Tables` drawable
 * mapper to load either a light or dark wood variant.
 *
 * Typically serves as a static visual base for the Send screen and
 * is layered behind animated components such as [NoteFlyer] and [NotesPile].
 *
 * @param modifier Layout modifier for positioning or scaling the background.
 * @param light **DEPRECATED** picks background based off of dark/light mode
 * @see Background.resourceMapper
 */
@Composable
fun WoodTableBackground(
    modifier: Modifier = Modifier,
   light: Boolean? = null
) {
    Image(
        painter = painterResource(Background(LocalContext.current).resourceMapper()),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

/**
 * Generates a QR code bitmap from a given text using ZXing's [QRCodeWriter].
 *
 * The produced bitmap is monochrome: black squares on a white background.
 * This method is synchronous and safe to call from any thread.
 *
 * @param text The text or URI to encode into the QR code.
 * @param size The desired width and height (in pixels) of the square bitmap.
 * @return A [Bitmap] containing the QR code.
 *
 * @throws WriterException If the input cannot be encoded.
 */
@SuppressLint("UseKtx")
fun generateQrBitmap(text: String, size: Int): Bitmap {
    val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    return createBitmap(size, size).apply {
        for (x in 0 until size)
            for (y in 0 until size)
                setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
    }
}
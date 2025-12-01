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
package net.taler.wallet.oim.utils.assets

import android.annotation.SuppressLint
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.*
import net.taler.wallet.oim.utils.res_mappers.Background

/**
 * ## Asset loader - WoodTableBackground
 *
 * Composable utility for rendering the wooden table texture beneath
 * send screen in the OIM Send flow. Uses the `Tables` drawable
 * mapper to load either a light or dark wood variant.
 *
 * Typically serves as a static visual base for the Send screen and
 * is layered behind animated components such as [net.taler.wallet.oim.notes.NoteFlyer] and [net.taler.wallet.oim.notes.NotesPile].
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

internal object OimColours {
    val INCOMING_COLOUR = Color(0xff4caf50)
    val OUTGOING_COLOUR = Color(0xFFC32909)
    val TRX_HIST_COLOUR = Color(0x6600838F)
}
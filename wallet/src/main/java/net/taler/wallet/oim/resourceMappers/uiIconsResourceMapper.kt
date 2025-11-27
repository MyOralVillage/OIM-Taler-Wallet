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

package net.taler.wallet.oim.resourceMappers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import net.taler.common.R.drawable.*
import androidx.core.graphics.createBitmap
import net.taler.wallet.TAG
import org.apache.commons.text.StringEscapeUtils

/**
 * Maps button names to their corresponding image bitmaps.
 *
 * @property buttonName The name of the button (case-insensitive).
 * @property iso: iso country code
 */
internal class UIIcons(val buttonName: String, val iso: String? = null) {

    /**
     * Maps a string-based button identifier into the corresponding [ImageBitmap] resource.
     *
     * ### Supported button names and their corresponding resources:
     * - `"send"`               → `send` bitmap
     * - `"receive"`            → `receive` bitmap
     * - `"tranx_hist"`         → `transaction_history` bitmap
     * - `"deposit"`            → `deposit` bitmap
     * - `"withdraw"`           → `withdraw_og` bitmap
     * - `"filter"`             → `filter` bitmap
     * - `"greenarrowright"`    → `greenarrowright` bitmap
     * - `"greencheckmark"`     → `greencheck` bitmap
     * - `"redcross"`           → `redcross` bitmap
     * - `"country_flag"`       → Result of [`countryFlag`](iso)
     * - `"chest_open"`         → `chest_open`
     * - `"chest_closed"`       → `chest_closed`
     * - `"incoming_transaction"` -> `incoming_transaction`
     *
     * @return The corresponding [ImageBitmap] for the given button name.
     * @throws IllegalArgumentException if the button name does not match any of the supported types.
     */
    @Composable
    fun resourceMapper(): ImageBitmap =
        when (this.buttonName.lowercase().trim()) {
            "send"              -> ImageBitmap.imageResource(send)
            "receive"           -> ImageBitmap.imageResource(receive)
            "tranx_hist"        -> ImageBitmap.imageResource(transaction_history)
            "deposit"           -> ImageBitmap.imageResource(deposit)
            "withdraw"          -> ImageBitmap.imageResource(withdraw_og)
            "filter"            -> ImageBitmap.imageResource(filter)
            "greenarrowright"   -> ImageBitmap.imageResource(greenarrowright)
            "greencheckmark"    -> ImageBitmap.imageResource(greencheck)
            "redcross"          -> ImageBitmap.imageResource(redcross)
            "country_flag"      -> countryFlag(iso)
            "chest_open"        -> ImageBitmap.imageResource(chest_open)
            "chest_closed"      -> ImageBitmap.imageResource(chest_closed)
            "incoming_transaction" -> ImageBitmap.imageResource(incoming_transaction)
            else ->
                throw IllegalArgumentException("Invalid button: $buttonName")
        }

    /**
     * Returns a flag bitmap for the current user location.
     *
     * If location permission is granted and a country code is available,
     * returns the corresponding country flag emoji as a bitmap.
     * Otherwise, returns a default flag image.
     *
     * @param iso If null, returns the default flag.
     * @return ImageBitmap of the country flag or default flag.
     */
    @SuppressLint("LocalContextResourcesRead")
    @Composable
    private fun countryFlag(iso: String?): ImageBitmap {
        if (iso == null) {
            LaunchedEffect(iso) {
                Log.d(TAG, "=== UIIcons.countryFlag SEES COUNTRY CODE: null    ===")
                Log.d(TAG, "=== UIIcons.countryFlag RETS COUNTRY FLAG: default ===")
            }
            return ImageBitmap.imageResource(default_flag)
        } else {

            // get country code converted to utf8 two letter code
            val cnt =
                iso
                    .trim()
                    .uppercase()
                    .map { char -> Character.codePointAt("$char", 0) - 0x41 + 0x1F1E6 }
                    .map { cpnt -> Character.toChars(cpnt) }
                    .joinToString("") { String(it) }

            // get dimensions of default flag - will ensure all
            // flags are same dimension; then create blank canvas
            val dim = FlagDimensions.getDim(LocalContext.current)
            val btm = createBitmap(dim.first, dim.second)
            val cnv = Canvas(btm)

            // paint
            val pnt = Paint(ANTI_ALIAS_FLAG).apply {
                textSize = dim.first.toFloat()
                textAlign = Paint.Align.CENTER
            }
            val xdm = dim.first / 2f
            val ydm = (dim.second / 2f) - ((pnt.descent() + pnt.ascent()) / 2f)
            cnv.drawText(cnt, xdm, ydm, pnt)

            LaunchedEffect(cnt) {
                Log.d(TAG, "=== UIIcons.countryFlag SEES COUNTRY CODE:  " +
                        "${StringEscapeUtils.escapeJava(cnt)}   ===")
                Log.d(TAG, "=== UIIcons.countryFlag RETS COUNTRY FLAG: $cnt ===")
            }
            return btm.asImageBitmap()
        }
    }
}

/**
 * Removes the need to continuously load flag sizes each time a flag is displayed.
 *
 * Caches the dimensions of the default flag resource to ensure consistent
 * display sizing across all flags in the app. Dimensions are loaded lazily on first access.
 */
private  object FlagDimensions {

    /** Tracks whether the dimensions have been initialized. */
    private var _isInit: Boolean = false

    /**
     * Cached width of the flag.
     *
     * Stored as a string to allow late initialization; converted to [Int] when accessed.
     */
    private lateinit var _width: String

    /**
     * Cached height of the flag.
     *
     * Stored as a string to allow late initialization; converted to [Int] when accessed.
     */
    private lateinit var _height: String

    /**
     * Retrieves the width and height of the default flag bitmap in pixels.
     *
     * This function lazily initializes and caches the dimensions on the first call using a
     * decode with `inJustDecodeBounds = true` to avoid loading the actual bitmap into memory.
     *
     * Subsequent calls return the cached dimensions without accessing resources again.
     *
     * @param ctx The application or activity context used to access resources.
     * @return A [Pair] containing the width and height of the flag in pixels.
     */
    fun getDim(ctx: Context): Pair<Int, Int> {
        return if (!_isInit) {

            // Get dimensions of the default flag - ensures all flags use the same dimension
            val opt = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(ctx.resources, default_flag, opt)

            // Store width/height, set isInit == true
            // Must convert to String since primitive types
            // like Int cannot be late-initialized
            _width = opt.outWidth.toString()
            _height = opt.outHeight.toString()
            _isInit = true

            // Recurse to return the just-initialized values
            getDim(ctx)
        } else {
            Pair(_width.toInt(), _height.toInt())
        }
    }
}
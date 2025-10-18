/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable

/** Map android_asset paths -> debug drawables so Compose Preview renders images. */
fun assetDrawable(context: Context, assetPath: String): Drawable? {
    return try {
        context.assets.open(assetPath).use { input ->
            val bitmap = BitmapFactory.decodeStream(input)
            bitmap.toDrawable(context.resources)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

<<<<<<< HEAD
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
=======
import androidx.annotation.DrawableRes
import net.taler.wallet.R

/**
 * Map android_asset paths -> debug drawables so Compose Preview renders images.
 */
object PreviewAssets {
    @DrawableRes
    fun id(assetPath: String): Int? = when (assetPath) {
        // Background
        WOOD_TABLE -> R.drawable.tara_meinczinger_g_ycplasnb4_unsplash

        // Top bar icons
        ICON_CHEST -> R.drawable.chestopen
        ICON_SEND  -> R.drawable.send_icon

        // Notes
        "OIM/currency/SLE/sle-1.png"     -> R.drawable.sle_1
        "OIM/currency/SLE/sle-5.png"     -> R.drawable.sle_5
        "OIM/currency/SLE/sle-10.png"    -> R.drawable.sle_10
        "OIM/currency/SLE/sle-25.png"    -> R.drawable.sle_25
        "OIM/currency/SLE/sle-50.png"    -> R.drawable.sle_50
        "OIM/currency/SLE/SLE-100.png"   -> R.drawable.sle_100
        "OIM/currency/SLE/SLE-200.png"   -> R.drawable.sle_200
        "OIM/currency/SLE/SLE-500.png"   -> R.drawable.sle_500
        "OIM/currency/SLE/SLE-1000.png"  -> R.drawable.sle_1000
        "OIM/currency/SLE/SLE-2000.png"  -> R.drawable.sle_2000

        // Purposes (examples; add more as you drop files)
        "OIM/transaction-purposes/medicine.png"        -> R.drawable.medicine
        "OIM/transaction-purposes/electricity.png"     -> R.drawable.electricity
        "OIM/transaction-purposes/phone.png"           -> R.drawable.phone
        "OIM/transaction-purposes/water.png"           -> R.drawable.water
        "OIM/transaction-purposes/groceries.png"       -> R.drawable.groceries
        "OIM/transaction-purposes/housing.png"         -> R.drawable.housing
        "OIM/transaction-purposes/tools.png"           -> R.drawable.tools
        "OIM/transaction-purposes/loan.png"            -> R.drawable.loan
        "OIM/transaction-purposes/schooling.png"       -> R.drawable.schooling
        "OIM/transaction-purposes/school_supplies.png" -> R.drawable.school_supplies
        "OIM/transaction-purposes/gas.png"             -> R.drawable.gas
        "OIM/transaction-purposes/market_stall.png"    -> R.drawable.market_stall
        "OIM/transaction-purposes/doctor_appointment.png" -> R.drawable.doctor_appointment

        else -> null
>>>>>>> 5c7011a (fixed preview animations)
    }
}

/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

// ---- Backgrounds ------------------------------------------------------------
const val WOOD_TABLE =
    "OIM/backgrounds/tables/tara-meinczinger-G_yCplAsnB4-unsplash.jpg"

// ---- Icons ------------------------------------------------------------------
const val ICON_CHEST = "OIM/buttons/chests/open/ChestOpen.svg"
const val ICON_SEND  = "OIM/buttons/send-01.svg"

// ---- SLE note images (path -> value) ----------------------------------------
val SleNotes: List<Pair<String, Int>> = listOf(
    "OIM/currency/SLE/sle-1.png" to 1,
    "OIM/currency/SLE/sle-5.png" to 5,
    "OIM/currency/SLE/sle-10.png" to 10,
    "OIM/currency/SLE/sle-25.png" to 25,
    "OIM/currency/SLE/sle-50.png" to 50,
    "OIM/currency/SLE/SLE-100.png" to 100,
    "OIM/currency/SLE/SLE-200.png" to 200,
    "OIM/currency/SLE/SLE-500.png" to 500,
    "OIM/currency/SLE/SLE-1000.png" to 1000,
    "OIM/currency/SLE/SLE-2000.png" to 2000,
    "OIM/currency/SLE/SLE-5000.png" to 5000,
    "OIM/currency/SLE/SLE-10000.png" to 10000,
    "OIM/currency/SLE/SLE-20000.png" to 20000,
)

// ---- Purpose icons (path -> label) ------------------------------------------
val PurposeIcons: List<Pair<String, String>> = listOf(
    "OIM/transaction-purposes/medicine.png" to "Health",
    "OIM/transaction-purposes/electricity.png" to "Electricity",
    "OIM/transaction-purposes/tools.png" to "Tools",
    "OIM/transaction-purposes/phone.png" to "Phone",
    "OIM/transaction-purposes/water.png" to "Water",
    "OIM/transaction-purposes/gas.png" to "Fuel",
    "OIM/transaction-purposes/groceries.png" to "Groceries",
    "OIM/transaction-purposes/housing.png" to "Housing",
    "OIM/transaction-purposes/schooling.png" to "School",
    "OIM/transaction-purposes/school_supplies.png" to "School Fees",
    "OIM/transaction-purposes/loan.png" to "Remittance",
    "OIM/transaction-purposes/books.png" to "Books",
)

/**
 * Load any file from src/main/assets via file:///android_asset/<path>.
 */
@Composable
fun assetPainter(assetPath: String) = rememberAsyncImagePainter(
    ImageRequest.Builder(LocalContext.current)
        .data("file:///android_asset/$assetPath")
        .build()
)

/**
 * Use assets at runtime; use an optional drawable fallback in Preview.
 */
@Composable
fun assetPainterOrPreview(assetPath: String, previewResId: Int? = null): Painter {
    return if (LocalInspectionMode.current) {
        previewResId?.let { painterResource(id = it) } ?: ColorPainter(Color(0xFF3A2F28))
    } else {
        rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/$assetPath")
                .build()
        )
    }
}

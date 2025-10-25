<<<<<<< HEAD
/*
<<<<<<< HEAD
 * GPLv3-or-later
 */
<<<<<<< HEAD

/*
 * GPLv3-or-later
 */
/*
=======
>>>>>>> 9068d57 (got rid of bugs in send apk)
 * GPLv3-or-later
=======
/**
 * ## QrScreen
 *
 * Displays the generated Taler payment QR code for peer-to-peer transfers.
 * Shows amount, currency, and purpose alongside the scannable code, and
 * gracefully handles loading states while the payment URI is being prepared.
 *
 * Includes navigation controls (Home, Back) and uses [WoodTableBackground]
 * for the contextual table texture backdrop.
 *
 * @param talerUri The Taler payment URI to encode as a QR code (null while loading).
 * @param amount The payment amount and currency to display.
 * @param purpose The transaction purpose icon to show next to the QR code.
 * @param onBack Invoked when the user presses the "Back" button.
 * @param onHome Optional callback to return to the home screen.
 *
 * @see net.taler.wallet.oim.send.components.generateQrBitmap
 * @see net.taler.wallet.oim.send.components.WoodTableBackground
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
 */

package net.taler.wallet.oim.send.screens

<<<<<<< HEAD
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
////import net.taler.wallet.oim.send.components.WOOD_TABLE
//import net.taler.wallet.oim.send.components.assetPainterOrPreview
//import net.taler.wallet.oim.send.components.generateQrBitmap
//import androidx.core.net.toUri
//
//// TODO refactor to use res_mapping_extensions
//
//
/////**
//// * QR screen with wood background.
//// *
//// * You can pass display fields (amount/currency/purpose) separately while the QR
//// * is generated from [talerUri]. If any display field is null, we try to infer it
//// * from the URI (amount=SLE:3, summary=...).
//// */
////@Composable
////fun QrScreen(
////    talerUri: String,
////    amountText: String? = null,
////    currencyCode: String? = null,
////    displayLabel: String? = null,
////    purpose: String? = null,
////    onBack: () -> Unit,
////) {
////    // Parse what we can from the URI for display fallbacks
////    val parsed = remember(talerUri) { parseFromTalerUri(talerUri) }
////    val uiAmount = amountText ?: parsed.amountNumber
////    val uiCurrency = currencyCode ?: parsed.currency
////    val uiLabel = displayLabel ?: uiCurrency
////    val uiPurpose = purpose ?: parsed.summary
////
////    val qr = remember(talerUri) { generateQrBitmap(talerUri, 720) }
////
////    Box(Modifier.fillMaxSize()) {
////        // SAME WOOD BACKGROUND AS BEFORE
////        Image(
//////            painter = assetPainterOrPreview(WOOD_TABLE),
////            contentDescription = null,
////            modifier = Modifier.fillMaxSize(),
////            contentScale = ContentScale.Crop
////        )
////
////        IconButton(
////            onClick = onBack,
////            modifier = Modifier
////                .padding(8.dp)
////                .size(40.dp)
////                .align(Alignment.TopStart)
////        ) {
////            Icon(
////                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
////                contentDescription = "Back",
////                tint = Color.White
////            )
////        }
////
////        Row(
////            modifier = Modifier
////                .fillMaxSize()
////                .padding(24.dp),
////            horizontalArrangement = Arrangement.SpaceBetween,
////            verticalAlignment = Alignment.CenterVertically
////        ) {
////            Surface(
////                color = Color.White,
////                shape = RoundedCornerShape(24.dp),
////                shadowElevation = 6.dp
////            ) {
////                Image(
////                    bitmap = qr.asImageBitmap(),
////                    contentDescription = "QR",
////                    modifier = Modifier.size(320.dp)
////                )
////            }
////
////            Column(horizontalAlignment = Alignment.End) {
////                uiAmount?.let {
////                    Text(
////                        text = it,
////                        color = Color.White,
////                        fontSize = 64.sp,
////                        fontWeight = FontWeight.ExtraBold
////                    )
////                }
////                uiLabel?.let {
////                    Text(
////                        text = it,
////                        color = Color.White,
////                        fontSize = 28.sp,
////                        fontWeight = FontWeight.SemiBold
////                    )
////                }
////                uiPurpose?.let {
////                    Spacer(Modifier.height(8.dp))
////                    Text(
////                        text = it,
////                        color = Color.White.copy(alpha = 0.9f),
////                        fontSize = 18.sp,
////                        fontWeight = FontWeight.Medium
////                    )
////                }
////            }
////        }
////    }
////}
////
////private data class ParsedDisplay(
////    val amountNumber: String? = null,
////    val currency: String? = null,
////    val summary: String? = null,
////)
////
/////** Minimal parser to recover amount/currency/summary from common Taler URIs. */
////private fun parseFromTalerUri(talerUri: String): ParsedDisplay {
////    val uri = runCatching { talerUri.toUri() }.getOrNull() ?: return ParsedDisplay()
////    val amountParam = uri.getQueryParameter("amount") // "SLE:3" etc.
////    val summary = uri.getQueryParameter("summary") ?: uri.getQueryParameter("subject")
////
////    val (currency, number) = amountParam
////        ?.split(':', limit = 2)
////        ?.let { it.getOrNull(0) to it.getOrNull(1) }
////        ?: (null to null)
////
////    return ParsedDisplay(
////        amountNumber = number,
////        currency = currency,
////        summary = summary
////    )
////}
////
=======
package net.taler.wallet.oim.send.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
=======
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ArrowBack
<<<<<<< HEAD
<<<<<<< HEAD
import androidx.compose.material3.*
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
>>>>>>> 321d128 (updated send to be more dynamic)
=======
import androidx.compose.material3.*
>>>>>>> 9068d57 (got rid of bugs in send apk)
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
import androidx.compose.ui.text.font.FontWeight
<<<<<<< HEAD
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.wallet.oim.send.app.OimTheme
import net.taler.wallet.oim.send.components.PreviewAssets
import net.taler.wallet.oim.send.components.WOOD_TABLE
import net.taler.wallet.oim.send.components.assetPainterOrPreview
import net.taler.wallet.oim.send.components.generateQrBitmap

/**
 * QR screen with wood background.
 *
 * You can pass display fields (amount/currency/purpose) separately while the QR
 * is generated from [talerUri]. If any display field is null, we try to infer it
 * from the URI (amount=SLE:3, summary=...).
=======
=======
import androidx.compose.ui.tooling.preview.Preview
>>>>>>> 321d128 (updated send to be more dynamic)
import androidx.compose.ui.unit.dp
=======
=======
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
>>>>>>> 9068d57 (got rid of bugs in send apk)
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
<<<<<<< HEAD
import net.taler.wallet.oim.res_mapping_extensions.Background
=======
<<<<<<< HEAD
<<<<<<< HEAD
import net.taler.wallet.oim.res_mapping_extensions.Background
=======
import net.taler.wallet.oim.res_mapping_extensions.Tables
>>>>>>> c4c1157 (got rid of bugs in send apk)
=======
>>>>>>> f82ba56 (UI changes and fix qr code loading for send)
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.oim.send.components.WoodTableBackground
import net.taler.wallet.oim.send.components.generateQrBitmap

<<<<<<< HEAD
/**
<<<<<<< HEAD
 * QR screen with wood background (assets now from res_mapping_extensions).
 * You can pass display fields separately while the QR is generated from [talerUri].
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
 * QR screen with wood background.
>>>>>>> 321d128 (updated send to be more dynamic)
 */
=======
>>>>>>> 9068d57 (got rid of bugs in send apk)
@Composable
fun QrScreen(
    talerUri: String?,        // null => show preparing state
    amount: Amount,
    purpose: TranxPurp?,
    onBack: () -> Unit,
    onHome: () -> Unit = {}
) {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
    // Parse what we can from the URI for display fallbacks
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
    val parsed = remember(talerUri) { parseFromTalerUri(talerUri) }
    val uiAmount = amountText ?: parsed.amountNumber
    val uiCurrency = currencyCode ?: parsed.currency
    val uiLabel = displayLabel ?: uiCurrency
    val uiPurpose = purpose ?: parsed.summary

=======
>>>>>>> 321d128 (updated send to be more dynamic)
    val qr = remember(talerUri) { generateQrBitmap(talerUri, 720) }

    Box(Modifier.fillMaxSize()) {
<<<<<<< HEAD
        // SAME WOOD BACKGROUND AS BEFORE
=======
    Box(Modifier.fillMaxSize()) {
<<<<<<< HEAD
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
        Image(
            painter = assetPainterOrPreview(WOOD_TABLE, PreviewAssets.id(WOOD_TABLE)),
=======
        Image(
<<<<<<< HEAD
            bitmap = Tables(type = false).resourceMapper(),
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
            painter = painterResource(Background(LocalContext.current).resourceMapper()),
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(8.dp)
                .size(40.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
=======
        WoodTableBackground(modifier = Modifier.fillMaxSize(), light = false)
>>>>>>> f82ba56 (UI changes and fix qr code loading for send)

        // Top controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.White)
            }
            FilledTonalButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Back")
            }
        }

        // Main content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR or spinner while preparing
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.size(360.dp)
            ) {
                if (talerUri == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Preparing payment…", color = Color.Black)
                    }
                } else {
                    val qr = remember(talerUri) { generateQrBitmap(talerUri, 1024) }
                    Image(
                        bitmap = qr.asImageBitmap(),
                        contentDescription = "Taler QR",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Amount + purpose
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 24.dp)
            ) {
                Text(
                    text = amount.amountStr,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp
                )
                Text(
                    text = amount.spec?.name ?: amount.currency,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                if (purpose != null) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(purpose.resourceMapper()),
                            contentDescription = null,
                            modifier = Modifier.size(112.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .alpha(0f)
                    )
                }
            }
        }
    }
}
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 5c7011a (fixed preview animations)
=======
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)

<<<<<<< HEAD
<<<<<<< HEAD
private data class ParsedDisplay(
    val amountNumber: String? = null,
    val currency: String? = null,
    val summary: String? = null,
)

/** Minimal parser to recover amount/currency/summary from common Taler URIs. */
private fun parseFromTalerUri(talerUri: String): ParsedDisplay {
<<<<<<< HEAD
    val uri = runCatching { Uri.parse(talerUri) }.getOrNull() ?: return ParsedDisplay()
=======
    val uri = runCatching { talerUri.toUri() }.getOrNull() ?: return ParsedDisplay()
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
    val amountParam = uri.getQueryParameter("amount") // "SLE:3" etc.
    val summary = uri.getQueryParameter("summary") ?: uri.getQueryParameter("subject")

    val (currency, number) = amountParam
        ?.split(':', limit = 2)
        ?.let { it.getOrNull(0) to it.getOrNull(1) }
        ?: (null to null)

    return ParsedDisplay(
        amountNumber = number,
        currency = currency,
        summary = summary
    )
}

<<<<<<< HEAD
>>>>>>> f512e18 (added backend integration and db transaction update)
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
@Preview(name = "QR Screen", showBackground = true, widthDp = 1280, heightDp = 800)
=======
@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
>>>>>>> 9068d57 (got rid of bugs in send apk)
@Composable
private fun QrScreenPreview() {
    MaterialTheme {
        QrScreen(
            talerUri = "ext+taler://pay-push/exchange.demo.taler.net/EXAMPLEPURSEID",
            amount = Amount.fromString("KUDOS", "10"),
            purpose = null,
            onBack = {}
        )
    }
}
<<<<<<< HEAD
>>>>>>> 321d128 (updated send to be more dynamic)
=======
>>>>>>> 9068d57 (got rid of bugs in send apk)
=======
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)

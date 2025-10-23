///*
// * This file is part of GNU Taler
// * (C) 2025 Taler Systems S.A.
// *
// * GNU Taler is free software; you can redistribute it and/or modify it under the
// * terms of the GNU General Public License as published by the Free Software
// * Foundation; either version 3, or (at your option) any later version.
// *
// * GNU Taler is distributed in the hope that it will be useful, but WITHOUT ANY
// * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
// * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License along with
// * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>
// */
//
// TODO: this needs to be refactored so that the bank notes can be updated dynamically
// TODO: use Amount.resourceMapper() direclty :)

package net.taler.wallet.oim.main
//
//import android.content.Context
//import android.util.Log
//import androidx.annotation.DrawableRes
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import net.taler.database.data_models.Amount
//import net.taler.wallet.BuildConfig
//import net.taler.wallet.R
//import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
//import net.taler.wallet.peer.IncomingTerms
//
//private const val TAG = "OIMPaymentDialog"
//
//
//// =================================================================================
//// 1. THE "SMART" COMPONENT (for your app)
//// This is the public function that your wallet code will call.
//// It understands the `IncomingTerms` data model.
//// =================================================================================
//@Composable
//fun OIMPaymentDialog(
//    terms: IncomingTerms,
//    onAccept: () -> Unit,
//    onReject: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    val context = LocalContext.current
//    val amount = terms.amountEffective
//
//    // TODO: just did some patches, might not work!
//    // It extracts the simple data and passes it to the "dumb" content component.
//    OIMPaymentDialogContent(
//        amountValue = amount.value.toString(),
//        currencyDisplayName = amount.currency,
//        summary = terms.contractTerms.summary,
//        banknoteDrawableId = amount.resourceMapper().first(), // only gets first one; needs fixing!
//        onAccept = {
//            Log.d(TAG, "Payment accepted: ${terms.id}")
//            onAccept()
//        },
//        onReject = {
//            Log.d(TAG, "Payment rejected: ${terms.id}")
//            onReject()
//        },
//        onPlayAudio = {
//            playAudioFeedback(context, amount)
//        },
//        modifier = modifier
//    )
//}
//
//
//// =================================================================================
//// 2. THE "DUMB" COMPONENT (for rendering UI and for previews)
//// This is a private implementation detail. It only knows how to display primitive data.
//// Because it has no dependency on `net.taler.database.*`, it is perfectly previewable.
//// =================================================================================
//@Composable
//private fun OIMPaymentDialogContent(
//    amountValue: String,
//    currencyDisplayName: String,
//    summary: String,
//    @DrawableRes banknoteDrawableId: Int,
//    onAccept: () -> Unit,
//    onReject: () -> Unit,
//    onPlayAudio: () -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    val isKudosCurrency = currencyDisplayName.uppercase() == "KUDOS"
//    Card(
//        modifier = modifier
//            .fillMaxWidth(0.95f)
//            .wrapContentHeight(),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White
//        ),
//        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(24.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Title text
//            Text(
//                text = "You have received $amountValue $currencyDisplayName",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center,
//                color = Color.Black
//            )
//
//            // Payment flow visualization
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                if (isKudosCurrency) {
//                    // For KUDOS: Show text instead of banknote
//                    Box(
//                        modifier = Modifier
//                            .width(120.dp)
//                            .height(80.dp)
//                            .background(Color(0xFFF0F8FF), shape = RoundedCornerShape(8.dp))
//                            .padding(8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "KUDOS\n$amountValue",
//                            fontSize = 16.sp,
//                            fontWeight = FontWeight.Bold,
//                            textAlign = TextAlign.Center,
//                            color = Color(0xFF1E90FF)
//                        )
//                    }
//                } else {
//                    // For other currencies: Show banknote image
//                    Image(
//                        painter = painterResource(id = banknoteDrawableId),
//                        contentDescription = "Currency banknote",
//                        modifier = Modifier
//                            .width(120.dp)
//                            .height(80.dp),
//                        contentScale = ContentScale.Fit
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                // Amount badge with arrow
//                Column(
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .background(Color(0xFFFFFFFF), shape = RoundedCornerShape(8.dp))
//                            .padding(horizontal = 12.dp, vertical = 6.dp)
//                    ) {
//                        Text(
//                            text = amountValue,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//                    }
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Image(
//                        painter = painterResource(id = R.drawable.greenarrowright),
//                        contentDescription = "Arrow",
//                        modifier = Modifier.size(40.dp)
//                    )
//                }
//
//                Spacer(modifier = Modifier.width(8.dp))
//
//                // Use different chest images for KUDOS
//                Image(
//                    painter = painterResource(
//                        id = if (isKudosCurrency) R.drawable.chest_open else R.drawable.chest_sl_open
//                    ),
//                    contentDescription = "Chest",
//                    modifier = Modifier.size(80.dp),
//                    contentScale = ContentScale.Fit
//                )
//            }
//
//            if (summary.isNotBlank()) {
//                Text(
//                    text = summary,
//                    fontSize = 14.sp,
//                    textAlign = TextAlign.Center,
//                    color = Color.Gray
//                )
//            }
//
//            // Action buttons row
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceEvenly,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Placeholder to keep button positions unchanged after removing sound icon
//                Box(
//                    modifier = Modifier
//                        .size(60.dp),
//                    contentAlignment = Alignment.Center
//                ) { /* intentionally empty */ }
//                Box(
//                    modifier = Modifier
//                        .size(60.dp)
//                        .clickable { onAccept() },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.greencheck),
//                        contentDescription = "Accept payment",
//                        modifier = Modifier.size(40.dp)
//                    )
//                }
//                Box(
//                    modifier = Modifier
//                        .size(60.dp)
//                        .clickable { onReject() },
//                    contentAlignment = Alignment.Center
//                ) {
//                    Image(
//                        painter = painterResource(id = R.drawable.redcross),
//                        contentDescription = "Reject payment",
//                        modifier = Modifier.size(40.dp)
//                    )
//                }
//            }
//        }
//    }
//}
//
//
//// =================================================================================
//// HELPER FUNCTIONS (Unchanged)
//// =================================================================================
//
////private fun getBanknoteDrawableId(amount: Amount): Int? {
////    val currency = amount.currency.uppercase()
////    val totalValue = amount.value + (amount.fraction / 100_000_000.0)
////
////    return when (currency) {
////        "SLE" -> getSLEBanknoteDrawable(totalValue)
////        "KUDOS", "TESTKUDOS" -> {
////            // For KUDOS currencies, we don't use banknote images
////            // The UI will show text instead
////            R.drawable.chest_closed
////        }
////        else -> {
////            Log.w(TAG, "Unsupported currency: $currency, using default banknote")
////            R.drawable.sle_1
////        }
////    }
//
//
/////** TODO only use in debug mode! needs refactoring */
////private fun getSLEBanknoteDrawable(amount: Double): Int? {
////    if (!BuildConfig.DEBUG) null
////    val banknotes = listOf(
////        1.0 to R.drawable.sle_1, 5.0 to R.drawable.sle_5, 10.0 to R.drawable.sle_10,
////        25.0 to R.drawable.sle_25, 50.0 to R.drawable.sle_50, 100.0 to R.drawable.sle_100,
////        200.0 to R.drawable.sle_200, 500.0 to R.drawable.sle_500, 1000.0 to R.drawable.sle_1000,
////        2000.0 to R.drawable.sle_2000
////    )
////    return banknotes.firstOrNull { it.first >= amount }?.second ?: banknotes.last().second
////}
////
/////** TODO only use in debug mode! needs refactoring */
////private fun getCurrencyDisplayName(currency: String): String {
////
////    return when (currency.uppercase()) {
////        "SLE" -> "Leones"
////        "USD" -> "Dollars"
////        "EUR" -> "Euros"
////        "CHF" -> "Francs"
////        "XOF" -> "Francs CFA"
////        "KUDOS" -> "KUDOS"
////        "TESTKUDOS" -> "Test KUDOS"
////        else -> currency
////    }
////}
//
//private fun playAudioFeedback(context: Context, amount: Amount) {
//    try {
//        Log.d(TAG, "Playing audio for amount: ${amount.value} ${amount.currency}")
//    } catch (e: Exception) {
//        Log.e(TAG, "Error playing audio feedback: ${e.message}", e)
//    }
//}
//
//
//// =================================================================================
//// 3. THE PREVIEW (Now fixed)
//// The preview now calls the private "dumb" OIMPaymentDialogContent directly.
//// This completely avoids the `NoClassDefFoundError`.
//// =================================================================================
//
////@Preview(showBackground = true
////
////)
////@Composable
////fun OIMPaymentDialogPreviewSLE() {
////    OIMPaymentDialogContent(
////        amountValue = "100",
////        currencyDisplayName = "Leones",
////        summary = "Payment for services",
////        banknoteDrawableId = R.drawable.sle_100,
////        onAccept = {},
////        onReject = {},
////        onPlayAudio = {}
////    )
////}
//
//@Preview(showBackground = true)
//@Composable
//fun OIMPaymentDialogPreviewKUDOS() {
//    OIMPaymentDialogContent(
//        amountValue = "50",
//        currencyDisplayName = "KUDOS",
//        summary = "Test payment in KUDOS",
//        banknoteDrawableId = R.drawable.chest_closed,
//        onAccept = {},
//        onReject = {},
//        onPlayAudio = {}
//    )
//}

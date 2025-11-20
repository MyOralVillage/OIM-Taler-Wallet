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

package net.taler.wallet.oim.main

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import net.taler.wallet.oim.res_mapping_extensions.UIIcons
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.peer.IncomingTerms
 
 private const val TAG = "OIMPaymentDialog"
 
/**
 * High-level payment dialog that wires network-facing [IncomingTerms] data to the stateless UI.
 *
 * @param terms pending peer payment terms retrieved from wallet-core.
 * @param onAccept callback invoked when the user accepts the payment.
 * @param onReject callback invoked when the user rejects the payment.
 * @param modifier optional modifier applied to the dialog container.
 */
@Composable
fun OIMPaymentDialog(
    terms: IncomingTerms,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    val amount = terms.amountEffective

    val amountText = amount.amountStr

    val banknoteDrawableIds = when (amount.currency.uppercase(Locale.ROOT)) {
         "CHF", "XOF", "EUR", "SLE" -> amount.resourceMapper()
         else -> {
             if (!isKudosCurrency(amount.currency)) {
                 Log.w(TAG, "No banknote mapper defined for ${amount.currency}")
             }
             emptyList()
         }
     }
 
    OIMPaymentDialogContent(
        amountText = amountText,
        currencyCode = amount.currency,
        summary = terms.contractTerms.summary,
        banknoteDrawableIds = banknoteDrawableIds,
        onAccept = {
            Log.d(TAG, "Payment accepted: ${terms.id}")
            onAccept()
        },
        onReject = {
            Log.d(TAG, "Payment rejected: ${terms.id}")
            onReject()
        }
    )
}
 
/**
 * Stateless payment dialog body used both at runtime and during Compose previews.
 *
 * @param amountText formatted amount string shown to the user.
 * @param currencyCode ISO or token currency code associated with the amount.
 * @param summary human-readable summary taken from the contract terms.
 * @param banknoteDrawableIds ordered list of drawable ids used to render banknotes.
 * @param onAccept triggered when the user accepts the payment.
 * @param onReject triggered when the user rejects the payment.
 * @param modifier layout modifier provided by the caller.
 */
@Composable
private fun OIMPaymentDialogContent(
    amountText: String,
    currencyCode: String,
    summary: String,
    banknoteDrawableIds: List<Int>,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
     val currencyDisplayName = currencyDisplayName(currencyCode)
     val isKudosCurrency = isKudosCurrency(currencyCode)
 
     Card(
         modifier = modifier
             .fillMaxWidth(0.95f)
             .wrapContentHeight(),
         shape = RoundedCornerShape(16.dp),
         colors = CardDefaults.cardColors(
             containerColor = Color.White
         ),
         elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
     ) {
         Column(
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(24.dp),
             horizontalAlignment = Alignment.CenterHorizontally,
             verticalArrangement = Arrangement.spacedBy(16.dp)
         ) {
             Text(
                 text = "You have received $amountText $currencyDisplayName",
                 fontSize = 20.sp,
                 fontWeight = FontWeight.Bold,
                 textAlign = TextAlign.Center,
                 color = Color.Black
             )
 
             Row(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.spacedBy(12.dp),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 when {
                     isKudosCurrency -> {
                         Box(
                             modifier = Modifier
                                 .weight(1f)
                                 .height(80.dp)
                                 .background(Color(0xFFF0F8FF), shape = RoundedCornerShape(8.dp)),
                             contentAlignment = Alignment.Center
                         ) {
                             Text(
                                 text = "KUDOS\n$amountText",
                                 fontSize = 16.sp,
                                 fontWeight = FontWeight.Bold,
                                 textAlign = TextAlign.Center,
                                 color = Color(0xFF1E90FF)
                             )
                         }
                     }
                     banknoteDrawableIds.isEmpty() -> {
                         Box(
                             modifier = Modifier
                                 .weight(1f)
                                 .height(80.dp)
                                 .background(Color(0xFFECECEC), shape = RoundedCornerShape(8.dp)),
                             contentAlignment = Alignment.Center
                         ) {
                             Text(
                                 text = "Artwork unavailable",
                                 fontSize = 13.sp,
                                 textAlign = TextAlign.Center,
                                 color = Color.DarkGray
                             )
                         }
                     }
                     else -> {
                         LazyRow(
                             modifier = Modifier
                                 .weight(1f)
                                 .height(80.dp),
                             horizontalArrangement = Arrangement.spacedBy(8.dp),
                             verticalAlignment = Alignment.CenterVertically
                         ) {
                             items(banknoteDrawableIds) { drawableId ->
                                 Image(
                                     painter = painterResource(id = drawableId),
                                     contentDescription = "Currency banknote",
                                     modifier = Modifier
                                         .width(120.dp)
                                         .height(80.dp),
                                     contentScale = ContentScale.Fit
                                 )
                             }
                         }
                     }
                 }
 
                 Column(
                     horizontalAlignment = Alignment.CenterHorizontally,
                     verticalArrangement = Arrangement.spacedBy(4.dp)
                 ) {
                     Box(
                         modifier = Modifier
                             .background(Color.White, shape = RoundedCornerShape(8.dp))
                             .padding(horizontal = 12.dp, vertical = 6.dp)
                     ) {
                         Text(
                             text = amountText,
                             fontSize = 18.sp,
                             fontWeight = FontWeight.Bold,
                             color = Color.Black
                         )
                     }
 
                     Image(
                         bitmap = UIIcons("greenarrowright").resourceMapper(),
                         contentDescription = "Payment transfer arrow",
                         modifier = Modifier.size(40.dp)
                     )
                 }
 
                 Image(
                     bitmap  = UIIcons("chest_open").resourceMapper(),
                     contentDescription = "Treasure chest",
                     modifier = Modifier.size(80.dp),
                     contentScale = ContentScale.Fit
                 )
             }
 
             if (summary.isNotBlank()) {
                 Text(
                     text = summary,
                     fontSize = 14.sp,
                     textAlign = TextAlign.Center,
                     color = Color.Gray
                 )
             }
 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(60.dp))

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onAccept() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap  = UIIcons("greencheckmark").resourceMapper(),
                        contentDescription = "Accept payment",
                        modifier = Modifier.size(40.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onReject() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = UIIcons("redcross").resourceMapper(),
                        contentDescription = "Reject payment",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}
 
/** Converts a currency code to a user-facing display name. */
private fun currencyDisplayName(currencyCode: String): String =
     when (currencyCode.uppercase(Locale.ROOT)) {
         "SLE" -> "Leones"
         "USD" -> "Dollars"
         "EUR" -> "Euros"
         "CHF" -> "Francs"
         "XOF" -> "Francs CFA"
         "KUDOS" -> "KUDOS"
         "TESTKUDOS" -> "Test KUDOS"
         else -> currencyCode
     }
 
/** Returns `true` if the provided currency code corresponds to a kudos token. */
private fun isKudosCurrency(currencyCode: String): Boolean =
     currencyCode.equals("KUDOS", ignoreCase = true) ||
         currencyCode.equals("TESTKUDOS", ignoreCase = true)
 
 

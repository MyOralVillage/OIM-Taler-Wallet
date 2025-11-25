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

package net.taler.wallet.oim.history.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.common.R.drawable.incoming_transaction
import net.taler.common.R.drawable.outgoing_transaction
import net.taler.database.data_models.Amount
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.TranxPurp
import net.taler.wallet.oim.history.components.Bills
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper // Amount
import java.time.LocalDate

@Composable
fun TransactionCard(
    amount: String,
    currency: String,
    date: String,
    purpose: TranxPurp?,
    modifier: Modifier = Modifier,
    dir: FilterableDirection,
    displayAmount: Amount
) {
    val badgeColor =
        if (dir.getValue()) Color(0xFFE8F5E9)
        else Color(0xFFFFB3B3)
    val textColor =
        if (dir.getValue()) Color(0xFF4CAF50)
        else Color(0xFFC32909)

    // Parse date "yyyy-MM-dd" -> year / month / day
    val parsedDate = remember(date) {
        runCatching { LocalDate.parse(date) }.getOrNull()
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (parsedDate != null) {
                val day = parsedDate.dayOfMonth.toString().padStart(2, '0')
                val month = parsedDate.monthValue.toString().padStart(2, '0')
                val year = parsedDate.year.toString()

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DatePill(icon = "☀️", value = day)

                    Text(
                        text = " / ",
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    DatePill(icon = "🌙", value = month)

                    Text(
                        text = " / ",
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    DatePill(icon = "⭐", value = year)
                }

            } else {
                // fallback if parsing fails
                Text(
                    text = date,
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Icons and Amount (unchanged)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(dir.resourceMapper()),
                    contentDescription = "Transaction direction",
                    modifier = Modifier.size(80.dp),
                    tint = Color.Unspecified
                )

                if (purpose != null) {
                    Icon(
                        painter = painterResource(purpose.resourceMapper()),
                        contentDescription = "Transaction purpose",
                        modifier = Modifier.size(80.dp),
                        tint = Color.Unspecified
                    )
                }

                Bills(
                    amount = displayAmount,
                    billWidth = 100,
                    billHeight = 64,
                    coinSize = 33,
                    billOffsetX = 7,
                    billOffsetY = 12,
                    coinOffsetX = 15,
                    coinOffsetY = 24
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = badgeColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = amount,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = currency,
                            fontSize = 14.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun DatePill(icon: String, value: String) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 14.sp)
            Text(text = value, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}
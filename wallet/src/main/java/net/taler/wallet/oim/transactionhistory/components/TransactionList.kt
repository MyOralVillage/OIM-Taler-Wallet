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

package net.taler.wallet.oim.transactionhistory.components

import net.taler.wallet.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TransactionCard(
    type: String,
    amount: String,
    currency: String,
    date: String,
    currencyImageRes: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // First Row - Title and Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(type=="S"){
                    Text(
                        text = "Sent $amount $currency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                else{
                    Text(
                        text = "Received $amount $currency",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }

                Text(
                    text = date,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Second Row - Icons and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(type == "R"){
                    Image(
                        painter = painterResource(id = currencyImageRes),
                        contentDescription = "Currency",
                        modifier = Modifier.size(80.dp)
                    )

                    // Amount Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFE8F5E9),
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
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = currency,
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    // Hand Icon
                    Image(
                        painter = painterResource(R.drawable.receive),
                        contentDescription = "Hand",
                        modifier = Modifier.size(70.dp)
                    )

                    // Stack Icon
                    Image(
                        painter = painterResource(R.drawable.chestopen),
                        contentDescription = "Stack",
                        modifier = Modifier.size(80.dp)
                    )
                }
                else{
                    Image(
                        painter = painterResource(R.drawable.chestopen),
                        contentDescription = "Stack",
                        modifier = Modifier.size(80.dp)
                    )

                    // Hand Icon
                    Image(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = "Hand",
                        modifier = Modifier.size(70.dp)
                    )

                    // Amount Badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFB3B3),
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
                                color = Color(0xFFC32909)
                            )
                            Text(
                                text = currency,
                                fontSize = 14.sp,
                                color = Color(0xFFC32909)
                            )
                        }
                    }

                    Image(
                        painter = painterResource(id = currencyImageRes),
                        contentDescription = "Currency",
                        modifier = Modifier.size(80.dp)
                    )

                    // Stack Icon
                }
                // Currency Image

            }
        }
    }
}

// Preview for Android Studio
@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun TransactionCardPreview() {
    TransactionCard(
        type = "R",
        amount = "25",
        currency = "Leones",
        date = "25 Sept 2025",
        currencyImageRes = R.drawable.sle // Using system drawable for preview
    )
}
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

/**
 *Components – TRANSACTION CARD
 *
 * This file defines the card UI for a single transaction in the OIM history
 * timeline view. Each card shows:
 *  - the transaction date formatted as ☀️ day / 🌙 month / ⭐ year,
 *  - an icon for direction (incoming / outgoing),
 *  - an optional icon for purpose/category,
 *  - a coloured amount badge that can be clicked separately.
 *
 * MAIN COMPOSABLES:
 *  - TransactionCard(): Reusable card used by TransactionsList and previews.
 *    Exposes:
 *      • dir + purpose + displayAmount from database models,
 *      • onClick for tapping the whole card (open details),
 *      • onAmountClick for tapping only the amount badge (e.g. open notes).
 *
 * INTEGRATION:
 *  - Consumes Amount, TranxPurp and FilterableDirection from the DB layer.
 *  - Uses OimColours + resourceMapper() for consistent icon and colour theming.
 *  - Rendered inside TransactionsList, which is then hosted by
 *    OimTransactionHistoryScreen as the list-based alternative to RiverSceneCanvas.
 */


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.database.data_models.EDUC_CLTH
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.TranxPurp
import net.taler.wallet.oim.utils.assets.OimColours
import net.taler.wallet.oim.utils.res_mappers.resourceMapper
import java.time.LocalDate

@Composable
fun TransactionCard(
    amount: String,
    currency: String,
    date: String,
    purpose: TranxPurp?,
    modifier: Modifier = Modifier,
    dir: FilterableDirection,
    displayAmount: Amount,
    onClick: (() -> Unit)? = null,
    onAmountClick: (() -> Unit)? = null // NEW: separate callback for amount click
) {
    val badgeColor =
        if (dir.getValue()) OimColours.INCOMING_COLOUR
        else OimColours.OUTGOING_COLOUR
    val textColor = Color.White

    // Parse date "yyyy-MM-dd" -> year / month / day
    val parsedDate = remember(date) {
        runCatching { LocalDate.parse(date) }.getOrNull()
    }

    Card(
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (purpose == null) Color.White else Color(purpose.colourInt())
        ),
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

            // Icons and Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
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

                // Clickable amount badge to show notes
                Box(
                    modifier = Modifier
                        .background(
                            color = badgeColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .then(
                            if (onAmountClick != null) Modifier.clickable { onAmountClick() }
                            else Modifier
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = amount,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = currency,
                            fontSize = 24.sp,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun DatePill(icon: String, value: String) {
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

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Transaction Card Preview",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
fun TransactionCardPreview() {
    TransactionCard(
        amount = "35",
        currency = "SLE",
        date = "2025-11-25",
        purpose = EDUC_CLTH,
        dir = FilterableDirection.INCOMING,
        displayAmount = Amount(
            currency = "SLE",
            value = 35,
            fraction = 0
        )
    )
}
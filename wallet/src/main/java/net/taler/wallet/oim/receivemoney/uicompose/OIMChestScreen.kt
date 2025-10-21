/*
 * This file is part of GNU Taler
 * (C) 2024 Taler Systems S.A.
 *
 * GNU Taler is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * GNU Taler is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.taler.wallet.oim.receivemoney.uicompose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.wallet.R
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.systemBarsPaddingBottom

@Composable
fun OIMChestScreenContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onRequestClick: () -> Unit,
    onTransactionHistoryClick: () -> Unit,
    onWithdrawTestKudosClick: () -> Unit,
    balanceState: BalanceState = BalanceState.None,
) {
    TalerSurface {
        Box(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPaddingBottom()
                .background(Color(0xFF8B4513))
        ) {
            // Wooden background
            Image(
                painter = painterResource(id = R.drawable.table_dark_wood_tara_meinczinger),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Top row with send (left), chest (center), receive (right)
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left send button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onSendClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.send),
                        contentDescription = "Send",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Center chest button (navigate back)
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chest_open),
                        contentDescription = "Chest",
                        modifier = Modifier.size(120.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Right receive button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onRequestClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.receive),
                        contentDescription = "Receive",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Center area with balance display
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val selectedBalance = when (balanceState) {
                    is BalanceState.Success -> {
                        balanceState.balances.firstOrNull { it.currency == "KUDOS" }
                            ?: balanceState.balances.firstOrNull { it.currency == "TESTKUDOS" }
                            ?: balanceState.balances.firstOrNull()
                    }
                    else -> null
                }

                val balanceText = when (balanceState) {
                    is BalanceState.Success -> selectedBalance?.available?.toString(showSymbol = false) ?: "0"
                    is BalanceState.Loading -> "Loading..."
                    is BalanceState.Error -> "Error"
                    else -> "0"
                }
                val currencyText = selectedBalance?.currency ?: "KUDOS"

                Text(
                    text = balanceText,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp
                )
                Text(
                    text = currencyText,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .clickable(onClick = onWithdrawTestKudosClick)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Withdraw Test KUDOS",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Bottom area with transaction history button (ledger icon)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onTransactionHistoryClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.transaction_history),
                        contentDescription = "Transaction History",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun OIMChestScreenPreview() {
    MaterialTheme {
        OIMChestScreenContent(
            onBackClick = { },
            onSendClick = { },
            onRequestClick = { },
            onTransactionHistoryClick = { },
            onWithdrawTestKudosClick = { },
            balanceState = BalanceState.Success(emptyList())
        )
    }
}


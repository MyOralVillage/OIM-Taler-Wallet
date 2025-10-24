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

package net.taler.wallet.oim.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.wallet.R
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.oim.res_mapping_extensions.Background
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
    /**

     * Main composable surface for the OIM Chest Screen.
     * Displays a wooden background, top action buttons (send/chest/receive),
     * balance information in the center, and a bottom transaction history button.
     */
    TalerSurface {
        Box(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPaddingBottom()
        ) {
            /**
             * Wooden background — this should be the ONLY background layer.
             */
            Icon(
                painter = painterResource(
                    Background(LocalContext.current).resourceMapper()
                ),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            ) {
                /**
                 * Top row: Send (left), Chest (center), Receive (right)
                 */
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    /** Left send button */
                    val sendInteraction = remember { MutableInteractionSource() }
                    val isSendPressed by sendInteraction.collectIsPressedAsState()


                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Color(0xFFC32909).copy(alpha = if (isSendPressed) 0.9f else 0.3f)
                            )
                            .clickable(
                                interactionSource = sendInteraction,
                                indication = null,
                                onClick = onSendClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Send",
                            modifier = Modifier.size(100.dp)
                        )
                    }

                    /**
                     * Center chest button (navigate back)
                     * TODO: Make this dynamically match the user's region or currency.
                     */
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(onClick = onBackClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chest_open),
                            contentDescription = "Chest",
                            modifier = Modifier.size(70.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    /** Right receive button */
                    val receiveInteraction = remember { MutableInteractionSource() }
                    val isReceivePressed by receiveInteraction.collectIsPressedAsState()

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Color(0xFF4CAF50).copy(alpha = if (isReceivePressed) 0.9f else 0.3f)
                            )
                            .clickable(
                                interactionSource = receiveInteraction,
                                indication = null,
                                onClick = onRequestClick
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.receive),
                            contentDescription = "Receive",
                            modifier = Modifier.size(100.dp)
                        )
                    }

                }

                /**
                 * Center area — shows balance and "Withdraw Test KUDOS" button.
                 */
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
                        is BalanceState.Success ->
                            selectedBalance?.available?.toString(showSymbol = false) ?: "0"

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

                    /** Withdraw Test KUDOS button */
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

                /**
                 * Bottom row: Transaction History button (ledger icon)
                 */
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {

                    /** state for alpha values */
                    val histInteraction = remember { MutableInteractionSource() }
                    val isHistPressed by histInteraction.collectIsPressedAsState()

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                Color(0x6600838F)
                                    .copy(alpha = if (isHistPressed) 0.9f else 0.3f)
                            )
                            .clickable(onClick = onTransactionHistoryClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.transaction_history),
                            contentDescription = "Transaction History",
                            modifier = Modifier.size(100.dp)
                        )
                    }
                }
            }

        }
    }
}

/**
 * Preview for the OIM Chest Screen.
 * Uses a mock empty balance state.
 */
@Preview
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


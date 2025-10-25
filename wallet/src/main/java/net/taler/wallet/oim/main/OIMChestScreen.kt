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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import kotlinx.coroutines.delay
import net.taler.wallet.R
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.oim.res_mapping_extensions.Background
import net.taler.wallet.systemBarsPaddingBottom
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.launch

/** Reusable button composable with toggle + delay */
@Composable
private fun ButtonBox(
    @DrawableRes drawableId: Int,
    bgColor: Color,
    onClick: () -> Unit,
    size: Dp = 110.dp,
    iconSize: Dp = 100.dp
) {
    val coroutineScope = rememberCoroutineScope()
    var isActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = if (isActive) 0.9f else 0.6f))
            .clickable {
                coroutineScope.launch {
                    isActive = true
                    delay(250)
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(drawableId),
            contentDescription = null,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Displays the OIM chest screen with balance, quick actions, and the visual banknote summary
 * for the currently selected currency.
 *
 * @param modifier external modifier for sizing and padding.
 * @param onBackClick invoked when the center chest button is pressed.
 * @param onSendClick invoked when the send shortcut is tapped.
 * @param onRequestClick invoked when the receive shortcut is tapped.
 * @param onTransactionHistoryClick opens the transaction history view.
 * @param onWithdrawTestKudosClick triggers the test faucet action for development builds.
 * @param balanceState reactive balance payload used to populate the UI.
 */
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
    /** Main composable surface for the OIM Chest Screen. */
    TalerSurface {
        Box(
            modifier = modifier
                .fillMaxSize()
                .systemBarsPaddingBottom()
        ) {
            /** Wooden background — this should be the ONLY background layer. */
            Image(
                painter = painterResource(Background(LocalContext.current).resourceMapper()),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            ) {
                /** Top row: Send (left), Chest (center), Receive (right) */
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ButtonBox(R.drawable.send, Color(0xFFC32909), onSendClick)
                    ButtonBox(R.drawable.chest_open, Color.White, onBackClick, iconSize = 70.dp)
                    ButtonBox(R.drawable.receive, Color(0xFF4CAF50), onRequestClick)
                }

                /** Center area — shows balance and "Withdraw Test KUDOS" button. */
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

                    Text(balanceText, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 48.sp)
                    Text(currencyText, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    /** Withdraw Test KUDOS button */
                    val coroutineScope = rememberCoroutineScope()
                    var isWithdrawActive by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = if (isWithdrawActive) 0.9f else 0.6f))
                            .clickable {
                                coroutineScope.launch {
                                    isWithdrawActive = true
                                    delay(250)
                                    onWithdrawTestKudosClick()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Withdraw Test KUDOS",
                            color = Color.Black,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                /** Bottom row: Transaction History button (ledger icon) */
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    var isHistActive by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x6600838F).copy(alpha = if (isHistActive) 0.9f else 0.6f))
                            .clickable {
                                coroutineScope.launch {
                                    isHistActive = true
                                    delay(250)
                                    onTransactionHistoryClick()
                                }
                            },
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

// ----------------- Banknote stack code and functions unchanged -----------------

@Preview
@Composable
fun OIMChestScreenPreview() {
    MaterialTheme {
        OIMChestScreenContent(
            onBackClick = {},
            onSendClick = {},
            onRequestClick = {},
            onTransactionHistoryClick = {},
            onWithdrawTestKudosClick = {},
            balanceState = BalanceState.Success(emptyList())
        )
    }
}
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
 * GNU General Public License along with GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.taler.wallet.oim.receive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.taler.wallet.MainViewModel
import net.taler.wallet.R
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.oim.send.components.assetPainter
import net.taler.wallet.systemBarsPaddingBottom

/**
 * Activity that displays the OIM Chest Screen showing wallet balance and action buttons.
 * 
 * This activity:
 * - Displays the current KUDOS balance from the wallet
 * - Shows placeholder buttons for send and transaction history
 * - Allows navigation back to the home screen via chest button
 */
class OIMChestScreenActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "OIMChestScreenActivity"
    }
    
    private val mainViewModel: MainViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Load balances when activity starts
        mainViewModel.balanceManager.loadBalances()
        
        setContent {
            MaterialTheme {
                val balanceState by mainViewModel.balanceManager.state.collectAsStateWithLifecycle()
                
                OIMChestScreenContent(
                    onBackClick = { finish() },
                    onSendClick = {
                        // TODO: Add send functionality
                        Log.d(TAG, "Send button clicked - placeholder")
                    },
                    onTransactionHistoryClick = {
                        // TODO: Add transaction history functionality
                        Log.d(TAG, "Transaction history button clicked - placeholder")
                    },
                    balanceState = balanceState
                )
            }
        }
    }
}

/**
 * The OIM Chest Screen content composable.
 * 
 * This displays the chest screen with balance and action buttons.
 */
@Composable
fun OIMChestScreenContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onSendClick: () -> Unit,
    onTransactionHistoryClick: () -> Unit,
    balanceState: BalanceState = BalanceState.None
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
                painter = painterResource(id = R.drawable.woodbackground),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Top row with send button (left), chest button (center), and send button (right)
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
                        painter = assetPainter("OIM/buttons/send-01.svg"),
                        contentDescription = "Send",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Center chest button (back to home)
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clickable(onClick = onBackClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = assetPainter("OIM/buttons/chests/open/ChestSLopen.svg"),
                        contentDescription = "Chest",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                
                // Right send button (duplicate for symmetry)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onSendClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = assetPainter("OIM/buttons/send-01.svg"),
                        contentDescription = "Send",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            // Center area - could be used for banknote representation in the future
            // For now, this is empty as requested
            
            // Bottom area with transaction history button and balance display
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Left transaction history button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onTransactionHistoryClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ledgericon),
                        contentDescription = "Transaction History",
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Right balance display
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val balanceText = when (balanceState) {
                        is BalanceState.Success -> {
                            val kudosBalance = balanceState.balances.find { it.currency == "KUDOS" }
                            kudosBalance?.available?.value?.toString() ?: "0"
                        }
                        is BalanceState.Loading -> "Loading..."
                        is BalanceState.Error -> "Error"
                        else -> "0"
                    }
                    
                    Text(
                        text = balanceText,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp
                    )
                    Text(
                        text = "KUDOS",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 24.sp
                    )
                }
            }
        }
    }
}

/**
 * Preview composable for the OIM Chest Screen.
 */
@Preview(showSystemUi = true)
@Composable
fun OIMChestScreenPreview() {
    MaterialTheme {
        OIMChestScreenContent(
            onBackClick = { /* No-op for preview */ },
            onSendClick = { /* No-op for preview */ },
            onTransactionHistoryClick = { /* No-op for preview */ },
            balanceState = BalanceState.Success(emptyList())
        )
    }
}

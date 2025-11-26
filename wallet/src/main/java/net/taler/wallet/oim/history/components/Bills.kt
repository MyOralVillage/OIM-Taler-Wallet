package net.taler.wallet.oim.history.components

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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.taler.common.R
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper

@Composable
fun Bills(
    amount: Amount,
    modifier: Modifier = Modifier,
    billWidth: Int = 280,
    billHeight: Int = 180,
    coinSize: Int = 100,
    billOffsetX: Int = 20,
    billOffsetY: Int = 35,
    coinOffsetX: Int = 40,
    coinOffsetY: Int = 60
) {
    // Calculate bills and coins outside of composable to avoid try-catch issues
    val billsResult = remember(amount) {
        try {
            val allDrawables = amount.resourceMapper()
            println("ResourceMapper returned: $allDrawables")
            println("Total count: ${allDrawables.size}")
            // Separate bills and coins based on drawable resource IDs
            val bills = mutableListOf<Int>()
            val coins = mutableListOf<Int>()

            allDrawables.forEach { drawableResId ->
                if (isCoin(drawableResId)) {
                    coins.add(drawableResId)
                } else {
                    bills.add(drawableResId)
                }
            }

            println("Bills: $bills")
            println("Coins: $coins")

            BillsResult.Success(bills, coins)
        } catch (e: IllegalArgumentException) {
            BillsResult.Error(e.message ?: "Unknown error")
        }
    }

    val effectiveCoinSize = if (billsResult is BillsResult.Success && billsResult.bills.isEmpty()) {
        (coinSize * 1.5).toInt()
    } else {
        coinSize
    }

    val effectiveCoinOffsetY = if (billsResult is BillsResult.Success && billsResult.bills.isEmpty()) {
        (coinOffsetY - 10).toInt()
    } else {
        coinOffsetY
    }

    when (billsResult) {
        is BillsResult.Success -> {
            Column(
                modifier = modifier
                    .wrapContentWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display amount header
//                Text(
//                    text = "${amount.currency} ${amount.value}.${String.format("%08d", amount.fraction)}",
//                    style = MaterialTheme.typography.headlineSmall,
//                    modifier = Modifier.padding(bottom = 16.dp)
//                )
//
//                Text(
//                    text = "Bills: ${billsResult.bills.size}, Coins: ${billsResult.coins.size}",
//                    style = MaterialTheme.typography.bodySmall
//                )

                // Display bills and coins in same box (layered)
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .requiredHeight(
                            (billHeight + (billsResult.bills.size) * billOffsetY).dp
                        )
                        .padding(0.dp, 0.dp, ((billsResult.bills.size) * billOffsetX / 2).dp, 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Bills stacked (bottom layer)
                    billsResult.bills.forEachIndexed { index, drawableResId ->
                        Image(
                            painter = painterResource(id = drawableResId),
                            contentDescription = "Bill ${index + 1}",
                            modifier = Modifier
                                .width(billWidth.dp)
                                .height(billHeight.dp)
                                .offset(
                                    x = (index * billOffsetX).dp,
                                    y = (index * billOffsetY).dp
                                )
                        )
                    }

                    // Coins stacked vertically (top layer, Z-direction on top of bills)
                    // Coins stacked horizontally (top layer, Z-direction on top of bills)
                    if (billsResult.coins.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(
                                    x = -(billsResult.coins.size * 15).dp,  // Shift left by half total width
                                    y = effectiveCoinOffsetY.dp
                                )
                        ) {
                            billsResult.coins.forEachIndexed { index, drawableResId ->
                                Image(
                                    painter = painterResource(id = drawableResId),
                                    contentDescription = "Coin ${index + 1}",
                                    modifier = Modifier
                                        .size(effectiveCoinSize.dp)
                                        .offset(x = (index * coinOffsetX).dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        is BillsResult.Error -> {
            Text(
                text = "Error: ${billsResult.message}",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = modifier.padding(16.dp)
            )
        }
    }
}

// Helper function to determine if a drawable is a coin
private fun isCoin(drawableResId: Int): Boolean {
    return when (drawableResId) {
        // CHF coins
        R.drawable.chf_five_hundred,
        R.drawable.chf_two_hundred,
        R.drawable.chf_one_hundred,
        R.drawable.chf_one,
        R.drawable.chf_zero_point_five,
            // EUR coins
        R.drawable.eur_one,
        R.drawable.eur_two,
        R.drawable.eur_zero_point_five,
        R.drawable.eur_zero_point_two,
        R.drawable.eur_zero_point_one,
        R.drawable.eur_zero_point_zero_five,
        R.drawable.eur_zero_point_zero_two,
        R.drawable.eur_zero_point_zero_one,
            // SLE coins
        R.drawable.sle_zero_point_five,
        R.drawable.sle_zero_point_twenty_five,
        R.drawable.sle_zero_point_one,
        R.drawable.sle_zero_point_zero_five,
        R.drawable.sle_zero_point_zero_one,
            // XOF coins
        R.drawable.xof_two_hundred,
        R.drawable.xof_one_hundred,
        R.drawable.xof_twenty_five,
        R.drawable.xof_ten,
        R.drawable.xof_five,
        R.drawable.xof_one -> true
        else -> false
    }
}

// Sealed class to handle success/error states
private sealed class BillsResult {
    data class Success(val bills: List<Int>, val coins: List<Int>) : BillsResult()
    data class Error(val message: String) : BillsResult()
}

// ===== PREVIEWS =====

@Preview(showBackground = true, name = "EUR 23.45")
@Composable
fun PreviewBillsEUR() {
    MaterialTheme {
        Bills(
            amount = Amount(
                currency = "EUR",
                value = 23L,
                fraction = 45_000_000 // 0.45
            )
        )
    }
}

@Preview(showBackground = true, name = "KUDOS 15.60")
@Composable
fun PreviewBillsKUDOS() {
    MaterialTheme {
        Bills(
            amount = Amount(
                currency = "KUDOS",
                value = 15L,
                fraction = 62_000_000
            )
        )
    }
}

@Preview(showBackground = true, name = "SLE 10.25")
@Composable
fun PreviewBillsSLE() {
    MaterialTheme {
        Bills(
            amount = Amount(
                currency = "SLE",
                value = 10L,
                fraction = 25_000_000 // 0.25
            )
        )
    }
}

@Preview(showBackground = true, name = "XOF 5000")
@Composable
fun PreviewBillsXOF() {
    MaterialTheme {
        Bills(
            amount = Amount(
                currency = "XOF",
                value = 5000L,
                fraction = 0
            )
        )
    }
}
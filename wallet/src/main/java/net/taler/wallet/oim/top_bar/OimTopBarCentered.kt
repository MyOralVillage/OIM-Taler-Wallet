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
package net.taler.wallet.oim.top_bar

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.utils.res_mappers.UIIcons
import net.taler.wallet.oim.utils.res_mappers.resourceMapper

/**
 * ## OimTopBarCentered
 *
 * Center-aligned top bar displaying the user's wallet balance
 * at the top-right corner. Shows a chest icon for visual anchoring of the balance.
 *
 * Features animated "spill out" when amount text is clicked (bills appear left-to-right)
 * and "fold in" when bills are clicked (bills disappear right-to-left back into chest).
 *
 * @param balance Current wallet [Amount] displayed in the top-center.
 * @param onChestClick function that maps map clicks; default to nothing
 * @param colour the background color; alpha is internally set to 0.65f
 */
@Composable
fun OimTopBarCentered(
    balance: Amount,
    onChestClick: () -> Unit = {},
    colour: Color
) {
    var showBills by remember { mutableStateOf(false) }
    var animatingOut by remember { mutableStateOf(false) }
    var animatingIn by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val billResIds = remember(balance) { balance.resourceMapper() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colour.copy(alpha = 0.65f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Chest icon button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(onClick = onChestClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = UIIcons("chest_open").resourceMapper(),
                    contentDescription = "Chest",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Balance display area
            Box(
                modifier = Modifier
                    .widthIn(min = 200.dp)
                    .heightIn(min = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                if (!showBills && !animatingOut) {
                    // Text display - clickable to spill out
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable {
                            animatingOut = true
                            scope.launch {
                                delay(50) // Small delay before starting
                                showBills = true
                                delay((billResIds.size * 80L) + 200) // Wait for animation
                                animatingOut = false
                            }
                        }
                    ) {
                        Text(
                            text = balance.amountStr,
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Text(
                            text = balance.currency,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                if (showBills || animatingOut || animatingIn) {
                    // Bills display - clickable to fold in
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!animatingIn && showBills) {
                                    animatingIn = true
                                    scope.launch {
                                        delay((billResIds.size * 80L) + 200) // Wait for fold-in
                                        showBills = false
                                        animatingIn = false
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            billResIds.forEachIndexed { index, resId ->
                                AnimatedBill(
                                    resId = resId,
                                    index = index,
                                    totalBills = billResIds.size,
                                    isSpillingOut = animatingOut || (showBills && !animatingIn),
                                    isFoldingIn = animatingIn
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual bill with spill-out (left-to-right) and fold-in (right-to-left) animations
 */
@Composable
private fun AnimatedBill(
    resId: Int,
    index: Int,
    totalBills: Int,
    isSpillingOut: Boolean,
    isFoldingIn: Boolean
) {
    // Spill out: left to right (index 0 appears first)
    // Fold in: right to left (last index disappears first)
    val delayForSpillOut = index * 80L
    val delayForFoldIn = (totalBills - 1 - index) * 80L

    var startSpillOut by remember { mutableStateOf(false) }
    var startFoldIn by remember { mutableStateOf(false) }

    LaunchedEffect(isSpillingOut) {
        if (isSpillingOut) {
            delay(delayForSpillOut)
            startSpillOut = true
        } else {
            startSpillOut = false
        }
    }

    LaunchedEffect(isFoldingIn) {
        if (isFoldingIn) {
            delay(delayForFoldIn)
            startFoldIn = true
        } else {
            startFoldIn = false
        }
    }

    // Animation for spill out (scale + alpha)
    val spillOutProgress by animateFloatAsState(
        targetValue = if (startSpillOut) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "spillOut"
    )

    // Animation for fold in (scale + alpha)
    val foldInProgress by animateFloatAsState(
        targetValue = if (startFoldIn) 0f else 1f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "foldIn"
    )

    // Determine current progress based on state
    val currentProgress = when {
        isFoldingIn -> foldInProgress
        else -> spillOutProgress
    }

    // Only show if there's some progress
    if (currentProgress > 0f) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .padding(2.dp)
                .graphicsLayer {
                    scaleX = currentProgress
                    scaleY = currentProgress
                    alpha = currentProgress

                    // Spill out: come from left (chest position)
                    // Fold in: go back to left (chest position)
                    if (isSpillingOut && !isFoldingIn) {
                        translationX = -200f * (1f - spillOutProgress)
                    } else if (isFoldingIn) {
                        translationX = -200f * (1f - foldInProgress)
                    }
                }
        )
    }
}
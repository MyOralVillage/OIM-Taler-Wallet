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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.oim.OimColours
import net.taler.wallet.oim.resourceMappers.Background
import net.taler.wallet.oim.resourceMappers.UIIcons
import net.taler.wallet.oim.resourceMappers.resourceMapper
import net.taler.wallet.oim.send.components.StackedNotes
import net.taler.wallet.oim.main.components.NotePreviewOverlay
import net.taler.wallet.oim.send.components.NotesGalleryOverlay
import androidx.compose.ui.tooling.preview.Preview
/** Reusable button composable with toggle + delay */
@Composable
private fun ButtonBox(
    bitmap: ImageBitmap,
    bgColor: Color,
    onClick: () -> Unit,
    size: Dp,
    iconSize: Dp
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
            bitmap = bitmap,
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
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
        ) {
            val topButtonSize = 75.dp
            val topIconSize = 65.dp
            val centerButtonSize = 60.dp
            val centerIconSize = 60.dp
            val historyButtonSize = 70.dp
            val historyIconSize = 60.dp
            val centerContentTopPadding = 60.dp
            val centerContentBottomPadding =20.dp

            var selectedNoteResId by remember { mutableStateOf<Int?>(null) }
            var isStackExpanded by remember { mutableStateOf(false) }
            var showStackPreview by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            val selectedBalance = when (balanceState) {
                is BalanceState.Success -> {
                    balanceState.balances.firstOrNull { it.currency == "KUDOS" }
                        ?: balanceState.balances.firstOrNull { it.currency == "TESTKUDOS" }
                        ?: balanceState.balances.firstOrNull()
                }
                else -> null
            }

            // Show banknotes/coins for the current balance using ResourceMapper.
            // Treat KUDOS/TESTKUDOS as SLE for visuals.
            val amountForNotes = remember(selectedBalance) {
                val a = selectedBalance?.available
                if (a != null) {
                    when (selectedBalance.currency) {
                        "KUDOS", "TESTKUDOS", "KUD" -> a.withCurrency("SLE")
                        else -> a
                    }
                } else null
            }

            BackHandler(enabled = selectedNoteResId != null || showStackPreview) {
                if (showStackPreview) {
                    showStackPreview = false
                    scope.launch {
                        delay(200)
                        isStackExpanded = false
                    }
                } else {
                    selectedNoteResId = null
                }
            }

            /** Wooden background  */
            Image(
                painter = painterResource(Background(LocalContext.current).resourceMapper()),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
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

                    // top right "send" button; shaded red
                    ButtonBox(
                        UIIcons("send").resourceMapper(),
                        OimColours.OUTGOING_COLOUR,
                        onSendClick,
                        topButtonSize,
                        topIconSize
                    )

                    // top center "chest open" button; no shade
                    ButtonBox(
                        UIIcons("chest_open").resourceMapper(),
                        Color.Unspecified,
                        onBackClick,
                        centerButtonSize,
                        centerIconSize
                    )

                    // top right "receive" button; green shade
                    ButtonBox(
                        UIIcons("receive").resourceMapper(),
                        OimColours.INCOMING_COLOUR,
                        onRequestClick,
                        topButtonSize,
                        topIconSize
                    )

                }


                /** Center area — shows balance and "Withdraw Test KUDOS" button. */
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = centerContentTopPadding,
                            bottom = centerContentBottomPadding,
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    if (amountForNotes != null) {
                        // Only notes/coins visuals; no numbers above/below.
                        Spacer(modifier = Modifier.height(12.dp))
//                         NotesOnTable(
//                            amount = amountForNotes,
//                            onNoteClick = { resId -> selectedNoteResId = resId }
//                         )
                        StackedNotes(
                            noteResIds = amountForNotes.resourceMapper(),
                            noteHeight = 79.dp,
                            noteWidth = 115.dp,
                            expanded = isStackExpanded,
                            onClick = {
                                if (!isStackExpanded) {
                                    isStackExpanded = true
                                    scope.launch {
                                        delay(400) // Wait for unstack animation
                                        showStackPreview = true
                                    }
                                }
                            }
                        )
                    } else {
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                }

                /** Withdraw Test button anchored bottom end for better spacing */
                WithdrawTestButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onClick = onWithdrawTestKudosClick,
                )

                /** Bottom row: Transaction History button (ledger icon) */
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val coroutineScope = rememberCoroutineScope()
                    var isHistActive by remember { mutableStateOf(false) }

                    Box(
                        modifier = Modifier
                            .size(historyButtonSize)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                OimColours.TRX_HIST_COLOUR.copy(
                                alpha = if (isHistActive) 0.9f else 0.6f
                            ))
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
                            bitmap = UIIcons("tranx_hist").resourceMapper(),
                            contentDescription = "Transaction History",
                            modifier = Modifier.size(historyIconSize)
                        )
                    }
                }
            }

            // Full-screen preview overlay
            // Full-screen preview overlay
            if (selectedNoteResId != null) {
                NotePreviewOverlay(
                    noteResId = selectedNoteResId!!,
                    onDismiss = { selectedNoteResId = null }
                )

            }

            NotesGalleryOverlay(
                isVisible = showStackPreview,
                onDismiss = {
                    showStackPreview = false
                    scope.launch {
                        delay(200)
                        isStackExpanded = false
                    }
                },
                drawableResIds = amountForNotes?.resourceMapper() ?: emptyList(),
                noteHeight = 115.dp
            )
        }
    }
}

@Composable
private fun WithdrawTestButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var isActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = if (isActive) 0.9f else 0.6f))
            .clickable {
                coroutineScope.launch {
                    isActive = true
                    delay(250)
                    onClick()
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

// ----------------- Banknote stack code -----------------

@Composable
private fun NotesOnTable(
    amount: net.taler.common.Amount,
    maxPerRow: Int = 4,
    dpi : Dp = 72.dp,
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
    onNoteClick: (Int) -> Unit = {},
) {
    // Build a flat list of drawable IDs using greedy mapping (non-stacked by design)
    val drawables = remember(amount) { amount.resourceMapper() }

    // Simple grid: chunk items into rows of up to maxPerRow
    val rows = remember(drawables, maxPerRow) { drawables.chunked(maxPerRow) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(horizontalGap)
            ) {
                row.forEach { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier
                            .width(dpi)
                            .height(dpi)
                            .clickable { onNoteClick(resId) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(verticalGap))
        }
    }
}

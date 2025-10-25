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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import net.taler.common.Amount
import net.taler.wallet.R
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.oim.res_mapping_extensions.Background
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.oim.res_mapping_extensions.SLE_BILLS_CENTS
import net.taler.wallet.systemBarsPaddingBottom
import kotlin.math.min

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
                    val banknoteStacks = selectedBalance?.available?.toWalletStacks().orEmpty()

                    if (banknoteStacks.isNotEmpty()) {
                        WalletNoteStacksRow(
                            stacks = banknoteStacks,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }

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

private const val MAX_VISIBLE_NOTE_LAYERS = 3
private val NOTE_WIDTH = 168.dp
private val NOTE_HEIGHT = 104.dp
private val NOTE_STACK_HORIZONTAL_OFFSET = 14.dp
private val NOTE_STACK_VERTICAL_OFFSET = 12.dp
private val KUDOS_CURRENCIES = setOf("KUDOS", "TESTKUDOS")
private val SLE_DRAWABLE_TO_CENTS = SLE_BILLS_CENTS.associate { (value, drawable) ->
    drawable to value
}

/**
 * Represents a group of identical banknotes that should be rendered as a single stack.
 *
 * @property drawableId drawable resource for the banknote artwork.
 * @property count number of banknotes represented by this stack.
 * @property denominationCents nominal value in cents when available.
 * @property currencyCode original currency code used for fallback labelling.
 * @property isKudos true when the stack is visually rendered as Sierra Leone notes for KUDOS.
 */
private data class BanknoteStack(
    @DrawableRes val drawableId: Int,
    val count: Int,
    val denominationCents: Int?,
    val currencyCode: String,
    val isKudos: Boolean,
)

/**
 * Maps the current balance amount to drawable stacks that can be rendered on the chest screen.
 * Uses the shared `Amount.resourceMapper()` to keep the visual denominations consistent with
 * the payment dialog and other parts of the app.
 */
@Composable
private fun Amount.toWalletStacks(): List<BanknoteStack> {
    // Reuse Amount.resourceMapper() to obtain the greedy denomination breakdown that already drives the payment dialog.
    // For KUDOS currencies we temporarily swap to SLE so we can render with the existing Leone artwork.
    val normalizedCurrency = currency.uppercase(Locale.ROOT)
    val usesKudosArtwork = normalizedCurrency in KUDOS_CURRENCIES
    val artworkAmount = if (usesKudosArtwork) {
        copy(currency = "SLE")
    } else this

    val drawableIds = runCatching {
        artworkAmount.resourceMapper()
    }.getOrElse { emptyList() }

    if (drawableIds.isEmpty()) return emptyList()

    val stacks = mutableListOf<BanknoteStack>()
    drawableIds.forEach { drawableId ->
        val last = stacks.lastOrNull()
        if (last != null && last.drawableId == drawableId) {
            // Consecutive identical drawables belong to the same stack; increase the visible count.
            stacks[stacks.lastIndex] = last.copy(count = last.count + 1)
        } else {
            stacks.add(
                BanknoteStack(
                    drawableId = drawableId,
                    count = 1,
                    denominationCents = SLE_DRAWABLE_TO_CENTS[drawableId],
                    currencyCode = normalizedCurrency,
                    isKudos = usesKudosArtwork,
                )
            )
        }
    }
    return stacks
}

/**
 * Horizontal row that renders each grouped banknote stack.
 *
 * @param stacks pre-grouped banknote stacks produced by [toWalletStacks].
 * @param modifier host modifier to control alignment within the chest layout.
 */
@Composable
private fun WalletNoteStacksRow(
    stacks: List<BanknoteStack>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Bottom,
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        items(
            items = stacks,
            key = { stack -> "${stack.drawableId}_${stack.count}" }
        ) { stack ->
            WalletNoteStack(stack = stack)
        }
    }
}

/**
 * Visualises a single stack of identical banknotes, including a capped overlay to avoid clutter
 * and an optional badge showing how many bills are represented.
 *
 * @param stack banknote grouping information (drawable, count, metadata).
 * @param modifier modifier applied to the stack container.
 */
@Composable
private fun WalletNoteStack(
    stack: BanknoteStack,
    modifier: Modifier = Modifier,
) {
    // Only render the frontmost few layers to avoid clutter but still hint at larger stacks.
    val visibleLayers = min(stack.count, MAX_VISIBLE_NOTE_LAYERS)
    if (visibleLayers <= 0) return

    val totalWidth = NOTE_WIDTH + NOTE_STACK_HORIZONTAL_OFFSET * (visibleLayers - 1)
    val totalHeight = NOTE_HEIGHT + NOTE_STACK_VERTICAL_OFFSET * (visibleLayers - 1)

    Column(
        modifier = modifier.width(totalWidth),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(totalWidth)
                .height(totalHeight),
            contentAlignment = Alignment.TopEnd
        ) {
            for (layerIndex in (visibleLayers - 1) downTo 0) {
                val horizontalOffset = NOTE_STACK_HORIZONTAL_OFFSET * layerIndex
                val verticalOffset = NOTE_STACK_VERTICAL_OFFSET * layerIndex
                Image(
                    painter = painterResource(id = stack.drawableId),
                    contentDescription = stack.contentDescription(),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = -horizontalOffset, y = verticalOffset)
                        .width(NOTE_WIDTH)
                        .height(NOTE_HEIGHT),
                    contentScale = ContentScale.Fit
                )
            }
            if (stack.count > 1) {
                Text(
                    text = "x${stack.count}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = stack.displayLabel(),
            color = Color.White,
            fontSize = 10.sp,
            textAlign = TextAlign.Center
        )
    }
}

/** Builds the label text displayed under the stack based on currency context. */
private fun BanknoteStack.displayLabel(): String {
    return when {
        isKudos && denominationCents != null -> formatKudosLabel(denominationCents * count)
        denominationCents != null -> formatLeoneLabel(denominationCents)
        else -> currencyCode
    }
}

/** Provides an accessibility description summarising the stack. */
private fun BanknoteStack.contentDescription(): String {
    val kind = if (count > 1) "stack" else "note"
    return "${displayLabel()} $kind"
}

/** Formats a Leone denomination using the conventional `Le` prefix. */
private fun formatLeoneLabel(denominationCents: Int): String {
    val major = denominationCents / 100
    val minor = denominationCents % 100
    return if (minor == 0) {
        "Le $major"
    } else {
        String.format(Locale.US, "Le %d.%02d", major, minor)
    }
}

/** Formats a KUDOS amount using a human-readable major/minor representation. */
private fun formatKudosLabel(totalCents: Int): String {
    val major = totalCents / 100
    val minor = totalCents % 100
    return if (minor == 0) {
        "$major KUDOS"
    } else {
        String.format(Locale.US, "%d.%02d KUDOS", major, minor)
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


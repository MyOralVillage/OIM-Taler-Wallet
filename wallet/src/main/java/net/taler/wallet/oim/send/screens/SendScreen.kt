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
package net.taler.wallet.oim.send.screens

/**
 * SEND MODULE – INTERACTIVE BANKNOTE AMOUNT PICKER
 *
 * This file implements `SendScreen`, the main interactive screen for building
 * up a send amount using visual banknotes instead of a plain numeric keypad.
 *
 * USER EXPERIENCE:
 *  - The user taps note thumbnails in a bottom `NotesStrip` to add currency
 *    notes to the table.
 *  - Notes “fly” into animated piles (`NoteFlyer`) in the center of the screen.
 *  - Piles are grouped and consolidated by denomination using `consolidate()`
 *    (e.g., multiple small notes combine into larger ones).
 *  - The total send amount is shown prominently next to the send and undo
 *    controls.
 *
 * MAIN COMPOSABLE:
 *  - SendScreen():
 *      • Displays the current balance in `OimTopBarCentered`.
 *      • Maintains a flat `allAmounts` list backing the visual piles and
 *        consolidated view.
 *      • Computes `displayAmount` from the underlying notes, using helper
 *        `plus` / `minus` functions.
 *      • Shows:
 *          - A red send button (only enabled when amount > 0) that clears
 *            piles and calls `onSend()`.
 *          - An undo button that removes the last-added note and calls
 *            `onRemoveLast()`.
 *          - A dynamic `NotesStrip` that only enables notes affordable with
 *            the remaining balance.
 *
 * INTEGRATION:
 *  - Consumes `Amount` from the database layer and chooses currency-specific
 *    bill sets via `CHF_BILLS`, `XOF_BILLS`, `EUR_BILLS_CENTS`,
 *    `SLE_BILLS_CENTS`, etc.
 *  - Used by the send flow controller (`SendApp`) as the primary amount-entry
 *    step before moving on to purpose selection and QR confirmation.
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import java.math.BigDecimal
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import net.taler.wallet.oim.utils.assets.OimColours
import net.taler.wallet.oim.notes.NoteFlyer
import net.taler.wallet.oim.notes.NotesStrip
import net.taler.wallet.oim.utils.assets.WoodTableBackground
import net.taler.wallet.oim.top_bar.OimTopBarCentered
import net.taler.wallet.oim.utils.res_mappers.CAD_BILLS_CENTS
import net.taler.wallet.oim.utils.res_mappers.EUR_BILLS_CENTS
import net.taler.wallet.oim.utils.res_mappers.SLE_BILLS_CENTS
import net.taler.wallet.oim.utils.res_mappers.UIIcons
import net.taler.wallet.oim.utils.res_mappers.XOF_BILLS
import net.taler.wallet.oim.utils.res_mappers.resourceMapper
import net.taler.wallet.oim.utils.res_mappers.consolidate
import kotlin.random.Random

/**
 * Main screen for sending money.
 *
 * Notes are displayed in horizontal rows, with each denomination getting its own pile.
 * When enough small denominations accumulate, they automatically consolidate into larger ones
 * with a smooth animation (e.g., 5x0.01 SLE → 1x0.05 SLE).
 *
 * @param balance The wallet balance to show in the top bar.
 * @param amount The currently selected sending amount.
 * @param onAdd Callback when a note is added to the pile.
 * @param onRemoveLast Callback when the last note is removed.
 * @param onChoosePurpose Callback to choose a purpose (kept for flow compat).
 * @param onSend Callback when the (red) send area is pressed.
 * @param onHome NOT used right now, kept for API compat.
 * @param onChest Called when the open-chest icon in the top bar is tapped.
 */
@Composable
fun SendScreen(
    balance: Amount,
    amount: Amount,
    onAdd: (Amount) -> Unit,
    onRemoveLast: (Amount) -> Unit,
    onChoosePurpose: () -> Unit,
    onSend: () -> Unit,
    onHome: () -> Unit = {},
    onChest: () -> Unit = {},
) {
    var displayAmount by remember { mutableStateOf(amount) }

    // Store all amounts as a flat list
    val allAmounts = remember { mutableStateListOf<Amount>() }

    // Reset everything when external amount changes (e.g., after send completes)
    LaunchedEffect(amount) {
        displayAmount = amount
        // If amount is reset to 0, clear the piles
        if (BigDecimal(amount.amountStr) == BigDecimal.ZERO) {
            allAmounts.clear()
        }
    }

    // Arithmetic helpers
    fun plus(a: Amount, b: Amount): Amount {
        val cur = a.spec?.name ?: a.currency
        val sum = BigDecimal(a.amountStr) + BigDecimal(b.amountStr)
        return Amount.fromString(cur, sum.stripTrailingZeros().toPlainString())
    }

    fun minus(a: Amount, b: Amount): Amount {
        val cur = a.spec?.name ?: a.currency
        val diff = (
                BigDecimal(a.amountStr) - BigDecimal(b.amountStr)
                ).coerceAtLeast(BigDecimal.ZERO)
        return Amount.fromString(cur, diff.stripTrailingZeros().toPlainString())
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // wood background
        WoodTableBackground(
            modifier = Modifier
                .fillMaxSize(),
            light = false
        )

        // Consolidation animation state
        data class ConsolidationAnim(
            val sourceDenom: String,
            val targetDenom: String,
            val sourcePositions: List<Offset>,
            val targetPosition: Offset,
            val sourceRes: List<Int>,
            val targetRes: Int
        )
        var consolidationAnim by remember { mutableStateOf<ConsolidationAnim?>(null) }

        // Regular note flight state
        data class Pending(val value: Amount, val bmp: Int, val start: Offset, val denomKey: String)
        var pending by remember { mutableStateOf<Pending?>(null) }

        // Derived state: consolidated view of amounts with error handling
        val consolidatedAmounts = remember(allAmounts.size) {
            derivedStateOf {
                if (allAmounts.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        allAmounts.toList().consolidate()
                    } catch (e: Exception) {
                        // Fallback: if consolidation fails, return original amounts
                        allAmounts.toList()
                    }
                }
            }
        }

        val density = LocalDensity.current
        val screenWidth = this@BoxWithConstraints.maxWidth
        val screenHeight = this@BoxWithConstraints.maxHeight

        // Base size for notes
        val baseSizeDp = remember(density, screenWidth, screenHeight) {
            with(density) {
                val minDimension = minOf(screenWidth, screenHeight)
                (minDimension * 0.12f).coerceIn(60.dp, 120.dp)
            }
        }

        // Layout constants
        val pileAreaCenterY = with(density) { (screenHeight * 0.45f).toPx() }
        val pileSpacing = with(density) { 16.dp.toPx() }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // TOP BAR
            OimTopBarCentered(
                balance = balance,
                onChestClick = onChest,
                colour = OimColours.OUTGOING_COLOUR
            )

            Spacer(Modifier.weight(1f))

            // total amount + buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = displayAmount.amountStr,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        fontSize = 60.sp
                    )
                    Text(
                        text = displayAmount.spec?.name ?: displayAmount.currency,
                        color = Color.White,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                }

                Column(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // SEND BUTTON
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .background(
                                color = OimColours.OUTGOING_COLOUR,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                // Only allow send if amount > 0
                                if (BigDecimal(displayAmount.amountStr) > BigDecimal.ZERO) {
                                    // Clear the piles before calling onSend
                                    allAmounts.clear()
                                    onSend()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = UIIcons("send").resourceMapper(),
                            contentDescription = "Send",
                            modifier = Modifier.size(50.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // UNDO BUTTON
                    Button(
                        onClick = {
                            if (allAmounts.isNotEmpty()) {
                                val popped = allAmounts.removeAt(allAmounts.lastIndex)
                                displayAmount = minus(displayAmount, popped)
                                onRemoveLast(popped)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OimColours.INCOMING_COLOUR
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(45.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoneyOff,
                            contentDescription = "Undo",
                            tint = Color.White,
                            modifier = Modifier.size(58.dp)
                        )
                    }
                }
            }

            // figure out currency and denominations
            val currency = displayAmount.spec?.name ?: displayAmount.currency
            val availableDenominations = when (currency) {
                "CAD" -> CAD_BILLS_CENTS
                "XOF" -> XOF_BILLS
                "EUR" -> EUR_BILLS_CENTS
                "SLE", "KUDOS", "KUD" -> SLE_BILLS_CENTS
                else -> emptyList()
            }

            // remaining balance
            val remainingBalance = minus(balance, displayAmount)

            // build thumbnails (resId, Amount)
            val noteThumbnails: List<Pair<Int, Amount>> =
                availableDenominations.map { (denomValue, resId) ->
                    val amountStr = when (currency) {
                        "XOF" -> denomValue.toString()
                        "CAD", "EUR", "SLE", "KUDOS", "KUD" -> {
                            val whole = denomValue / 100
                            val cents = denomValue % 100
                            "$whole.${cents.toString().padStart(2, '0')}"
                        }
                        else -> "0"
                    }
                    resId to Amount.fromString(currency, amountStr)
                }

            // which are affordable
            val affordableNotes = noteThumbnails.map { (_, noteAmount) ->
                val noteValue = BigDecimal(noteAmount.amountStr)
                val remainingValue = BigDecimal(remainingBalance.amountStr)
                noteValue <= remainingValue
            }

            // strip of notes at the bottom
            NotesStrip(
                noteThumbHeight = with(density) {remember(screenHeight) {(screenHeight * 0.2f)}},
                notes = noteThumbnails,
                enabledStates = affordableNotes,
                onAddRequest = { billAmount, startCenter ->
                    val bmp = billAmount.resourceMapper().firstOrNull() ?: return@NotesStrip
                    val denomKey = billAmount.amountStr
                    pending = Pending(billAmount, bmp, startCenter, denomKey)
                },
                onRemoveLast = {
                    if (allAmounts.isNotEmpty()) {
                        val popped = allAmounts.removeAt(allAmounts.lastIndex)
                        displayAmount = minus(displayAmount, popped)
                        onRemoveLast(popped)
                    }
                }
            )
        }

        // Calculate pile positions for consolidated view (left to right, largest to smallest)
        val consolidated = consolidatedAmounts.value
        val sortedDenoms =
            consolidated
            .map { it.amountStr }
            .distinct()
            .sortedByDescending { BigDecimal(it) }

        // Group consolidated amounts by denomination
        val pilesByDenom = consolidated.groupBy { it.amountStr }

        // Helper function to calculate positions for a given set of denominations
        fun calculatePilePositions(denoms: List<String>): Map<String, Offset> {
            val positions = mutableMapOf<String, Offset>()
            if (denoms.isEmpty()) return positions

            val totalWidth = denoms.size * with(density) { baseSizeDp.toPx() } +
                    (denoms.size - 1) * pileSpacing
            val startXPos = with(density) { (screenWidth / 2).toPx() } - totalWidth / 2
            var currentXPos = startXPos

            denoms.forEach { denomKey ->
                val pileWidth = with(density) { baseSizeDp.toPx() }
                positions[denomKey] = Offset(currentXPos + pileWidth / 2, pileAreaCenterY)
                currentXPos += pileWidth + pileSpacing
            }
            return positions
        }

        val pilePositions = calculatePilePositions(sortedDenoms)

        // Regular note flight (when adding a new note)
        pending?.let { p ->
            // Simulate what the consolidated state will look like after adding this note
            val afterAdd = (allAmounts.toList() + p.value).consolidate()
            val afterAddDenoms = afterAdd.map { it.amountStr }.distinct()
                .sortedByDescending { BigDecimal(it) }

            // Find the largest denomination that will exist after consolidation
            // The new note will appear to fly to that largest pile
            val targetDenom = afterAddDenoms.firstOrNull() ?: p.denomKey

            // Calculate positions for the future state
            val futurePositions = calculatePilePositions(afterAddDenoms)
            val targetPos = futurePositions[targetDenom]
                ?: Offset(with(density) { (screenWidth / 2).toPx() }, pileAreaCenterY)

            NoteFlyer(
                noteRes = p.bmp,
                startInRoot = p.start,
                endInRoot = targetPos,
                widthPx = with(density) { baseSizeDp.toPx() },
                onArrive = {
                    // Add to the flat list
                    allAmounts.add(p.value)
                    displayAmount = plus(displayAmount, p.value)
                    onAdd(p.value)
                    pending = null
                    // Consolidation happens automatically via derivedStateOf
                }
            )
        }

        // Render all denomination piles
        sortedDenoms.forEach { denomKey ->
            val notesInPile = pilesByDenom[denomKey] ?: return@forEach
            val position = pilePositions[denomKey] ?: return@forEach

            // Get resource IDs for this pile
            val resourceIds = notesInPile.mapNotNull { amount ->
                amount.resourceMapper().firstOrNull()
            }

            // Render the pile directly at the calculated position
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (position.x - baseSizeDp.toPx() / 2).toDp() },
                        y = with(density) { (position.y - baseSizeDp.toPx() * 0.65f).toDp() }
                    )
                    .width(with(density) { baseSizeDp })
            ) {
                // Draw notes bottom-to-top with random offsets
                resourceIds.forEachIndexed { i, resId ->
                    val rot = remember(denomKey, i) { Random.nextFloat() * 18f - 9f }
                    val dx = remember(denomKey, i) { (Random.nextInt(-10, 10)).dp }
                    val dy = remember(denomKey, i) { (Random.nextInt(-3, 9)).dp }

                    Image(
                        bitmap = ImageBitmap.imageResource(resId),
                        contentDescription = null,
                        modifier = Modifier
                            .offset(dx, dy)
                            .width(with(density) { baseSizeDp })
                            .graphicsLayer {
                                rotationZ = rot
                            },
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }
    }
}

/**
 * Preview: With amount already selected
 * Shows how stacks of different denominations appear side-by-side
 */
@Preview(
    showBackground = true,
    device = "spec:width=920dp,height=460dp,orientation=landscape",
    name = "With Amount Selected"
)
@Composable
fun SendScreenWithAmountPreview() {
    MaterialTheme {
        val mockBalance = Amount.fromString("EUR", "500.00")
        val mockAmount = Amount.fromString("EUR", "0")

        SendScreen(
            balance = mockBalance,
            amount = mockAmount,
            onAdd = { },
            onRemoveLast = { },
            onChoosePurpose = { },
            onSend = { },
            onHome = { },
            onChest = { }
        )
    }
}

@Preview(
    showBackground = true,
    name = " Small Landscape Phone 640x360dp (xhdpi)",
    device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape"
)
@Composable
fun TransactionHistoryPreview_SmallPhoneXhdpi() {
    SendScreenWithAmountPreview()
}
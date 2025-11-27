package net.taler.wallet.oim.send.screens

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.send.components.*
import java.math.BigDecimal
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import net.taler.wallet.oim.OimColours
import net.taler.wallet.oim.OimTopBarCentered
import net.taler.wallet.oim.resourceMappers.CHF_BILLS
import net.taler.wallet.oim.resourceMappers.EUR_BILLS_CENTS
import net.taler.wallet.oim.resourceMappers.SLE_BILLS_CENTS
import net.taler.wallet.oim.resourceMappers.UIIcons
import net.taler.wallet.oim.resourceMappers.XOF_BILLS
import net.taler.wallet.oim.resourceMappers.resourceMapper

/**
 * Main screen for sending money.
 *
 * Integrates amount display, flying notes animation, and note selection.
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
    LaunchedEffect(amount) { displayAmount = amount }

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

        // flying-note state
        data class Pending(val value: Amount, val bmp: Int, val start: Offset, val denomKey: String, val scaleFactor: Float)
        var pending by remember { mutableStateOf<Pending?>(null) }

        // Multiple piles - one per denomination, sorted by value (largest to smallest)
        val pilesByDenom = remember { mutableStateMapOf<String, MutableList<Int>>() }
        val pileAmountsByDenom = remember { mutableStateMapOf<String, MutableList<Amount>>() }
        val denomOrder = remember { mutableStateListOf<String>() }
        val denomScaleFactors = remember { mutableStateMapOf<String, Float>() }

        // Expanded state for click-to-enlarge
        var expandedDenom by remember { mutableStateOf<String?>(null) }

        val density = LocalDensity.current
        val screenWidth = this@BoxWithConstraints.maxWidth
        val screenHeight = this@BoxWithConstraints.maxHeight

        // Base size relative to screen DPI and size
        val baseSizeDp = remember(density, screenWidth, screenHeight) {
            with(density) {
                val screenWidthDp = screenWidth
                val screenHeightDp = screenHeight
                val minDimension = minOf(screenWidthDp, screenHeightDp)
                // Base size is proportional to screen size, typically 15-20% of min dimension
                (minDimension * 0.18f).coerceIn(80.dp, 150.dp)
            }
        }

        // Helper function to sort denominations by value (largest first)
        fun getSortedDenomOrder(): List<String> {
            return denomOrder.sortedByDescending { denomKey ->
                BigDecimal(denomKey)
            }
        }

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

            // total amount + undo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // SEND BUTTON
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .background(
                                color = OimColours.OUTGOING_COLOUR,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSend() },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = UIIcons("send").resourceMapper(),
                            contentDescription = "Send",
                            modifier = Modifier.size(50.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp)) // space between send + undo

                    // UNDO BUTTON
                    Button(
                        onClick = {
                            if (denomOrder.isNotEmpty()) {
                                val lastDenom = denomOrder.last()
                                val amounts = pileAmountsByDenom[lastDenom]
                                val notes = pilesByDenom[lastDenom]

                                if (amounts != null && amounts.isNotEmpty()) {
                                    val popped = amounts.removeAt(amounts.lastIndex)
                                    if (notes != null && notes.isNotEmpty()) {
                                        notes.removeAt(notes.lastIndex)
                                    }

                                    if (amounts.isEmpty()) {
                                        pileAmountsByDenom.remove(lastDenom)
                                        pilesByDenom.remove(lastDenom)
                                        denomOrder.removeAt(denomOrder.lastIndex)
                                        denomScaleFactors.remove(lastDenom)
                                        if (expandedDenom == lastDenom) expandedDenom = null
                                    }

                                    displayAmount = minus(displayAmount, popped)
                                    onRemoveLast(popped)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OimColours.INCOMING_COLOUR
                        ),
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(70.dp)
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
                "CHF" -> CHF_BILLS
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
                        "CHF" -> {
                            val francs = denomValue / 2
                            val half = denomValue % 2
                            if (half == 0) "$francs.00" else "$francs.50"
                        }
                        "XOF" -> denomValue.toString()
                        "EUR", "SLE", "KUDOS", "KUD" -> {
                            val whole = denomValue / 100
                            val cents = denomValue % 100
                            "$whole.${cents.toString().padStart(2, '0')}"
                        }
                        else -> "0"
                    }
                    resId to Amount.fromString(currency, amountStr)
                }

            // Calculate scale factors based on denomination value
            // Lowest denomination = smallest size, highest = largest size
            val denominationValues = noteThumbnails.map { (_, amount) ->
                BigDecimal(amount.amountStr)
            }
            val minDenom = denominationValues.minOrNull() ?: BigDecimal.ONE
            val maxDenom = denominationValues.maxOrNull() ?: BigDecimal.ONE

            val scaleFactors = noteThumbnails.map { (_, amount) ->
                val value = BigDecimal(amount.amountStr)
                if (maxDenom == minDenom) {
                    1.0f
                } else {
                    // Subtle scale from 0.85 to 1.15 based on denomination
                    val normalized = (value - minDenom).toFloat() / (maxDenom - minDenom).toFloat()
                    0.85f + (normalized * 0.3f)
                }
            }

            // which are affordable
            val affordableNotes = noteThumbnails.map { (_, noteAmount) ->
                val noteValue = BigDecimal(noteAmount.amountStr)
                val remainingValue = BigDecimal(remainingBalance.amountStr)
                noteValue <= remainingValue
            }

            // strip of notes at the bottom
            NotesStrip(
                noteThumbHeight = 100.dp,
                notes = noteThumbnails,
                enabledStates = affordableNotes,
                onAddRequest = { billAmount, startCenter ->
                    val bmp = billAmount.resourceMapper().firstOrNull() ?: return@NotesStrip
                    val denomKey = billAmount.amountStr

                    // Find the scale factor for this denomination
                    val index = noteThumbnails.indexOfFirst { it.second.amountStr == denomKey }
                    val scaleFactor = if (index >= 0) scaleFactors[index] else 1.0f

                    if (!denomScaleFactors.containsKey(denomKey)) {
                        denomScaleFactors[denomKey] = scaleFactor
                    }

                    pending = Pending(billAmount, bmp, startCenter, denomKey, scaleFactor)
                },
                onRemoveLast = {
                    if (denomOrder.isNotEmpty()) {
                        val lastDenom = denomOrder.last()
                        val amounts = pileAmountsByDenom[lastDenom]
                        val notes = pilesByDenom[lastDenom]

                        if (amounts != null && amounts.isNotEmpty()) {
                            val popped = amounts.removeAt(amounts.lastIndex)
                            if (notes != null && notes.isNotEmpty()) {
                                notes.removeAt(notes.lastIndex)
                            }

                            if (amounts.isEmpty()) {
                                pileAmountsByDenom.remove(lastDenom)
                                pilesByDenom.remove(lastDenom)
                                denomOrder.removeAt(denomOrder.lastIndex)
                                denomScaleFactors.remove(lastDenom)
                                if (expandedDenom == lastDenom) {
                                    expandedDenom = null
                                }
                            }

                            displayAmount = minus(displayAmount, popped)
                            onRemoveLast(popped)
                        }
                    }
                }
            )
        }

        // actual flying note
        pending?.let { p ->
            // Calculate target position based on sorted order (largest to smallest, left to right)
            val sortedOrder = getSortedDenomOrder()
            val stackIndex = sortedOrder.indexOf(p.denomKey).let {
                if (it == -1) sortedOrder.size else it
            }

            val pileWidthPx = with(density) { (baseSizeDp * p.scaleFactor).toPx() }
            val spacing = with(density) { (baseSizeDp * 0.2f).toPx() }

            // Calculate total width considering scale factors of sorted denominations
            val totalWidth = sortedOrder.sumOf { denom ->
                val scale = denomScaleFactors[denom] ?: 1.0f
                with(density) { (baseSizeDp * scale).toPx() }.toDouble()
            } + spacing * (sortedOrder.size.coerceAtLeast(1) - 1)

            val startX = with(density) { (screenWidth / 2).toPx() } - totalWidth.toFloat() / 2

            // Calculate X position by summing up widths of previous stacks in sorted order
            var targetX = startX
            for (i in 0 until stackIndex) {
                val prevScale = denomScaleFactors[sortedOrder[i]] ?: 1.0f
                targetX += with(density) { (baseSizeDp * prevScale).toPx() }
                targetX += spacing
            }
            targetX += pileWidthPx / 2

            val targetY = with(density) { (screenHeight / 2).toPx() }
            val endPosition = Offset(targetX, targetY)

            NoteFlyer(
                noteRes = p.bmp,
                startInRoot = p.start,
                endInRoot = endPosition,
                widthPx = pileWidthPx,
                onArrive = {
                    // Add to appropriate denomination pile
                    if (!pilesByDenom.containsKey(p.denomKey)) {
                        pilesByDenom[p.denomKey] = mutableListOf()
                        pileAmountsByDenom[p.denomKey] = mutableListOf()
                        denomOrder.add(p.denomKey)
                    }
                    pilesByDenom[p.denomKey]?.add(p.bmp)
                    pileAmountsByDenom[p.denomKey]?.add(p.value)

                    displayAmount = plus(displayAmount, p.value)
                    onAdd(p.value)
                    pending = null
                },
            )
        }

        // Render all piles horizontally (largest to smallest, left to right)
        val sortedOrder = getSortedDenomOrder()
        val spacing = with(density) { (baseSizeDp * 0.2f).toPx() }

        // Calculate total width for centering
        val totalWidth = sortedOrder.sumOf { denom ->
            val scale = denomScaleFactors[denom] ?: 1.0f
            with(density) { (baseSizeDp * scale).toPx() }.toDouble()
        } + spacing * (sortedOrder.size - 1).coerceAtLeast(0)

        val startX = with(density) { (screenWidth / 2).toPx() } - totalWidth.toFloat() / 2
        var currentX = startX

        sortedOrder.forEach { denomKey ->
            val notes = pilesByDenom[denomKey] ?: return@forEach
            val scaleFactor = denomScaleFactors[denomKey] ?: 1.0f
            val pileWidthPx = with(density) { (baseSizeDp * scaleFactor).toPx() }

            // Check if this pile is expanded
            val isExpanded = expandedDenom == denomKey
            val displayScale = if (isExpanded) 2.5f else 1.0f

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { currentX.toDp() },
                        y = with(density) {
                            val baseY = (
                            (screenHeight / 2).toPx()).toDp()
                            - (baseSizeDp * scaleFactor * 0.65f)
                            if (isExpanded) baseY - 100.dp else baseY
                        }
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        expandedDenom = if (isExpanded) null else denomKey
                    }
            ) {
                NotesPile(
                    landedNotes = notes.map { ImageBitmap.imageResource(it) },
                    noteWidthPx = pileWidthPx * displayScale
                )
            }

            currentX += pileWidthPx + spacing
        }
    }
}

/**
 * Preview: Standard Pixel 5 device with EUR currency
 * Shows the initial state with no amount selected
 */
@Preview(
    showBackground = true,
    device = "id:pixel_5",
    name = "Pixel 5 - EUR"
)
@Composable
fun SendScreenPreview() {
    MaterialTheme {
        val mockBalance = Amount.fromString("EUR", "500.00")
        val mockAmount = Amount.fromString("EUR", "0.00")

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

/**
 * Preview: High DPI device with CHF currency
 * Demonstrates how notes scale on high-density screens
 */
@Preview(
    showBackground = true,
    device = "spec:width=411dp,height=891dp,dpi=420",
    name = "High DPI - CHF"
)
@Composable
fun SendScreenPreviewHighDPI() {
    MaterialTheme {
        val mockBalance = Amount.fromString("CHF", "1000.00")
        val mockAmount = Amount.fromString("CHF", "0.00")

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

/**
 * Preview: Low DPI device with KUDOS currency
 * Shows how the interface adapts to lower-resolution screens
 */
@Preview(
    showBackground = true,
    device = "spec:width=320dp,height=640dp,dpi=240",
    name = "Low DPI - KUDOS"
)
@Composable
fun SendScreenPreviewLowDPI() {
    MaterialTheme {
        val mockBalance = Amount.fromString("KUDOS", "250.00")
        val mockAmount = Amount.fromString("KUDOS", "0.00")

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

/**
 * Preview: Tablet-sized device with XOF currency
 * Demonstrates the layout on larger screens
 */
@Preview(
    showBackground = true,
    device = "spec:width=800dp,height=1280dp,dpi=320",
    name = "Tablet - XOF"
)
@Composable
fun SendScreenPreviewTablet() {
    MaterialTheme {
        val mockBalance = Amount.fromString("XOF", "50000")
        val mockAmount = Amount.fromString("XOF", "0")

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

/**
 * Preview: With amount already selected
 * Shows how stacks of different denominations appear side-by-side
 */
@Preview(
    showBackground = true,
    device = "id:pixel_5",
    name = "With Amount Selected"
)
@Composable
fun SendScreenWithAmountPreview() {
    MaterialTheme {
        val mockBalance = Amount.fromString("EUR", "500.00")
        val mockAmount = Amount.fromString("EUR", "87.50")

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
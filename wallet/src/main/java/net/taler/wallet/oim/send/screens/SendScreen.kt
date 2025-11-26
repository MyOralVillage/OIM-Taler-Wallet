package net.taler.wallet.oim.send.screens

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.res_mapping_extensions.*
import net.taler.wallet.oim.send.components.*
import java.math.BigDecimal
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color

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
        data class Pending(val value: Amount, val bmp: Int, val start: Offset)
        var pending by remember { mutableStateOf<Pending?>(null) }

        val pile = remember { mutableStateListOf<Int>() }
        val pileAmounts = remember { mutableStateListOf<Amount>() }

        val density = LocalDensity.current
        val endCenter = with(density) {
            Offset(
                x = (this@BoxWithConstraints.maxWidth / 2).toPx(),
                y = (this@BoxWithConstraints.maxHeight / 2).toPx()
            )
        }

        // the stack of landed notes in the middle
//        val pileWidthPx = with(density) { 160.dp.toPx() }
//        NotesPile(
//            landedNotes = pile.map { ImageBitmap.imageResource(it) },
//            noteWidthPx = pileWidthPx
//        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // TOP BAR — now has clickable chest
            OimTopBarCentered(
                balance = balance,
                onSendClick = onSend,
                onChestClick = onChest
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

                // simple undo button
                Button(
                    onClick = {
                        if (pileAmounts.isNotEmpty()) {
                            val popped = pileAmounts.removeAt(pileAmounts.lastIndex)
                            if (pile.isNotEmpty()) pile.removeAt(pile.lastIndex)
                            displayAmount = minus(displayAmount, popped)
                            onRemoveLast(popped)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                    modifier = Modifier
                        .size(80.dp)
                        .alpha(0.8f)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoneyOff,
                        contentDescription = "Undo",
                        tint = Color.White
                    )
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
                    pending = Pending(billAmount, bmp, startCenter)
                },
                onRemoveLast = {
                    if (pileAmounts.isNotEmpty()) {
                        val popped = pileAmounts.removeAt(pileAmounts.lastIndex)
                        if (pile.isNotEmpty()) pile.removeAt(pile.lastIndex)
                        displayAmount = minus(displayAmount, popped)
                        onRemoveLast(popped)
                    }
                }
            )
        }

        // actual flying note
        pending?.let { p ->
            val widthPx = with(density) { 115.dp.toPx() }
            NoteFlyer(
                noteRes = p.bmp,
                startInRoot = p.start,
                endInRoot = endCenter,
                widthPx = widthPx,
                onArrive = {
                    pile.add(p.bmp)
                    pileAmounts.add(p.value)
                    displayAmount = plus(displayAmount, p.value)
                    onAdd(p.value)
                    pending = null
                },
            )
        }

        val pileWidthPx = with(density) { 115.dp.toPx() }
        NotesPile(
            landedNotes = pile.map { ImageBitmap.imageResource(it) },
            noteWidthPx = pileWidthPx
        )

    }
}
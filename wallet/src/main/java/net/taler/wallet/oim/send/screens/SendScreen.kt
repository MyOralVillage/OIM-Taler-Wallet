package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.res_mapping_extensions.*
import net.taler.wallet.oim.send.components.*
import java.math.BigDecimal
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex


/**
 * Main screen for sending money.
 *
 * Integrates amount display, flying notes animation, and note selection.
 *
 * @param balance The wallet balance to show in the top bar.
 * @param amount The currently selected sending amount.
 * @param onAdd Callback when a note is added to the pile.
 * @param onRemoveLast Callback when the last note is removed.
 * @param onChoosePurpose Callback to choose a purpose (currently placeholder).
 * @param onSend Callback when the Send button is pressed.
 * @param onHome Callback for home button press.
 */
@Composable
fun SendScreen(
    balance: Amount,
    amount: Amount,
    onAdd: (Amount) -> Unit,
    onRemoveLast: (Amount) -> Unit,
    onChoosePurpose: () -> Unit,
    onSend: () -> Unit,
    onHome: () -> Unit = {}
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
        val diff = (BigDecimal(a.amountStr) - BigDecimal(b.amountStr)).coerceAtLeast(BigDecimal.ZERO)
        return Amount.fromString(cur, diff.stripTrailingZeros().toPlainString())
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        WoodTableBackground(modifier = Modifier.fillMaxSize(), light = false)


        // Flying note state
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

        val pileWidthPx = with(density) { 160.dp.toPx() }
        NotesPile(
            landedNotes = pile.map { ImageBitmap.imageResource(it) },
            noteWidthPx = pileWidthPx
        )

        // After the Home button in SendScreen
        IconButton(
            onClick = onHome,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .size(60.dp)
                .zIndex(2f)
        ) {
            Icon(
                Icons.Filled.House,
                contentDescription = "Home",
                tint = Color.White,
                modifier = Modifier.size(120.dp),
            )
        }

// Add Undo button in top-right
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
                .align(Alignment.CenterEnd)
                .padding(8.dp)
                .size(100.dp)
                .alpha(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.MoneyOff,
                contentDescription = "Undo",
                tint = Color.White,
                modifier = Modifier.size(100.dp)
            )
            Spacer(Modifier.width(8.dp))
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OimTopBarCentered(balance = balance, onSendClick = onSend)
            Spacer(Modifier.weight(1f))

            // Display total amount + send
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = displayAmount.amountStr,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 64.sp
                    )
                    Text(
                        text = displayAmount.spec?.name ?: displayAmount.currency,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp
                    )
                }

            }


            // Get available denominations based on currency
            val currency = displayAmount.spec?.name ?: displayAmount.currency
            val availableDenominations = when (currency) {
                "CHF" -> CHF_BILLS
                "XOF" -> XOF_BILLS
                "EUR" -> EUR_BILLS_CENTS
                "SLE", "KUDOS", "KUD" -> SLE_BILLS_CENTS
                else -> emptyList()
            }

            // Calculate remaining balance
            val remainingBalance = minus(balance, displayAmount)

            // Create note thumbnails with all denominations
            val noteThumbnails: List<Pair<Int, Amount>> = availableDenominations.map {
                (denomValue, resId) ->
                val amountStr = when (currency) {
                    "CHF" -> {
                        val francs = denomValue / 2
                        val halfFrancs = denomValue % 2
                        if (halfFrancs == 0) "$francs.00" else "$francs.50"
                    }
                    "XOF" -> denomValue.toString()
                    "EUR", "SLE", "KUDOS", "KUD" -> {
                        val whole = denomValue / 100
                        val cents = denomValue % 100
                        "$whole.${cents.toString().padStart(2, '0')}"
                    }
                    else -> "0"
                }
                val noteAmount = Amount.fromString(currency, amountStr)
                resId to noteAmount
            }

            // Check which notes are affordable
            val affordableNotes = noteThumbnails.map { (resId, noteAmount) ->
                val noteValue = BigDecimal(noteAmount.amountStr)
                val remainingValue = BigDecimal(remainingBalance.amountStr)
                noteValue <= remainingValue
            }

            NotesStrip(
                noteThumbWidth = 160.dp,
                notes = noteThumbnails,
                enabledStates = affordableNotes,
                onAddRequest = { billAmount, startCenter ->
                    val bmp = billAmount.resourceMapper().firstOrNull() ?: return@NotesStrip
                    pending = Pending(billAmount, bmp, startCenter)
                },
                onRemoveLast = {
                    if (pileAmounts.isNotEmpty( )) {
                        val popped = pileAmounts.removeAt(pileAmounts.lastIndex)
                        if (pile.isNotEmpty()) pile.removeAt(pile.lastIndex)
                        displayAmount = minus(displayAmount, popped)
                        onRemoveLast(popped)
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
        }

        // Flying note animation
        pending?.let { p ->
            val widthPx = with(density) { 160.dp.toPx() }
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
    }
}

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun SendScreenPreview() {
    MaterialTheme {
        SendScreen(
            balance = Amount.fromString("KUDOS", "100"),  // Wallet has 100
            amount = Amount.fromString("KUDOS", "0"),     // Starting with 0 selected
            onAdd = {},
            onRemoveLast = {},
            onChoosePurpose = {},
            onSend = {},
            onHome = {}
        )
    }
}
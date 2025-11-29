@file:OptIn(InternalSerializationApi::class)

package net.taler.wallet.oim.history.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.serialization.InternalSerializationApi
import net.taler.common.Amount as CommonAmount
import net.taler.database.data_models.*
import net.taler.wallet.oim.utils.res_mappers.Tile
import kotlin.math.*

/**
 * River scene canvas that displays transactions as a flowing river.
 * River thickness changes based on transaction amounts.
 * Incoming transactions show as farm areas, outgoing as lakes.
 */
@Composable
fun RiverSceneCanvas(
    transactions: List<Tranx>,
    onTransactionClick: (Tranx) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Textures
    val farmRes = Tile("farm").resourceMapper()
    val blankRes = Tile("blank").resourceMapper()
    val riverRes = Tile("river").resourceMapper()
    val lakeRes = Tile("lake").resourceMapper()

    val farmBitmap = ImageBitmap.imageResource(farmRes)
    val blankBitmap = ImageBitmap.imageResource(blankRes)
    val riverBitmap = ImageBitmap.imageResource(riverRes)
    val lakeBitmap = ImageBitmap.imageResource(lakeRes)

    val n = max(transactions.size, 1)
    val perTxnWidth = 180.dp
    val minCanvasWidth = 1000.dp
    val desiredWidth = perTxnWidth * n
    val canvasWidth = if (desiredWidth < minCanvasWidth) minCanvasWidth else desiredWidth

    // Store hit areas - needs to be remembered across recompositions
    val hitAreas = remember(transactions.size) { mutableStateListOf<Pair<Rect, Tranx>>() }

    Box(
        modifier = modifier
            .horizontalScroll(scrollState)
            .pointerInput(transactions) {
                detectTapGestures { offset ->
                    // Find which transaction was tapped
                    hitAreas.firstOrNull { (rect, _) ->
                        rect.contains(offset)
                    }?.let { (_, transaction) ->
                        onTransactionClick(transaction)
                    }
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .width(canvasWidth)
                .fillMaxHeight()
        ) {
            hitAreas.clear()

            val w = size.width
            val h = size.height

            // Reserve space at top for dates
            val dateStripHeight = 40.dp.toPx()

            val landBottom = h * 0.58f
            val riverBaseline = landBottom
            val yellowTop = landBottom
            val yellowBottom = h

            // ---------- BACKGROUNDS ----------
            // Farm tiled background on top
            drawFarm(
                image = farmBitmap,
                rect = Rect(0f, dateStripHeight, w, landBottom)
            )
            // Blank background on bottom
            drawFarm(
                image = blankBitmap,
                rect = Rect(0f, yellowTop, w, yellowBottom)
            )

            val nTx = transactions.size
            if (nTx == 0) return@Canvas

            // ---------- RIVER THICKNESS LOGIC ----------
            val maxSingleMaj = transactions.maxOfOrNull {
                val maj = it.amount.value.toDouble() +
                        it.amount.fraction.toDouble() / 100_000_000.0
                abs(maj)
            }?.takeIf { it > 0.0 } ?: 1.0

            val minTh = h * 0.028f
            val maxTh = h * 0.11f
            var currentTh = h * 0.055f
            val thicknessAtPoints = mutableListOf<Float>()
            thicknessAtPoints += currentTh

            transactions.forEach { t ->
                val maj = t.amount.value.toDouble() +
                        t.amount.fraction.toDouble() / 100_000_000.0
                val proportion = (abs(maj) / maxSingleMaj).toFloat()
                val delta = (maxTh - minTh) * 0.6f * proportion

                currentTh = if (t.direction.getValue()) {
                    (currentTh + delta)
                } else {
                    (currentTh - delta)
                }.coerceIn(minTh, maxTh)

                thicknessAtPoints += currentTh
            }

            val leftPad = 0f
            val usableW = w - leftPad
            val step = usableW / nTx

            // ---------- RIVER PATH ----------
            val topPath = Path()
            val bottomPath = Path()
            for (i in 0..nTx) {
                val x = leftPad + i * step
                val thisTh = thicknessAtPoints[i]
                val wave = sin(i / max(1f, nTx.toFloat()) * 5f) * (h * 0.01f)
                val topY = riverBaseline - thisTh + wave
                val bottomY = riverBaseline + wave

                if (i == 0) {
                    topPath.moveTo(x, topY)
                    bottomPath.moveTo(x, bottomY)
                } else {
                    topPath.lineTo(x, topY)
                    bottomPath.lineTo(x, bottomY)
                }
            }

            val river = Path().apply {
                addPath(topPath)
                for (i in nTx downTo 0) {
                    val x = leftPad + i * step
                    val wave = sin(i / max(1f, nTx.toFloat()) * 5f) * (h * 0.01f)
                    val bottomY = riverBaseline + wave
                    lineTo(x, bottomY)
                }
                close()
            }

            // Fill river with river texture
            clipPath(river) {
                drawFarm(
                    image = riverBitmap,
                    rect = Rect(0f, 0f, w, h)
                )
            }
            drawPath(river, color = Color(0xFF005188), style = Stroke(2.dp.toPx()))

            // ---------- CLICKABLE FARMS (INCOMING) & LAKES (OUTGOING) ----------
            transactions.forEachIndexed { idx, t ->
                val x = leftPad + idx * step + step / 2f

                if (t.direction.getValue()) {
                    // INCOMING = CLICKABLE FARM AREA
                    val farmWidth = step * 0.8f
                    val farmHeight = (landBottom - dateStripHeight) * 0.7f
                    val farmTop = landBottom - farmHeight

                    val farmRect = Rect(
                        left = x - farmWidth / 2f,
                        top = farmTop,
                        right = x + farmWidth / 2f,
                        bottom = landBottom
                    )

                    // REMOVE ALL OVERLAYS — JUST HIT AREA
                    hitAreas += farmRect to t
                } else {
                    // OUTGOING = CLICKABLE LAKE
                    val lakeHeight = h * 0.35f
                    val lakeWidth = lakeHeight * 1.3f
                    val lakeTop = riverBaseline

                    val lakeRect = Rect(
                        left = x - lakeWidth / 2f,
                        top = lakeTop,
                        right = x + lakeWidth / 2f,
                        bottom = (lakeTop + lakeHeight).coerceAtMost(h)
                    )

                    drawLake(image = lakeBitmap, rect = lakeRect)
                    hitAreas += lakeRect to t
                }
            }
        }
    }
}

private fun DrawScope.drawFarm(
    image: ImageBitmap,
    rect: Rect
) {
    val iw = image.width.toFloat()
    val ih = image.height.toFloat()

    var y = rect.top
    while (y < rect.bottom) {
        var x = rect.left
        while (x < rect.right) {
            drawImage(
                image = image,
                srcSize = IntSize(image.width, image.height),
                dstOffset = IntOffset(x.toInt(), y.toInt())
            )
            x += iw
        }
        y += ih
    }
}

private fun DrawScope.drawLake(
    image: ImageBitmap,
    rect: Rect
) {
    val iw = image.width.toFloat()
    val ih = image.height.toFloat()
    val rw = rect.width
    val rh = rect.height

    val scale = min(rw / iw, rh / ih)

    val dw = iw * scale
    val dh = ih * scale

    val left = rect.left + (rw - dw) / 2f
    val top = rect.top + (rh - dh) / 2f

    drawImage(
        image = image,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(left.toInt(), top.toInt()),
        dstSize = IntSize(dw.toInt(), dh.toInt())
    )
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "River Canvas - Multiple Transactions",
    device = "spec:width=920dp,height=460dp,orientation=landscape"
)
@Composable
fun RiverCanvasPreview() {
    MaterialTheme {
        val sameDate = FDtm()

        val fakeTranx = listOf(
            Tranx(
                amount = CommonAmount("XOF", 10000L, 0),
                datetime = sameDate,
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "1"
            ),
            Tranx(
                amount = Amount("EUR", 5000L, 0),
                datetime = sameDate,
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "2"
            ),
            Tranx(
                amount = Amount("SLE", 15000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "3"
            ),
            Tranx(
                amount = Amount("XOF", 8000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "4"
            ),
            Tranx(
                amount = Amount("SLE", 20000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "5"
            )
        )

        RiverSceneCanvas(
            transactions = fakeTranx,
            onTransactionClick = {}
        )
    }
}
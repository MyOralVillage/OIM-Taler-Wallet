@file:OptIn(InternalSerializationApi::class)

package net.taler.wallet.oim.history.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.serialization.InternalSerializationApi
import net.taler.common.Amount as CommonAmount
import net.taler.database.data_models.*
import net.taler.wallet.oim.utils.res_mappers.Tile
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun RiverSceneCanvas(
    transactions: List<Tranx>,
    dates: List<String>,
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

    val hitAreas = remember(transactions.size) { mutableStateListOf<Pair<Rect, Tranx>>() }

    Box(
        modifier = modifier
            .horizontalScroll(scrollState)
            .pointerInput(transactions) {
                detectTapGestures { offset ->
                    hitAreas.firstOrNull { (rect, _) -> rect.contains(offset) }
                        ?.let { (_, t) -> onTransactionClick(t) }
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

            // Date strip
            val dateStripHeight = 40.dp.toPx()

            val landBottom = h * 0.58f
            val riverBaseline = landBottom
            val yellowTop = landBottom
            val yellowBottom = h

            // bottom soil
            drawFarmTiled(
                image = blankBitmap,
                rect = Rect(0f, yellowTop, w, yellowBottom)
            )

            val nTx = transactions.size
            if (nTx == 0) return@Canvas

            // ---------- thickness for river ----------
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
                    currentTh + delta
                } else {
                    currentTh - delta
                }.coerceIn(minTh, maxTh)

                thicknessAtPoints += currentTh
            }

            val leftPad = 0f
            val usableW = w - leftPad
            val step = usableW / nTx

            // ---------- text paint ----------
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 12.sp.toPx()
                color = android.graphics.Color.WHITE
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            val maxMajForWidth = maxSingleMaj
            val minFarmWidth = step * 0.4f
            val maxFarmWidth = step * 0.95f
            val farmHeight = (landBottom - dateStripHeight) * 0.7f  // CONSTANT for all farms

            // ---------- FARMS FIRST (so river draws on top) ----------
            transactions.forEachIndexed { idx, t ->
                val x = leftPad + idx * step + step / 2f
                val maj = t.amount.value.toDouble() +
                        t.amount.fraction.toDouble() / 100_000_000.0

                val raw = dates.getOrNull(idx)
                val labelText = raw?.takeIf { it.length >= 10 }?.let { ds ->
                    // yyyy-MM-dd
                    val year = ds.substring(0, 4)
                    val month = ds.substring(5, 7)
                    val day = ds.substring(8, 10)
                    "☀️ $day / 🌙 $month / ⭐ $year"
                } ?: ""

                // width based on amount
                val amountRatio = (abs(maj) / maxMajForWidth)
                    .toFloat()
                    .coerceIn(0f, 1f)

                var farmWidth = minFarmWidth +
                        (maxFarmWidth - minFarmWidth) * amountRatio

                if (labelText.isNotEmpty()) {
                    val textWidth = textPaint.measureText(labelText)
                    val minWidthForText = textWidth + 16.dp.toPx()
                    farmWidth = max(farmWidth, minWidthForText)
                }
                farmWidth = farmWidth.coerceIn(minFarmWidth, maxFarmWidth)

                val farmTop = landBottom - farmHeight
                val farmRect = Rect(
                    left = x - farmWidth / 2f,
                    top = farmTop,
                    right = x + farmWidth / 2f,
                    bottom = landBottom
                )

                // stretch farm texture to full rect so all heights match visually
                drawImageStretchToRect(
                    image = farmBitmap,
                    rect = farmRect
                )

                // date right above farm
                if (labelText.isNotEmpty()) {
                    val textY = (farmTop - 6.dp.toPx())
                        .coerceAtLeast(12.sp.toPx())
                    drawContext.canvas.nativeCanvas.drawText(
                        labelText,
                        x,
                        textY,
                        textPaint
                    )
                }

                // entire farm clickable
                hitAreas += farmRect to t
            }

            // ---------- RIVER ON TOP OF FARMS ----------
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

            clipPath(river) {
                drawFarmTiled(
                    image = riverBitmap,
                    rect = Rect(0f, 0f, w, h)
                )
            }
            drawPath(river, color = Color(0xFF005188), style = Stroke(2.dp.toPx()))

            // ---------- LAKES (drawn after river, same as before) ----------
            transactions.forEachIndexed { idx, t ->
                if (!t.direction.getValue()) {
                    val x = leftPad + idx * step + step / 2f

                    val lakeHeight = h * 0.35f
                    val lakeWidth = lakeHeight * 1.3f
                    val lakeTop = riverBaseline
                    val lakeBottom = (lakeTop + lakeHeight).coerceAtMost(h)

                    val lakeRect = Rect(
                        left = x - lakeWidth / 2f,
                        top = lakeTop,
                        right = x + lakeWidth / 2f,
                        bottom = lakeBottom
                    )

                    drawLake(image = lakeBitmap, rect = lakeRect)

                    // lake clickable too
                    hitAreas += lakeRect to t
                }
            }
        }
    }
}

// helpers ----------------------------------------------------------

private fun DrawScope.drawFarmTiled(
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

/** Stretch the image to exactly fill the rect (constant farm height visually). */
private fun DrawScope.drawImageStretchToRect(
    image: ImageBitmap,
    rect: Rect
) {
    drawImage(
        image = image,
        srcSize = IntSize(image.width, image.height),
        dstOffset = IntOffset(rect.left.toInt(), rect.top.toInt()),
        dstSize = IntSize(rect.width.toInt(), rect.height.toInt())
    )
}

private fun DrawScope.drawLake(
    image: ImageBitmap,
    rect: Rect
) {
    // keep lakes aspect-ratio–correct
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
// preview -----------------------------------------------------------

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

        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateStrings = fakeTranx.map { it.datetime.fmtString(formatter) }

        RiverSceneCanvas(
            transactions = fakeTranx,
            dates = dateStrings,
            onTransactionClick = {}
        )
    }
}

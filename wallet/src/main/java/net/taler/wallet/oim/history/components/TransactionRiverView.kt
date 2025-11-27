@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    kotlinx.serialization.InternalSerializationApi::class
)
/*
 * This file is part of GNU Taler
 * (C) 2025 Taler Systems S.A.
 * GPLv3-or-later
 */

package net.taler.wallet.oim.history.components

import android.graphics.Paint
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.common.Amount as CommonAmount
import net.taler.database.TranxHistory
import net.taler.database.data_models.Tranx
import net.taler.wallet.BuildConfig
import net.taler.wallet.oim.utils.resourceMappers.Tile
import net.taler.wallet.oim.utils.resourceMappers.UIIcons
import net.taler.wallet.oim.utils.resourceMappers.resourceMapper
import net.taler.wallet.oim.send.components.NotesGalleryOverlay
import net.taler.wallet.oim.send.components.StackedNotes
import net.taler.wallet.oim.send.components.WoodTableBackground
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Composable
fun TransactionHistoryView(
    modifier: Modifier = Modifier,
    onHome: () -> Unit = {},
    balanceAmount: net.taler.common.Amount? = null,
    onSendClick: () -> Unit = {},
    onReceiveClick: () -> Unit = {},
) {
    var showRiver by rememberSaveable { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        if (showRiver) {
            OimRiverTransactionsView(
                modifier = Modifier.fillMaxSize(),
                balanceAmount = balanceAmount,
                onSendClick = onSendClick,
                onReceiveClick = onReceiveClick,
            )
        } else {
            TransactionsListView()
        }

        if (showRiver) {
            FloatingActionButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp),
                containerColor = Color.White,
                contentColor = Color.White,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    bitmap = UIIcons("chest_open").resourceMapper(),
                    contentDescription = "Back to OIM home",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(52.dp)   // bigger chest
                )
            }
        }

        FloatingActionButton(
            onClick = { showRiver = !showRiver },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color.White,
            contentColor = Color(0xFF0376C4),
            shape = MaterialTheme.shapes.large
        ) {
            if (showRiver) {
                Icon(
                    Icons.Default.List,
                    contentDescription = "Show list",
                    modifier = Modifier.size(32.dp) // bigger list icon
                )
            } else {
                Icon(
                    Icons.Default.Water,
                    contentDescription = "Show river",
                    modifier = Modifier.size(32.dp) // bigger water icon
                )
            }
        }
    }
}

@Composable
fun OimRiverTransactionsView(
    modifier: Modifier = Modifier,
    transactions: List<Tranx>? = null,
    balanceAmount: net.taler.common.Amount? = null,
    onSendClick: () -> Unit = {},
    onReceiveClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var txns by remember { mutableStateOf(transactions ?: emptyList()) }

    LaunchedEffect(transactions) {
        if (transactions == null) {
            if (BuildConfig.DEBUG) TranxHistory.initTest(context) else TranxHistory.init(context)
            txns = TranxHistory.getHistory()
        }
    }

    var selected by remember { mutableStateOf<Tranx?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var isStackExpanded by remember { mutableStateOf(false) }
    var showStackPreview by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        WoodTableBackground(
            modifier = Modifier.fillMaxSize(),
            light = false
        )

        Column(
            Modifier
                .fillMaxSize()
                .padding(top = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                RiverSceneCanvasPerEvent(
                    transactions = txns,
                    onTransactionClick = {
                        selected = it
                        scope.launch { sheetState.show() }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(
                            start = 120.dp,
                            end = 120.dp,
                            top = 12.dp,
                            bottom = 12.dp
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .padding(start = 8.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HistorySideButton(
                        bitmap = UIIcons("receive").resourceMapper(),
                        bgColor = Color(0xFF4CAF50),
                        onClick = onReceiveClick
                    )
                    HistorySideButton(
                        bitmap = UIIcons("send").resourceMapper(),
                        bgColor = Color(0xFFC32909),
                        onClick = onSendClick
                    )
                }

                if (balanceAmount != null) {
                    BalanceStackedNotes(
                        amount = balanceAmount,
                        isStackExpanded = isStackExpanded,
                        onExpand = {
                            if (!isStackExpanded) {
                                isStackExpanded = true
                                scope.launch {
                                    delay(400)
                                    showStackPreview = true
                                }
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = 12.dp)
                    )
                }
            }
        }

        if (txns.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet", color = Color.White)
            }
        }
    }

    if (balanceAmount != null) {
        val noteResIds = remember(balanceAmount) { balanceAmount.resourceMapper() }
        NotesGalleryOverlay(
            isVisible = showStackPreview,
            onDismiss = {
                showStackPreview = false
                scope.launch {
                    delay(200)
                    isStackExpanded = false
                }
            },
            drawableResIds = noteResIds,
            noteHeight = 115.dp
        )
    }

    if (selected != null) {
        val t = selected!!
        ModalBottomSheet(
            onDismissRequest = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { selected = null }
            },
            sheetState = sheetState
        ) {
            val dateStr = t.datetime.fmtString(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            TransactionCard(
                amount = t.amount.toString(false),
                currency = t.amount.currency,
                date = dateStr,
                purpose = t.purpose,
                dir = t.direction,
                displayAmount = t.amount,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )
        }
    }
}

/** Small square button used on the left of the river (history screen only). */
@Composable
private fun HistorySideButton(
    bitmap: ImageBitmap,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(18.dp))
            .background(bgColor.copy(alpha = 0.9f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = null,
            modifier = Modifier.size(size * 0.7f)
        )
    }
}

@Composable
private fun BalanceNotes(
    amount: net.taler.common.Amount,
    modifier: Modifier = Modifier,
    maxPerRow: Int = 3,
) {
    val drawables = remember(amount) { amount.resourceMapper() }
    val rows = remember(drawables, maxPerRow) { drawables.chunked(maxPerRow) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEachIndexed { idx, row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { resId ->
                    Image(
                        painter = painterResource(id = resId),
                        contentDescription = null,
                        modifier = Modifier
                            .width(56.dp)
                            .height(56.dp)
                    )
                }
            }
            if (idx != rows.lastIndex) {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun RiverSceneCanvasPerEvent(
    transactions: List<Tranx>,
    onTransactionClick: (Tranx) -> Unit,
    modifier: Modifier = Modifier
) {
    val hitRects = remember { mutableStateListOf<Pair<Rect, Tranx>>() }
    val scrollState = rememberScrollState()

    val farmRes  = Tile("farm").resourceMapper()
    val blankRes = Tile("blank").resourceMapper()
    val riverRes = Tile("river").resourceMapper()
    val lakeRes  = Tile("lake").resourceMapper()

    val farmBitmap  = ImageBitmap.imageResource(farmRes)
    val blankBitmap = ImageBitmap.imageResource(blankRes)
    val riverBitmap = ImageBitmap.imageResource(riverRes)
    val lakeBitmap  = ImageBitmap.imageResource(lakeRes)

    // layout constants (dp -> will be converted inside canvas)
    val minFarmWidthDp = 110.dp          // still wide enough for date, but allows more variation
    val farmSpacingDp = 24.dp
    val perTxnWidthBase = 170.dp
    val perTxnWidth = max(perTxnWidthBase, minFarmWidthDp + farmSpacingDp)

    Row(
        modifier = modifier.horizontalScroll(scrollState)
    ) {
        val n = max(transactions.size, 1)
        val minCanvasWidth = 1000.dp
        val desiredWidth = perTxnWidth * n
        val canvasWidth =
            if (desiredWidth < minCanvasWidth) minCanvasWidth else desiredWidth

        Canvas(
            modifier = Modifier
                .width(canvasWidth)
                .fillMaxHeight()
                .pointerInput(transactions) {
                    detectTapGestures { offset ->
                        hitRects.firstOrNull { it.first.contains(offset) }
                            ?.let { onTransactionClick(it.second) }
                    }
                }
        ) {
            hitRects.clear()

            val w = size.width
            val h = size.height

            // strip at the very top reserved for dates
            val dateStripHeightPx = 32.dp.toPx()

            // ----- RIVER GEOMETRY -----
            val riverBaseline = h * 0.55f       // bottom of the river
            val landBottom = riverBaseline
            val yellowTop = riverBaseline
            val yellowBottom = h

            // backgrounds
            drawTiledImageInRect(
                image = blankBitmap,
                rect = Rect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = landBottom
                )

            )
            drawTiledImageInRect(
                image = blankBitmap,
                rect = Rect(0f, yellowTop, w, yellowBottom)
            )

            val nTx = transactions.size
            if (nTx == 0) return@Canvas

            // ----- AMOUNT PROPORTIONS -----
            val maxSingleMaj = transactions
                .map {
                    val maj = it.amount.value.toDouble() +
                            it.amount.fraction.toDouble() / 100_000_000.0
                    abs(maj)
                }
                .maxOrNull()
                ?.takeIf { it > 0.0 } ?: 1.0

            val amountProportions = transactions.map { t ->
                val maj = t.amount.value.toDouble() +
                        t.amount.fraction.toDouble() / 100_000_000.0
                (abs(maj) / maxSingleMaj).toFloat()
            }

            // ----- RIVER THICKNESS (dynamic) -----
            val minTh = h * 0.03f
            val maxTh = h * 0.12f
            var currentTh = h * 0.06f
            val thicknessAtPoints = mutableListOf<Float>()
            thicknessAtPoints += currentTh

            transactions.forEachIndexed { idx, t ->
                val proportion = amountProportions[idx]
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
            val step = if (nTx > 0) usableW / nTx else 0f

            val minFarmWidthPx = minFarmWidthDp.toPx()
            val farmSpacingPx = farmSpacingDp.toPx()

            // ----- FARMS: same height, variable width, with spacing -----
            transactions.forEachIndexed { idx, t ->
                val xCenter = leftPad + idx * step + step / 2f
                val proportion = amountProportions[idx]

                val farmTop = dateStripHeightPx
                val farmHeight = landBottom - farmTop

                val minWidthFactor = 0.6f
                val maxWidthFactor = 1.7f
                val widthFactor = (minWidthFactor +
                        (maxWidthFactor - minWidthFactor) * proportion)
                    .coerceIn(minWidthFactor, maxWidthFactor)

                var farmWidth = step * widthFactor
                farmWidth = max(farmWidth, minFarmWidthPx)

                val maxAllowedWidth = max(step - farmSpacingPx, minFarmWidthPx)
                farmWidth = min(farmWidth, maxAllowedWidth)

                val farmRect = Rect(
                    left = xCenter - farmWidth / 2f,
                    top = farmTop,
                    right = xCenter + farmWidth / 2f,
                    bottom = farmTop + farmHeight
                )

                drawImageStretchInRect(
                    image = farmBitmap,
                    rect = farmRect
                )

                hitRects += farmRect to t

                val baseDate = t.datetime.fmtString(
                    DateTimeFormatter.ofPattern("yyyy/MM/dd")
                )
                val parts = baseDate.split("/")
                val year = parts.getOrNull(0) ?: "----"
                val month = parts.getOrNull(1) ?: "--"
                val day = parts.getOrNull(2) ?: "--"
                val dateStr = "☀️ $day / 🌙 $month / ⭐ $year"

                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = AndroidColor.WHITE
                        textSize = 14.dp.toPx()
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                    }

                    // baseline a bit above the farm top
                    val textY = dateStripHeightPx - 6.dp.toPx()
                    canvas.nativeCanvas.drawText(
                        dateStr,
                        xCenter,
                        textY,
                        paint
                    )
                }
            }

            // ----- RIVER PATH: dynamic thickness, flat bottom -----
            val topPath = Path()
            val bottomPath = Path()

            for (i in 0..nTx) {
                val x = leftPad + i * step
                val thisTh = thicknessAtPoints[i]
                val topY = riverBaseline - thisTh
                val bottomY = riverBaseline

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
                    val bottomY = riverBaseline
                    lineTo(x, bottomY)
                }
                close()
            }

            clipPath(river) {
                drawTiledImageInRect(
                    image = riverBitmap,
                    rect = Rect(0f, 0f, w, h)
                )
            }
            drawPath(river, color = Color(0xFF005188), style = Stroke(2.dp.toPx()))

            // ----- LAKES: big, attached to bottom of river -----
            transactions.forEachIndexed { idx, t ->
                if (!t.direction.getValue()) {
                    val xCenter = leftPad + idx * step + step / 2f
                    val proportion = amountProportions[idx]

                    val baseLakeHeight = h * 0.34f
                    val minLakeFactor = 0.9f
                    val maxLakeFactor = 1.5f
                    val lakeFactor = (minLakeFactor +
                            (maxLakeFactor - minLakeFactor) * proportion)
                        .coerceIn(minLakeFactor, maxLakeFactor)

                    val lakeHeight = baseLakeHeight * lakeFactor
                    val lakeWidth = lakeHeight * 1.35f

                    val lakeTop = riverBaseline

                    val lakeRect = Rect(
                        left = xCenter - lakeWidth / 2f,
                        top = lakeTop,
                        right = xCenter + lakeWidth / 2f,
                        bottom = lakeTop + lakeHeight
                    )

                    drawImageFitInRect(
                        image = lakeBitmap,
                        rect = lakeRect
                    )

                    hitRects += lakeRect to t
                }
            }
        }
    }
}

@Composable
private fun BalanceStackedNotes(
    amount: net.taler.common.Amount,
    isStackExpanded: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val noteResIds = remember(amount) { amount.resourceMapper() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        StackedNotes(
            noteResIds = noteResIds,
            noteHeight = 60.dp,
            noteWidth = 86.dp,
            expanded = isStackExpanded,
            onClick = onExpand,
            notesPerRow = 2,
            stacksPerRow = 2
        )
    }
}

@Composable
private fun SideOimActionButton(
    iconName: String,
    contentDescription: String,
    backgroundColor: Color,
    onClick: () -> Unit,
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(64.dp),
        containerColor = backgroundColor,
        contentColor = Color.White,
        shape = MaterialTheme.shapes.large
    ) {
        Icon(
            bitmap = UIIcons(iconName).resourceMapper(),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier.size(40.dp)
        )
    }
}

@Composable
private fun NotesOnTable(
    amount: CommonAmount,
    maxPerRow: Int = 4,
    dpi: Dp = 72.dp,
    horizontalGap: Dp = 8.dp,
    verticalGap: Dp = 8.dp,
) {
    val drawables = remember(amount) { amount.resourceMapper() }
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
                    )
                }
            }
            Spacer(modifier = Modifier.height(verticalGap))
        }
    }
}

@Composable
private fun DayNightStrip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { idx ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("☀️ ${20 + idx * 2}", style = MaterialTheme.typography.bodySmall)
                Text("🌙 9", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun DrawScope.drawTiledImageInRect(
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

// Stretch image to fill rect (used for farms so height is uniform visually)
private fun DrawScope.drawImageStretchInRect(
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

// Fit the whole image into a rect while preserving aspect ratio (used for lakes)
private fun DrawScope.drawImageFitInRect(
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

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "History Toggle Preview",
    device = "spec:width=920dp,height=460dp,orientation=landscape"
)
@Composable
fun TransactionHistoryViewPreview() {
    MaterialTheme {
        TransactionHistoryView()
    }
}

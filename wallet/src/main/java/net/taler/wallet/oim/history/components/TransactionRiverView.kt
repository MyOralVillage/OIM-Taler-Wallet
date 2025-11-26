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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.taler.common.Amount as CommonAmount
import net.taler.database.TranxHistory
import net.taler.database.data_models.Tranx
import net.taler.wallet.BuildConfig
import net.taler.wallet.oim.res_mapping_extensions.UIIcons
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.oim.send.components.WoodTableBackground
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import net.taler.wallet.oim.res_mapping_extensions.Tile
import kotlinx.coroutines.delay
import net.taler.wallet.oim.send.components.StackedNotes
import net.taler.wallet.oim.send.components.NotesGalleryOverlay


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

        // show chest/home ONLY when the river is visible
        if (showRiver) {
            FloatingActionButton(
                onClick = onHome,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 14.dp),
                containerColor = Color(0xFF0B6AAE),
                contentColor = Color.White,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    bitmap = UIIcons("chest_open").resourceMapper(),
                    contentDescription = "Back to OIM home",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        // toggle button stays bottom-right
        FloatingActionButton(
            onClick = { showRiver = !showRiver },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = Color(0xFF0376C4),
            contentColor = Color.White,
            shape = MaterialTheme.shapes.large
        ) {
            if (showRiver) {
                Icon(Icons.Default.List, contentDescription = "Show list")
            } else {
                Icon(Icons.Default.Water, contentDescription = "Show river")
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
        // wood background like other OIM screens
        WoodTableBackground(
            modifier = Modifier.fillMaxSize(),
            light = false
        )

        Column(
            Modifier
                .fillMaxSize()
                // push whole river scene down so it starts below the chest
                .padding(top = 96.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                // river itself (leaves room on the right for notes)
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
                            start = 16.dp,
                            end = 120.dp, // reserved gap for BalanceNotes
                            top = 12.dp,
                            bottom = 12.dp
                        )
                )

                // LEFT SIDE: Receive (top) + Send (bottom)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .padding(start = 8.dp, top = 24.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TOP = RECEIVE
                    HistorySideButton(
                        bitmap = UIIcons("receive").resourceMapper(),
                        bgColor = Color(0xFF4CAF50),
                        onClick = onReceiveClick
                    )
                    // BOTTOM = SEND
                    HistorySideButton(
                        bitmap = UIIcons("send").resourceMapper(),
                        bgColor = Color(0xFFC32909),
                        onClick = onSendClick
                    )
                }

                // RIGHT SIDE: current balance as notes (no numbers),
                // centered vertically next to the river.
                if (balanceAmount != null) {
                    BalanceStackedNotes(
                        amount = balanceAmount,
                        isStackExpanded = isStackExpanded,
                        onExpand = {
                            if (!isStackExpanded) {
                                isStackExpanded = true
                                scope.launch {
                                    delay(400)   // wait for unstack animation
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

/** Shows the balance as a stack/grid of notes on the right side (no numeric text). */
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

    // Textures
    val farmRes  = Tile("farm").resourceMapper()
    val blankRes = Tile("blank").resourceMapper()
    val riverRes = Tile("river").resourceMapper()
    val lakeRes  = Tile("lake").resourceMapper()

    val farmBitmap  = ImageBitmap.imageResource(farmRes)
    val blankBitmap = ImageBitmap.imageResource(blankRes)
    val riverBitmap = ImageBitmap.imageResource(riverRes)
    val lakeBitmap  = ImageBitmap.imageResource(lakeRes)

    Row(
        modifier = modifier.horizontalScroll(scrollState)
    ) {
        val n = max(transactions.size, 1)
        val perTxnWidth = 140.dp
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

            val landBottom = h * 0.58f
            val riverBaseline = landBottom
            val yellowTop = landBottom
            val yellowBottom = h

            // ---------- BACKGROUNDS ----------
            drawTiledImageInRect(
                image = farmBitmap,
                rect = Rect(0f, 0f, w, landBottom)
            )
            drawTiledImageInRect(
                image = blankBitmap,
                rect = Rect(0f, yellowTop, w, yellowBottom)
            )

            // ---------- RIVER THICKNESS LOGIC (unchanged) ----------
            val nTx = transactions.size
            if (nTx == 0) return@Canvas

            val maxSingleMaj = transactions
                .map {
                    val maj = it.amount.value.toDouble() +
                            it.amount.fraction.toDouble() / 100_000_000.0
                    abs(maj)
                }
                .maxOrNull()
                ?.takeIf { it > 0.0 } ?: 1.0

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
            val step = if (nTx > 0) usableW / nTx else 0f

            val topPath = Path()
            val bottomPath = Path()
            for (i in 0..nTx) {
                val x = leftPad + i * step
                val thisTh = thicknessAtPoints[i]
                val wave = kotlin.math.sin(
                    i / max(1f, nTx.toFloat()) * 5f
                ) * (h * 0.01f)
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
                    val wave = kotlin.math.sin(
                        i / max(1f, nTx.toFloat()) * 5f
                    ) * (h * 0.01f)
                    val bottomY = riverBaseline + wave
                    lineTo(x, bottomY)
                }
                close()
            }

            // Fill river with river texture
            clipPath(river) {
                drawTiledImageInRect(
                    image = riverBitmap,
                    rect = Rect(0f, 0f, w, h)
                )
            }
            drawPath(river, color = Color(0xFF005188), style = Stroke(2.dp.toPx()))

            // ---------- LAKES ALONG THE BOTTOM EDGE OF THE RIVER ----------
            val lakeCount = 4

            val lakeHeight = h * 0.40f          // taller
            val lakeWidth  = lakeHeight * 1.3f  // wider than tall

            for (i in 0 until lakeCount) {
                val frac = (i + 1) / (lakeCount + 1).toFloat()
                val cx = leftPad + frac * usableW

                // Move them up so they sit more "on the tip" of the river
                val top = (riverBaseline - lakeHeight * 0.01f).coerceAtLeast(0f)
                val bottom = (top + lakeHeight).coerceAtMost(h)

                val rect = Rect(
                    left = cx - lakeWidth / 2f,
                    top = top,
                    right = cx + lakeWidth / 2f,
                    bottom = bottom
                )

                drawImageFitInRect(
                    image = lakeBitmap,
                    rect = rect
                )
            }

            // ---------- EVENTS & HIT AREAS (unchanged) ----------
            val hitRadius = 20.dp.toPx()
            transactions.forEachIndexed { idx, t ->
                val x = leftPad + idx * step + step / 2f
                val thisTh = thicknessAtPoints[idx + 1]
                val wave = kotlin.math.sin(
                    idx / max(1f, nTx.toFloat()) * 5f
                ) * (h * 0.01f)
                val topY = riverBaseline - thisTh + wave
                val bottomY = riverBaseline + wave

                if (t.direction.getValue()) {
                    val pipeTop = topY - 26.dp.toPx()
                    drawLine(
                        color = Color(0xFF2ECC71),
                        start = Offset(x, pipeTop),
                        end = Offset(x, topY),
                        strokeWidth = 6.dp.toPx()
                    )
                    drawCircle(
                        color = Color(0xFF2ECC71),
                        radius = 6.dp.toPx(),
                        center = Offset(x, pipeTop)
                    )
                    val rect = Rect(
                        left = x - hitRadius,
                        top = pipeTop - hitRadius,
                        right = x + hitRadius,
                        bottom = topY + hitRadius
                    )
                    hitRects += rect to t
                } else {
                    val center = Offset(x, bottomY + 16.dp.toPx())
                    drawCircle(
                        color = Color(0xFFE74C3C),
                        radius = 10.dp.toPx(),
                        center = center
                    )
                    val rect = Rect(
                        left = center.x - hitRadius,
                        top = center.y - hitRadius,
                        right = center.x + hitRadius,
                        bottom = center.y + hitRadius
                    )
                    hitRects += rect to t
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

// Fit the whole image into a rect (used for lake tiles)
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

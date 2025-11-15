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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.taler.database.TranxHistory
import net.taler.database.data_models.Tranx
import net.taler.wallet.BuildConfig
import net.taler.wallet.oim.res_mapping_extensions.UIIcons
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import net.taler.wallet.oim.send.components.WoodTableBackground



@Composable
fun TransactionHistoryView(
    modifier: Modifier = Modifier,
    onHome: () -> Unit = {},
) {
    var showRiver by rememberSaveable { mutableStateOf(true) }

    Box(modifier = modifier.fillMaxSize()) {
        if (showRiver) {
            OimRiverTransactionsView(
                modifier = Modifier.fillMaxSize()
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

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // wood background like other OIM screens
        WoodTableBackground(
            modifier = Modifier.fillMaxSize(),
            light = false
        )

        Column(Modifier.fillMaxSize()) {
            // moved down so it doesn't clash with status bar
            DayNightStrip(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp, start = 20.dp, end = 20.dp)
                    .height(50.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }

        if (txns.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No transactions yet", color = Color.White)
            }
        }
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



@Composable
private fun RiverSceneCanvasPerEvent(
    transactions: List<Tranx>,
    onTransactionClick: (Tranx) -> Unit,
    modifier: Modifier = Modifier
) {
    val hitRects = remember { mutableStateListOf<Pair<Rect, Tranx>>() }

    Canvas(
        modifier = modifier
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

        // brown land
        drawRect(
            color = Color(0xFF8D5A38),
            topLeft = Offset.Zero,
            size = androidx.compose.ui.geometry.Size(w, landBottom)
        )

        // lake
        val lake = Path().apply {
            val left = w * 0.33f
            val right = w * 0.47f
            val top = landBottom * 0.12f
            val bottom = landBottom * 0.55f
            moveTo(left, top)
            cubicTo(
                right, top + (bottom - top) * 0.15f,
                right, top + (bottom - top) * 0.6f,
                (left + right) / 1.6f, bottom
            )
            cubicTo(
                left, bottom - 25f,
                left - 12f, top + 20f,
                left, top
            )
            close()
        }
        drawPath(lake, Color(0xFF47B9FF))

        // yellow underground
        drawRect(
            color = Color(0xFFF9C92B),
            topLeft = Offset(0f, yellowTop),
            size = androidx.compose.ui.geometry.Size(w, yellowBottom - yellowTop)
        )

        val n = transactions.size
        if (n == 0) return@Canvas

        // figure out max single amount to scale thickness deltas
        val maxSingleMaj = transactions
            .map {
                val maj = it.amount.value.toDouble() + it.amount.fraction.toDouble() / 100_000_000.0
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
            val maj = t.amount.value.toDouble() + t.amount.fraction.toDouble() / 100_000_000.0
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
        val step = if (n > 0) usableW / n else 0f

        // build river path
        val topPath = Path()
        val bottomPath = Path()
        for (i in 0..n) {
            val x = leftPad + i * step
            val thisTh = thicknessAtPoints[i]
            val wave = kotlin.math.sin(i / max(1f, n.toFloat()) * 5f) * (h * 0.01f)
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
            for (i in n downTo 0) {
                val x = leftPad + i * step
                val wave = kotlin.math.sin(i / max(1f, n.toFloat()) * 5f) * (h * 0.01f)
                val bottomY = riverBaseline + wave
                lineTo(x, bottomY)
            }
            close()
        }

        // draw river
        drawPath(river, color = Color(0xFF0376C4))
        drawPath(river, color = Color(0xFF005188), style = Stroke(2.dp.toPx()))

        // bottom boxes ONLY (we removed the thin blue lines/canals)
        val boxCount = min(4, max(1, n))
        val boxWidth = (w * 0.10f).coerceAtMost(160f)
        val boxHeight = h * 0.22f
        for (i in 0 until boxCount) {
            val frac = (i + 1) / (boxCount + 1).toFloat()
            val cx = w * frac
            drawRect(
                color = Color(0xFF0376C4),
                topLeft = Offset(cx - boxWidth / 2f, yellowBottom - boxHeight),
                size = androidx.compose.ui.geometry.Size(boxWidth, boxHeight)
            )
        }

        // events (IN = water in, OUT = red dot)
        val hitRadius = 20.dp.toPx()
        transactions.forEachIndexed { idx, t ->
            val x = leftPad + idx * step + step / 2f
            val thisTh = thicknessAtPoints[idx + 1]
            val wave = kotlin.math.sin(idx / max(1f, n.toFloat()) * 5f) * (h * 0.01f)
            val topY = riverBaseline - thisTh + wave
            val bottomY = riverBaseline + wave

            if (t.direction.getValue()) {
                // IN
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
                // OUT
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

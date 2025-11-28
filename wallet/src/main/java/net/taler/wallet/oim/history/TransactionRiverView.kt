@file:OptIn(
    ExperimentalMaterial3Api::class,
    InternalSerializationApi::class
)

package net.taler.wallet.oim.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.taler.common.Amount as CommonAmount
import net.taler.database.TranxHistory
import net.taler.database.data_models.Tranx
import net.taler.wallet.BuildConfig
import net.taler.wallet.oim.resourceMappers.*
import net.taler.wallet.oim.send.components.WoodTableBackground
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import kotlinx.serialization.InternalSerializationApi
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.*
import net.taler.wallet.oim.OimColours
import net.taler.wallet.oim.OimTopBarCentered
import kotlin.math.sin
import android.graphics.Paint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalWindowInfo
import net.taler.wallet.oim.send.screens.SendScreenWithAmountPreview
import android.graphics.Color as AndroidColor

@Composable
fun TransactionHistoryView(
    modifier: Modifier = Modifier,
    onHome: () -> Unit = {},
    balanceAmount: Amount,
    onSendClick: () -> Unit = {},
    onReceiveClick: () -> Unit = {},
    previewTransactions: List<Tranx>? = null, // For preview/testing only
) {
    var showRiver by rememberSaveable { mutableStateOf(true) }
    val context = LocalContext.current
    var txns by remember { mutableStateOf<List<Tranx>>(previewTransactions ?: emptyList()) }

    LaunchedEffect(previewTransactions) {
        if (previewTransactions == null) {
            if (BuildConfig.DEBUG) TranxHistory.initTest(context)
            else TranxHistory.init(context)
            txns = TranxHistory.getHistory()
        }
    }

    var selected by remember { mutableStateOf<Tranx?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
        // Wood background
        WoodTableBackground(
            modifier = Modifier.fillMaxSize(),
            light = false
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // OIM Top Bar - stays on both screens
            OimTopBarCentered(
                balance = balanceAmount,
                onChestClick = onHome,
                colour = OimColours.TRX_HIST_COLOUR
            )


            // Content area with side buttons
            Box(modifier = Modifier) {
                // Main content (river or list)
                if (showRiver) {
                    RiverSceneCanvasPerEvent(
                        transactions = txns,
                        onTransactionClick = {
                            selected = it
                            scope.launch { sheetState.show() }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 104.dp) // Space for buttons
                    )
                } else {
                    TransactionsListContent(
                        transactions = txns,
                        onTransactionClick = {
                            selected = it
                            scope.launch { sheetState.show() }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 104.dp) // Space for buttons
                    )
                }

                // LEFT SIDE: Receive (top) + Toggle (middle) + Send (bottom)
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .padding(horizontal=5.dp,vertical = 15.dp),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TOP = RECEIVE
                    HistorySideButton(
                        bitmap = UIIcons("receive").resourceMapper(),
                        bgColor = OimColours.INCOMING_COLOUR,
                        onClick = onReceiveClick,
                        size = LocalWindowInfo.current.containerSize
                    )

                    // MIDDLE = TOGGLE VIEW
                    HistorySideButton(
                        bitmap =    if (showRiver) UIIcons("river").resourceMapper()
                                    else UIIcons("timeline").resourceMapper(),
                        bgColor = Color.Transparent,
                        onClick = { showRiver = !showRiver },
                        contentDescription = if (showRiver) "Show list" else "Show river",
                        size = LocalWindowInfo.current.containerSize
                    )
                    // BOTTOM = SEND
                    HistorySideButton(
                        bitmap = UIIcons("send").resourceMapper(),
                        bgColor = OimColours.OUTGOING_COLOUR,
                        onClick = onSendClick,
                        size = LocalWindowInfo.current.containerSize
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

    // Transaction details modal
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

/** Side button with bitmap icon */
@Composable
private fun HistorySideButton(
    bitmap: ImageBitmap,
    bgColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size : IntSize,
            contentDescription: String? = null
) {
    Box(
        modifier = modifier
            .size((size.height * 0.06f).dp)
            .clip(RoundedCornerShape(19.dp))
            .background(bgColor.copy(alpha = 0.9f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            modifier = Modifier.size((size.height * 0.06f).dp)
        )
    }
}


@Composable
private fun TransactionsListContent(
    transactions: List<Tranx>,
    onTransactionClick: (Tranx) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        transactions.forEach { transaction ->
            TransactionCard(
                amount = transaction.amount.toString(false),
                currency = transaction.amount.currency,
                date = transaction.datetime.fmtString(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                ),
                purpose = transaction.purpose,
                dir = transaction.direction,
                displayAmount = transaction.amount,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTransactionClick(transaction) }
                    .padding(vertical = 4.dp)
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

            // ---------- GROUP TRANSACTIONS BY DATE ----------
            val dateFormat = DateTimeFormatter.ofPattern("yyyy/MM/dd")
            val transactionsByDate = transactions.groupBy {
                it.datetime.fmtString(dateFormat)
            }

            // Track which dates we've already drawn
            val drawnDates = mutableSetOf<String>()

            // ---------- CLICKABLE FARMS (INCOMING) & LAKES (OUTGOING) ----------
            transactions.forEachIndexed { idx, t ->
                val x = leftPad + idx * step + step / 2f
                val dateStr = t.datetime.fmtString(dateFormat)

                if (t.direction.getValue()) {
                    // INCOMING = CLICKABLE FARM AREA (drawn on top of farm background)
                    val farmWidth = step * 0.8f
                    val farmHeight = (landBottom - dateStripHeight) * 0.7f
                    val farmTop = landBottom - farmHeight

                    val farmRect = Rect(
                        left = x - farmWidth / 2f,
                        top = farmTop,
                        right = x + farmWidth / 2f,
                        bottom = landBottom
                    )

                    // Draw a semi-transparent overlay to show it's clickable
                    // This makes the farm areas more visible and indicates interactivity
                    drawRect(
                        color = Color(0x30FFD700), // Semi-transparent gold
                        topLeft = androidx.compose.ui.geometry.Offset(farmRect.left, farmRect.top),
                        size = androidx.compose.ui.geometry.Size(farmRect.width, farmRect.height)
                    )

                    // DEBUG: Draw border around clickable area
                    drawRect(
                        color = Color.Red,
                        topLeft = androidx.compose.ui.geometry.Offset(farmRect.left, farmRect.top),
                        size = androidx.compose.ui.geometry.Size(farmRect.width, farmRect.height),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    hitAreas += farmRect to t
                } else {
                    // OUTGOING = VISIBLE CLICKABLE LAKE
                    val lakeHeight = h * 0.35f
                    val lakeWidth = lakeHeight * 1.3f
                    val lakeTop = riverBaseline

                    val lakeRect = Rect(
                        left = x - lakeWidth / 2f,
                        top = lakeTop,
                        right = x + lakeWidth / 2f,
                        bottom = (lakeTop + lakeHeight).coerceAtMost(h)
                    )

                    drawLake(
                        image = lakeBitmap,
                        rect = lakeRect
                    )

//                    // DEBUG: Draw border around clickable area
//                    drawRect(
//                        color = Color.Blue,
//                        topLeft = androidx.compose.ui.geometry.Offset(lakeRect.left, lakeRect.top),
//                        size = androidx.compose.ui.geometry.Size(lakeRect.width, lakeRect.height),
//                        style = Stroke(width = 3.dp.toPx())
//                    )
//
//                    hitAreas += lakeRect to t
                }

                // ---------- DATE LABELS (Only draw once per date) ----------
//                if (!drawnDates.contains(dateStr)) {
//                    drawnDates.add(dateStr)
//
//                    drawIntoCanvas { canvas ->
//                        val paint = Paint().apply {
//                            color = AndroidColor.WHITE
//                            textSize = 13.dp.toPx()
//                            isAntiAlias = true
//                            textAlign = Paint.Align.CENTER
//                        }
//
//                        // Format: ☀️ day / 🌙 month / ⭐ year
//                        val parts = dateStr.split("/")
//                        val year = parts.getOrNull(0) ?: "----"
//                        val month = parts.getOrNull(1) ?: "--"
//                        val day = parts.getOrNull(2) ?: "--"
//                        val formattedDate = "☀️ $day / 🌙 $month / ⭐ $year"
//
//                        // Measure text width for background
//                        val textWidth = paint.measureText(formattedDate)
//                        val textY = dateStripHeight * 0.6f
//                        val padding = 8.dp.toPx()
//
//                        // Draw semi-transparent white background
//                        val bgPaint = Paint().apply {
//                            color = AndroidColor.WHITE
//                            alpha = 50 // Very slight white background (0-255 scale)
//                            isAntiAlias = true
//                        }
//
//                        canvas.nativeCanvas.drawRoundRect(
//                            x - textWidth / 2f - padding,
//                            textY - 16.dp.toPx(),
//                            x + textWidth / 2f + padding,
//                            textY + 6.dp.toPx(),
//                            4.dp.toPx(),
//                            4.dp.toPx(),
//                            bgPaint
//                        )
//
//                        // Draw date text
//                        canvas.nativeCanvas.drawText(
//                            formattedDate,
//                            x,
//                            textY,
//                            paint
//                        )
//                    }
//                }
            }
        }
    }
}

@Composable
private fun FarmDrawer (
    image: ImageBitmap,
    rect:  Rect,
    onClick: () -> Unit,

) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    if (rect.contains(offset)) {
                        onClick()
                    }
                }
            }
    ) {drawFarm(image,rect)}
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

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Transaction History - River View",
    device = "spec:width=920dp,height=460dp,orientation=landscape"
)
@Composable
fun TransactionHistoryPreview() {
    MaterialTheme {
        // Create a specific date for testing
        val sameDate = FDtm() // Same date for multiple transactions

        val fakeTranx = listOf(
            Tranx(
                amount = CommonAmount("XOF", 10000L, 0),
                datetime = sameDate, // Same date
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "1"
            ),
            Tranx(
                amount = Amount("EUR", 5000L, 0),
                datetime = sameDate, // Same date as previous
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "2"
            ),
            Tranx(
                amount = Amount("SLE", 15000L, 0),
                datetime = FDtm(), // Different date
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "3"
            ),
            Tranx(
                amount = Amount("XOF", 8000L, 0),
                datetime = FDtm(), // Different date
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "4"
            ),
            Tranx(
                amount = Amount("SLE", 20000L, 0),
                datetime = FDtm(), // Different date
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "5"
            )
        )

        TransactionHistoryView(
            balanceAmount = Amount("SLE", 100L, 50_000_000),
            onHome = {},
            onSendClick = {},
            onReceiveClick = {},
            previewTransactions = fakeTranx
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
    TransactionHistoryPreview()
}
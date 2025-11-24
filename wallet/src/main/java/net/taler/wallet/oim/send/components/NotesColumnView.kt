package net.taler.wallet.oim.send.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import net.taler.common.R
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun StackedNotes(
    @DrawableRes noteResIds: List<Int>,
    modifier: Modifier = Modifier,
    stackOffsetX: Dp = 8.dp,
    stackOffsetY: Dp = 6.dp,
    noteWidth: Dp = 100.dp,
    noteHeight: Dp = 50.dp,
    expandedGap: Dp = 2.dp,
    rowGap: Dp = 2.dp,
    notesPerRow: Int = 5,
    animationDurationMs: Int = 400
) {
    if (noteResIds.isEmpty()) return

    var isExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }

    // Calculate rows needed
    val totalRows = ceil(noteResIds.size.toFloat() / notesPerRow).toInt()

    // Calculate stacked dimensions
    val stackedWidth = noteWidth + stackOffsetX * (noteResIds.size - 1)
    val stackedHeight = noteHeight + stackOffsetY * (noteResIds.size - 1)

    // Calculate expanded dimensions
    val notesInFirstRow = min(noteResIds.size, notesPerRow)
    val expandedWidth = (noteWidth + expandedGap) * notesInFirstRow - expandedGap
    val expandedHeight = (noteHeight + rowGap) * totalRows - rowGap

    // Animate container size
    val animatedWidth by animateDpAsState(
        targetValue = if (isExpanded) expandedWidth else stackedWidth,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "containerWidth"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (isExpanded) expandedHeight else stackedHeight,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "containerHeight"
    )

    // Single progress value for all notes
    val animationProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "expansionProgress"
    )

    // Convert to pixels
    val stackOffsetXPx = with(density) { stackOffsetX.toPx() }
    val stackOffsetYPx = with(density) { stackOffsetY.toPx() }
    val noteWidthPx = with(density) { noteWidth.toPx() }
    val noteHeightPx = with(density) { noteHeight.toPx() }
    val expandedGapPx = with(density) { expandedGap.toPx() }
    val rowGapPx = with(density) { rowGap.toPx() }

    Box(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) { isExpanded = !isExpanded },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(width = animatedWidth, height = animatedHeight),
            contentAlignment = Alignment.Center
        ) {
            noteResIds.forEachIndexed { index, resId ->
                val reverseIndex = noteResIds.size - 1 - index

                // Stacked position: diagonal stack centered in container
                val stackedTotalOffsetX = stackOffsetXPx * (noteResIds.size - 1)
                val stackedTotalOffsetY = stackOffsetYPx * (noteResIds.size - 1)
                val stackedX = stackOffsetXPx * reverseIndex - stackedTotalOffsetX / 2
                val stackedY = stackOffsetYPx * reverseIndex - stackedTotalOffsetY / 2

                // Calculate row and column for expanded grid
                val row = index / notesPerRow
                val col = index % notesPerRow
                val notesInThisRow = min(notesPerRow, noteResIds.size - row * notesPerRow)

                // Expanded position: grid layout, each row centered
                val rowWidth = (noteWidthPx + expandedGapPx) * notesInThisRow - expandedGapPx
                val expandedStartX = -rowWidth / 2 + noteWidthPx / 2
                val expandedX = expandedStartX + col * (noteWidthPx + expandedGapPx)

                // Y position based on row, centered vertically
                val totalGridHeight = (noteHeightPx + rowGapPx) * totalRows - rowGapPx
                val expandedStartY = -totalGridHeight / 2 + noteHeightPx / 2
                val expandedY = expandedStartY + row * (noteHeightPx + rowGapPx)

                // Interpolate positions
                val currentX = lerp(stackedX, expandedX, animationProgress)
                val currentY = lerp(stackedY, expandedY, animationProgress)

                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Note ${index + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(width = noteWidth, height = noteHeight)
                        .graphicsLayer {
                            translationX = currentX
                            translationY = currentY
                        }
                        .zIndex(index.toFloat())
                )
            }
        }
    }
}

private fun lerp(start: Float, end: Float, fraction: Float): Float {
    return start + (end - start) * fraction
}

// ==================== PREVIEWS ====================

@Preview(
    name = "Stacked Notes - 3 Notes",
    showBackground = true,
    backgroundColor = 0xFF8B7355,
    widthDp = 400
)
@Composable
private fun StackedNotesPreview() {
    val sampleNotes = listOf(
        R.drawable.sle_twenty,
        R.drawable.sle_five,
        R.drawable.sle_two
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        StackedNotes(
            noteResIds = sampleNotes,
            stackOffsetX = 12.dp,
            stackOffsetY = 8.dp,
            noteWidth = 120.dp,
            noteHeight = 60.dp
        )
    }
}

@Preview(
    name = "Stacked Notes - 7 Notes (2 rows)",
    showBackground = true,
    backgroundColor = 0xFF8B7355,
    widthDp = 500,
    heightDp = 300
)
@Composable
private fun StackedNotesTwoRowsPreview() {
    val notes = listOf(
        R.drawable.sle_one_hundred,
        R.drawable.sle_forty,
        R.drawable.sle_twenty,
        R.drawable.sle_ten,
        R.drawable.sle_five,
        R.drawable.sle_two,
        R.drawable.sle_one
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        StackedNotes(
            noteResIds = notes,
            stackOffsetX = 10.dp,
            stackOffsetY = 6.dp,
            noteWidth = 80.dp,
            noteHeight = 40.dp,
            expandedGap = 8.dp,
            rowGap = 12.dp,
            notesPerRow = 5
        )
    }
}

@Preview(
    name = "Stacked Notes - 12 Notes (3 rows)",
    showBackground = true,
    backgroundColor = 0xFF8B7355,
    widthDp = 500,
    heightDp = 350
)
@Composable
private fun StackedNotesThreeRowsPreview() {
    val notes = listOf(
        R.drawable.sle_one_hundred,
        R.drawable.sle_forty,
        R.drawable.sle_twenty,
        R.drawable.sle_ten,
        R.drawable.sle_five,
        R.drawable.sle_two,
        R.drawable.sle_one,
        R.drawable.sle_zero_point_five,
        R.drawable.sle_zero_point_twenty_five,
        R.drawable.sle_zero_point_one,
        R.drawable.sle_zero_point_zero_five,
        R.drawable.sle_zero_point_zero_one
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        StackedNotes(
            noteResIds = notes,
            stackOffsetX = 6.dp,
            stackOffsetY = 4.dp,
            noteWidth = 70.dp,
            noteHeight = 35.dp,
            expandedGap = 6.dp,
            rowGap = 10.dp,
            notesPerRow = 5
        )
    }
}

@Preview(name = "Single Note", showBackground = true, backgroundColor = 0xFF8B7355)
@Composable
private fun SingleNotePreview() {
    Box(modifier = Modifier.padding(16.dp)) {
        StackedNotes(
            noteResIds = listOf(R.drawable.sle_twenty),
            noteWidth = 140.dp,
            noteHeight = 70.dp
        )
    }
}
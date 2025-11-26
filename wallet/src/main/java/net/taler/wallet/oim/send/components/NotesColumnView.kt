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

data class NoteStack(
    val resId: Int,
    val count: Int,
    val startIndex: Int
)

@Composable
fun StackedNotes(
    @DrawableRes noteResIds: List<Int>,
    modifier: Modifier = Modifier,
    stackOffsetX: Dp = 8.dp,
    stackOffsetY: Dp = 8.dp,
    stackGap: Dp = 2.dp,
    noteWidth: Dp = 100.dp,
    noteHeight: Dp = 50.dp,
    expandedGap: Dp = 2.dp,
    rowGap: Dp = 2.dp,
    stackedRowGap: Dp = 8.dp,
    notesPerRow: Int = 5,
    stacksPerRow: Int = 5,
    animationDurationMs: Int = 400,
    expanded: Boolean = false,
    onClick: () -> Unit = {}
) {
    if (noteResIds.isEmpty()) return

    val density = LocalDensity.current
    val interactionSource = remember { MutableInteractionSource() }

    // Group notes by denomination to create stacks
    val noteStacks = remember(noteResIds) {
        val stacks = mutableListOf<NoteStack>()
        var currentResId = noteResIds[0]
        var currentCount = 1
        var startIndex = 0

        for (i in 1 until noteResIds.size) {
            if (noteResIds[i] == currentResId) {
                currentCount++
            } else {
                stacks.add(NoteStack(currentResId, currentCount, startIndex))
                startIndex = i
                currentResId = noteResIds[i]
                currentCount = 1
            }
        }
        stacks.add(NoteStack(currentResId, currentCount, startIndex))
        stacks
    }

    // Calculate rows needed for expanded view
    val totalRows = ceil(noteResIds.size.toFloat() / notesPerRow).toInt()

    // Calculate rows needed for stacked view
    val totalStackRows = ceil(noteStacks.size.toFloat() / stacksPerRow).toInt()

    // Calculate stacked dimensions
    // For multi-row stacked layout, calculate width per row and take the max
    val stackedWidthPx = (0 until totalStackRows).maxOf { rowIndex ->
        val stacksInRow = min(stacksPerRow, noteStacks.size - rowIndex * stacksPerRow)
        val stacksForRow = noteStacks.subList(
            rowIndex * stacksPerRow,
            min((rowIndex + 1) * stacksPerRow, noteStacks.size)
        )
        stacksForRow.sumOf { stack ->
            with(density) { (noteWidth + stackOffsetX * (stack.count - 1)).toPx() }.toDouble()
        }.toFloat() + with(density) { (stackGap * (stacksInRow - 1)).toPx() }
    }

    // Calculate height per row (find the tallest stack in each row)
    val stackedHeightDp = (0 until totalStackRows).sumOf { rowIndex ->
        val stacksForRow = noteStacks.subList(
            rowIndex * stacksPerRow,
            min((rowIndex + 1) * stacksPerRow, noteStacks.size)
        )
        val maxStackHeightInRow = stacksForRow.maxOf { it.count }
        with(density) { (noteHeight + stackOffsetY * (maxStackHeightInRow - 1)).toPx() }.toDouble()
    }.toFloat() + with(density) { (stackedRowGap * (totalStackRows - 1)).toPx() }

    // Calculate expanded dimensions
    val notesInFirstRow = min(noteResIds.size, notesPerRow)
    val expandedWidth = (noteWidth + expandedGap) * notesInFirstRow - expandedGap
    val expandedHeight = (noteHeight + rowGap) * totalRows - rowGap

    // Animate container size
    val animatedWidth by animateDpAsState(
        targetValue = if (expanded) expandedWidth else with(density) { stackedWidthPx.toDp() },
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "containerWidth"
    )
    val animatedHeight by animateDpAsState(
        targetValue = if (expanded) expandedHeight else with(density) { stackedHeightDp.toDp() },
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "containerHeight"
    )

    // Single progress value for all notes
    val animationProgress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(durationMillis = animationDurationMs),
        label = "expansionProgress"
    )

    // Convert to pixels
    val stackOffsetXPx = with(density) { stackOffsetX.toPx() }
    val stackOffsetYPx = with(density) { stackOffsetY.toPx() }
    val stackGapPx = with(density) { stackGap.toPx() }
    val stackedRowGapPx = with(density) { stackedRowGap.toPx() }
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
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(width = animatedWidth, height = animatedHeight),
            contentAlignment = Alignment.Center
        ) {
            noteResIds.forEachIndexed { index, resId ->
                // Find which stack this note belongs to
                val stackInfo = noteStacks.first { stack ->
                    index >= stack.startIndex && index < stack.startIndex + stack.count
                }
                val positionInStack = index - stackInfo.startIndex
                val stackIndex = noteStacks.indexOf(stackInfo)

                // Calculate which row this stack is in
                val stackRow = stackIndex / stacksPerRow
                val stackCol = stackIndex % stacksPerRow
                val stacksInThisRow = min(stacksPerRow, noteStacks.size - stackRow * stacksPerRow)

                // Calculate stacked position
                // X: position based on which stack + position within stack
                val stacksForRow = noteStacks.subList(
                    stackRow * stacksPerRow,
                    min((stackRow + 1) * stacksPerRow, noteStacks.size)
                )
                val rowWidthPx = stacksForRow.sumOf { stack ->
                    (noteWidthPx + stackOffsetXPx * (stack.count - 1)).toDouble()
                }.toFloat() + stackGapPx * (stacksInThisRow - 1)

                var stackedX = -rowWidthPx / 2
                for (i in 0 until stackCol) {
                    val stack = stacksForRow[i]
                    stackedX += noteWidthPx + stackOffsetXPx * (stack.count - 1) + stackGapPx
                }
                stackedX += noteWidthPx / 2 + stackOffsetXPx * positionInStack

                // Y: based on row position and position within stack
                // Calculate cumulative height up to this row
                var cumulativeY = -stackedHeightDp / 2
                for (r in 0 until stackRow) {
                    val stacksInPrevRow = noteStacks.subList(
                        r * stacksPerRow,
                        min((r + 1) * stacksPerRow, noteStacks.size)
                    )
                    val maxHeightInPrevRow = stacksInPrevRow.maxOf { it.count }
                    cumulativeY += with(density) { (noteHeight + stackOffsetY * (maxHeightInPrevRow - 1)).toPx() }
                    if (r < totalStackRows - 1) {
                        cumulativeY += stackedRowGapPx
                    }
                }

                // Add height of current row to center this stack vertically in its row
                val maxHeightInCurrentRow = stacksForRow.maxOf { it.count }
                val currentRowHeight = with(density) { (noteHeight + stackOffsetY * (maxHeightInCurrentRow - 1)).toPx() }
                cumulativeY += currentRowHeight / 2

                // Add offset within the stack
                val stackedY = cumulativeY + stackOffsetYPx * positionInStack - stackOffsetYPx * (stackInfo.count - 1) / 2

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
    name = "Stacked Notes - 45 (20+20+5)",
    showBackground = true,
    backgroundColor = 0xFF8B7355,
    widthDp = 400
)
@Composable
private fun StackedNotesPreview45() {
    val sampleNotes = listOf(
        R.drawable.sle_twenty,
        R.drawable.sle_twenty,
        R.drawable.sle_five
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        StackedNotes(
            noteResIds = sampleNotes,
            stackOffsetX = 8.dp,
            stackOffsetY = 6.dp,
            stackGap = 16.dp,
            noteWidth = 120.dp,
            noteHeight = 60.dp
        )
    }
}

@Preview(
    name = "Stacked Notes - Many Stacks (Multi-row)",
    showBackground = true,
    backgroundColor = 0xFF8B7355,
    widthDp = 400
)
@Composable
private fun StackedNotesPreviewManyStacks() {
    val sampleNotes = listOf(
        R.drawable.sle_twenty,
        R.drawable.sle_twenty,
        R.drawable.sle_twenty,
        R.drawable.sle_five,
        R.drawable.sle_five,
        R.drawable.sle_two,
        R.drawable.sle_two,
        R.drawable.sle_twenty,
        R.drawable.sle_five
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        StackedNotes(
            noteResIds = sampleNotes,
            stackOffsetX = 8.dp,
            stackOffsetY = 6.dp,
            stackGap = 16.dp,
            stackedRowGap = 12.dp,
            noteWidth = 80.dp,
            noteHeight = 40.dp,
            stacksPerRow = 3
        )
    }
}
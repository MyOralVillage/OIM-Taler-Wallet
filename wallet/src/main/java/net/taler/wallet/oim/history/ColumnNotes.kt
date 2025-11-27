package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.resourceMappers.resourceMapper
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun ColumnNotes(
    amount: Amount,
    modifier: Modifier = Modifier,
    stackOffsetX: Dp = 8.dp,
    stackOffsetY: Dp = 8.dp,
    stackGap: Dp = 2.dp,
    noteWidth: Dp = 100.dp,
    noteHeight: Dp = 50.dp,
    rowGap: Dp = 4.dp,
    stacksPerRow: Int = 5
) {
    val density = LocalDensity.current

    // Try to map amount to note resources
    var noteResIds: List<Int>
    var errorMessage: String?

    try {
        noteResIds = amount.resourceMapper()
        errorMessage = null
    } catch (e: IllegalArgumentException) {
        noteResIds = emptyList()
        errorMessage = e.message
    }

    // Show error message if present
    if (errorMessage != null) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage,
                color = Color.Red
            )
        }
        return
    }

    if (noteResIds.isEmpty()) return

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

    // Calculate rows needed
    val totalStackRows = ceil(noteStacks.size.toFloat() / stacksPerRow).toInt()

    // Calculate dimensions
    // For multi-row layout, calculate width per row and take the max
    val gridWidthPx = (0 until totalStackRows).maxOf { rowIndex ->
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
    val gridHeightPx = (0 until totalStackRows).sumOf { rowIndex ->
        val stacksForRow = noteStacks.subList(
            rowIndex * stacksPerRow,
            min((rowIndex + 1) * stacksPerRow, noteStacks.size)
        )
        val maxStackHeightInRow = stacksForRow.maxOf { it.count }
        with(density) { (noteHeight + stackOffsetY * (maxStackHeightInRow - 1)).toPx() }.toDouble()
    }.toFloat() + with(density) { (rowGap * (totalStackRows - 1)).toPx() }

    // Convert to pixels
    val stackOffsetXPx = with(density) { stackOffsetX.toPx() }
    val stackOffsetYPx = with(density) { stackOffsetY.toPx() }
    val stackGapPx = with(density) { stackGap.toPx() }
    val rowGapPx = with(density) { rowGap.toPx() }
    val noteWidthPx = with(density) { noteWidth.toPx() }
    val noteHeightPx = with(density) { noteHeight.toPx() }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(
                width = with(density) { gridWidthPx.toDp() },
                height = with(density) { gridHeightPx.toDp() }
            ),
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

                // Calculate position
                // X: position based on which stack + position within stack
                val stacksForRow = noteStacks.subList(
                    stackRow * stacksPerRow,
                    min((stackRow + 1) * stacksPerRow, noteStacks.size)
                )
                val rowWidthPx = stacksForRow.sumOf { stack ->
                    (noteWidthPx + stackOffsetXPx * (stack.count - 1)).toDouble()
                }.toFloat() + stackGapPx * (stacksInThisRow - 1)

                var xPos = -rowWidthPx / 2
                for (i in 0 until stackCol) {
                    val stack = stacksForRow[i]
                    xPos += noteWidthPx + stackOffsetXPx * (stack.count - 1) + stackGapPx
                }
                xPos += noteWidthPx / 2 + stackOffsetXPx * positionInStack

                // Y: based on row position and position within stack
                // Calculate cumulative height up to this row
                var cumulativeY = -gridHeightPx / 2
                for (r in 0 until stackRow) {
                    val stacksInPrevRow = noteStacks.subList(
                        r * stacksPerRow,
                        min((r + 1) * stacksPerRow, noteStacks.size)
                    )
                    val maxHeightInPrevRow = stacksInPrevRow.maxOf { it.count }
                    cumulativeY += noteHeightPx + stackOffsetYPx * (maxHeightInPrevRow - 1)
                    if (r < totalStackRows - 1) {
                        cumulativeY += rowGapPx
                    }
                }

                // Add height of current row to center this stack vertically in its row
                val maxHeightInCurrentRow = stacksForRow.maxOf { it.count }
                val currentRowHeight = noteHeightPx + stackOffsetYPx * (maxHeightInCurrentRow - 1)
                cumulativeY += currentRowHeight / 2

                // Add offset within the stack
                val yPos = cumulativeY + stackOffsetYPx * positionInStack - stackOffsetYPx * (stackInfo.count - 1) / 2

                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "Note ${index + 1}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(width = noteWidth, height = noteHeight)
                        .graphicsLayer {
                            translationX = xPos
                            translationY = yPos
                        }
                        .zIndex(index.toFloat())
                )
            }
        }
    }
}

// ==================== PREVIEWS ====================

//@Preview(
//    name = "Column Notes - 45 (20+20+5)",
//    showBackground = true,
//    backgroundColor = 0xFF8B7355,
//    widthDp = 400
//)
//@Composable
//private fun ColumnNotesPreview45() {
//    val sampleNotes = listOf(
//        R.drawable.sle_twenty,
//        R.drawable.sle_twenty,
//        R.drawable.sle_five
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(32.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        ColumnNotes(
//            noteResIds = sampleNotes,
//            stackOffsetX = 8.dp,
//            stackOffsetY = 6.dp,
//            stackGap = 16.dp,
//            noteWidth = 120.dp,
//            noteHeight = 60.dp,
//            stacksPerRow = 5
//        )
//    }
//}
//
//@Preview(
//    name = "Column Notes - Multi-row Stacks",
//    showBackground = true,
//    backgroundColor = 0xFF8B7355,
//    widthDp = 400
//)
//@Composable
//private fun ColumnNotesPreviewMultiRow() {
//    val sampleNotes = listOf(
//        R.drawable.sle_twenty,
//        R.drawable.sle_twenty,
//        R.drawable.sle_twenty,
//        R.drawable.sle_five,
//        R.drawable.sle_five,
//        R.drawable.sle_two,
//        R.drawable.sle_two,
//        R.drawable.sle_twenty,
//        R.drawable.sle_five
//    )
//
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(32.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        ColumnNotes(
//            noteResIds = sampleNotes,
//            stackOffsetX = 8.dp,
//            stackOffsetY = 6.dp,
//            stackGap = 16.dp,
//            noteWidth = 80.dp,
//            noteHeight = 40.dp,
//            rowGap = 12.dp,
//            stacksPerRow = 3
//        )
//    }
//}
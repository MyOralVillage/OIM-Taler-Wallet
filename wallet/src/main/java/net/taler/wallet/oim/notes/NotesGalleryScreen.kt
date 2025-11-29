package net.taler.wallet.oim.notes

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.taler.common.R

/**
 * ## NotesGalleryScreen
 *
 * A full-screen view that displays a collection of banknotes/images
 * in a grid layout with a top app bar.
 *
 * Features:
 * - Material 3 Scaffold with TopAppBar
 * - Back button navigation
 * - Vertical scrollable grid view
 * - Smooth item animations
 * - Dynamic sizing based on item count
 *
 * @param onBackClick Callback invoked when back button is pressed
 * @param bitmaps List of ImageBitmap to display (use this OR drawableResIds)
 * @param drawableResIds List of drawable resource IDs to display (use this OR bitmaps)
 * @param title Title to display in the top app bar
 * @param noteHeight Fixed height of each note/image in the gallery
 * @param notesPerRow Number of notes displayed per row (default: 4)
 * @param noteSpacing Spacing between notes in the gallery
 * @param useDynamicSizing Whether to adapt columns and height based on item count
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesGalleryScreen(
    onBackClick: () -> Unit,
    bitmaps: List<ImageBitmap> = emptyList(),
    drawableResIds: List<Int> = emptyList(),
    title: String = "Banknotes",
    noteHeight: Dp = 100.dp,
    notesPerRow: Int = 4,
    noteSpacing: Dp = 8.dp,
    useDynamicSizing: Boolean = true,
    colour : Color = Color.White
) {
    // Dynamic sizing logic
    val itemCount = bitmaps.size + drawableResIds.size
    val (dynamicColumns, dynamicHeight) = if (useDynamicSizing) {
        when (itemCount) {
            1 -> 1 to 250.dp
            2 -> 2 to 180.dp
            3 -> 3 to 140.dp
            4 -> 2 to 140.dp // 2x2 grid
            5, 6 -> 3 to 120.dp
            else -> 4 to 100.dp // Default for 7+
        }
    } else {
        notesPerRow to noteHeight
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {  },

                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colour,
                    titleContentColor = colour
                )
            )
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(dynamicColumns),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(noteSpacing),
            verticalArrangement = Arrangement.spacedBy(noteSpacing)
        ) {
            if (bitmaps.isNotEmpty()) {
                itemsIndexed(bitmaps) { index, bitmap ->
                    NoteItem(
                        bitmap = bitmap,
                        index = index,
                        noteHeight = dynamicHeight
                    )
                }
            }

            if (drawableResIds.isNotEmpty()) {
                itemsIndexed(drawableResIds) { index, resId ->
                    NoteItem(
                        drawableResId = resId,
                        index = index,
                        noteHeight = dynamicHeight
                    )
                }
            }
        }
    }
}

/**
 * Internal composable for displaying individual note items
 */
@Composable
private fun NoteItem(
    bitmap: ImageBitmap? = null,
    @DrawableRes drawableResId: Int? = null,
    index: Int,
    noteHeight: Dp,
) {
    Card(
        modifier = Modifier
            .height(noteHeight)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            when {
                bitmap != null -> {
                    Image(
                        bitmap = bitmap,
                        contentDescription = "Note ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                drawableResId != null -> {
                    Image(
                        painter = painterResource(id = drawableResId),
                        contentDescription = "Note ${index + 1}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Gallery Screen - 3 Notes",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryScreenPreview() {
    MaterialTheme {
        NotesGalleryScreen(
            onBackClick = { },
            drawableResIds = listOf(
                R.drawable.sle_twenty,
                R.drawable.sle_five,
                R.drawable.sle_two
            ),
            title = "Leone Banknotes"
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Gallery Screen - Many Notes",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryScreenManyNotesPreview() {
    MaterialTheme {
        NotesGalleryScreen(
            onBackClick = { },
            drawableResIds = listOf(
                R.drawable.sle_one_hundred,
                R.drawable.sle_forty,
                R.drawable.sle_twenty,
                R.drawable.sle_ten,
                R.drawable.sle_five,
                R.drawable.sle_two,
                R.drawable.sle_one,
                R.drawable.sle_zero_point_five,
                R.drawable.sle_zero_point_twenty_five,
                R.drawable.sle_zero_point_one
            ),
            title = "All Leone Notes"
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Gallery Screen - Euro Notes",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryScreenEuroPreview() {
    MaterialTheme {
        NotesGalleryScreen(
            onBackClick = { },
            drawableResIds = listOf(
                R.drawable.eur_fifty,
                R.drawable.eur_twenty,
                R.drawable.eur_ten,
                R.drawable.eur_five,
                R.drawable.eur_two,
                R.drawable.eur_one
            ),
            title = "Euro Banknotes"
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Gallery Screen - Single Note",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryScreenSinglePreview() {
    MaterialTheme {
        NotesGalleryScreen(
            onBackClick = { },
            drawableResIds = listOf(R.drawable.sle_one_hundred),
            title = "100 Leone Note"
        )
    }
}
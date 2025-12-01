/*
 * This file is part of GNU Taler
 * (C) 2025 Taler Systems S.A.
 *
 * GNU Taler is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 *
 * GNU Taler is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>
 */
package net.taler.wallet.oim.notes

/**
 * NOTES MODULE – FULL-SCREEN GALLERY OVERLAY
 *
 * A reusable overlay that shows a collection of notes in a grid on top of
 * the current screen. It is intended for quick, modal inspection of all
 * bills backing an amount, without leaving the current flow.
 *
 * Features:
 *  - Dimmed background with tap-to-dismiss behaviour.
 *  - Animated fade / scale for the gallery container and individual notes.
 *  - Supports both [ImageBitmap]s and drawable resource IDs.
 *  - Dynamic columns and note heights based on the number of items.
 *
 * Typically invoked from components like [NotesPile] or [StackedNotes].
 */

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.taler.common.R
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign

/**
 * ## NotesGalleryOverlay
 *
 * A reusable overlay component that displays a collection of banknotes/images
 * in a white card popup with smooth animations. Can display either ImageBitmaps
 * or drawable resource IDs.
 *
 * Features:
 * - Smooth pop-up animation with spring physics
 * - Semi-transparent background overlay
 * - Vertical scrollable grid view (4 notes per row)
 * - Close button in top-right corner
 * - Tap outside to dismiss
 *
 * @param isVisible Controls the visibility of the overlay
 * @param onDismiss Callback invoked when overlay should be dismissed
 * @param bitmaps List of ImageBitmap to display (use this OR drawableResIds)
 * @param drawableResIds List of drawable resource IDs to display (use this OR bitmaps)
 * @param noteHeight Fixed height of each note/image in the gallery
 * @param notesPerRow Number of notes displayed per row (default: 4)
 * @param cardMaxWidth Maximum width of the popup card
 * @param noteSpacing Spacing between notes in the gallery
 * @param backgroundAlpha Alpha value for the semi-transparent background (0f-1f)
 * @param useDynamicSizing Whether to adapt columns and height based on item count
 */
@Composable
fun NotesGalleryOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    bitmaps: List<ImageBitmap> = emptyList(),
    drawableResIds: List<Int> = emptyList(),
    noteHeight: Dp = 100.dp,
    notesPerRow: Int = 4,
    cardMaxWidth: Dp = 700.dp,
    noteSpacing: Dp = 8.dp,
    backgroundAlpha: Float = 0.5f,
    useDynamicSizing: Boolean = true
) {

    val overlayAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "overlayAlpha"
    )

    val cardScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.7f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "cardScale"
    )

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

    // Only render when visible or animating out
    if (overlayAlpha > 0f) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Semi-transparent background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(overlayAlpha)
                    .background(Color.Black.copy(alpha = backgroundAlpha))
                    .clickable { onDismiss() }
            )

            // White card popup
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(overlayAlpha)
                    .background(Color.White)
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top close row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Grid fills rest of screen
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(dynamicColumns),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(noteSpacing),
                        verticalArrangement = Arrangement.spacedBy(noteSpacing)
                    ) {

                        if (bitmaps.isNotEmpty())
                            itemsIndexed(bitmaps) { index, bitmap ->
                                NoteItem(
                                    bitmap = bitmap,
                                    index = index,
                                    isVisible = isVisible,
                                    noteHeight = dynamicHeight
                                )
                            }

                        if (drawableResIds.isNotEmpty())
                            itemsIndexed(drawableResIds) { index, resId ->
                                NoteItem(
                                    drawableResId = resId,
                                    index = index,
                                    isVisible = isVisible,
                                    noteHeight = dynamicHeight
                                )
                            }
                    }
                }
            }
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Gallery Overlay - 3 Notes",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryOverlayPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        // Simulated background content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF8B7355)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Background Content",
                color = Color.White
            )
        }

        // Overlay
        NotesGalleryOverlay(
            isVisible = true,
            onDismiss = { },
            drawableResIds = listOf(
                R.drawable.sle_twenty,
                R.drawable.sle_five,
                R.drawable.sle_two
            ),
            noteHeight = 115.dp
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Gallery Overlay - Many Notes (Grid)",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryOverlayManyNotesPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        NotesGalleryOverlay(
            isVisible = true,
            onDismiss = { },
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
            noteHeight = 115.dp,
            notesPerRow = 4,
            noteSpacing = 8.dp
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Gallery Overlay - Large Notes (Grid)",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryOverlayLargePreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        NotesGalleryOverlay(
            isVisible = true,
            onDismiss = { },
            drawableResIds = listOf(
                R.drawable.eur_fifty,
                R.drawable.eur_twenty,
                R.drawable.eur_ten,
                R.drawable.eur_five,
                R.drawable.eur_two,
                R.drawable.eur_one
            ),
            noteHeight = 115.dp,
            notesPerRow = 4,
            noteSpacing = 16.dp,
            backgroundAlpha = 0.7f
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Gallery Overlay - Single Note",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryOverlaySinglePreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray)
    ) {
        NotesGalleryOverlay(
            isVisible = true,
            onDismiss = { },
            drawableResIds = listOf(R.drawable.sle_one_hundred),
            noteHeight = 115.dp
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Gallery Overlay - Closed State",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
private fun NotesGalleryOverlayClosedPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF8B7355)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "Tap to open gallery\n(Preview shows closed state)",
            color = Color.White,
            textAlign = TextAlign.Center
        )

        // Overlay is not visible
        NotesGalleryOverlay(
            isVisible = false,
            onDismiss = { },
            drawableResIds = listOf(
                R.drawable.sle_twenty,
                R.drawable.sle_five
            ),
            noteHeight = 115.dp
        )
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
    isVisible: Boolean,
    noteHeight: Dp
) {
    // Individual note scale animation
    val noteScale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "noteScale_$index"
    )

    Box(
        modifier = Modifier
            .height(noteHeight)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = noteScale
                scaleY = noteScale
            },
        contentAlignment = Alignment.Center
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
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}
package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * ## NotesPile
 *
 * Renders a visually scattered pile of banknotes centered on the screen.
 * Each note is slightly rotated and offset for a natural, layered effect.
 *
 * This is typically used alongside [NoteFlyer]. When a flyer animation
 * completes, its bitmap is added to [landedNotes] to appear in the pile.
 *
 * Notes are drawn in the order they appear in [landedNotes]: first items
 * appear at the bottom, later items appear on top.
 *
 * @param landedNotes List of bitmaps representing notes that have landed.
 *                     New notes should be appended to this list when
 *                     flyer animations finish.
 * @param noteWidthPx Width of each note in pixels. The height is
 *                    automatically calculated as `0.55 × width` to
 *                    maintain visual proportion with flyers.
 *
 * @see NoteFlyer for animated flying notes.
 */
@Composable
fun NotesPile(
    landedNotes: List<ImageBitmap>,
    noteWidthPx: Float
) {
    val density = LocalDensity.current
    val wDp = with(density) { noteWidthPx.toDp() }
    val hDp = with(density) { (noteWidthPx * 0.55f).toDp() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Draw notes bottom-to-top
        landedNotes.forEachIndexed { i, bmp ->
            // Stable pseudo-random rotation and offset per note
            val rot = remember(i) { Random.nextFloat() * 18f - 9f }   // -9..+9 degrees
            val dx = remember(i) { (Random.nextInt(-10, 10)).dp }     // slight horizontal shift
            val dy = remember(i) { (Random.nextInt(-6, 6)).dp }       // slight vertical shift

            Image(
                bitmap = bmp,
                contentDescription = null,
                modifier = Modifier
                    .offset(dx, dy)
                    .size(wDp, hDp)
                    .graphicsLayer {
                        rotationZ = rot
                        shadowElevation = 10f
                    },
                contentScale = ContentScale.Crop
            )
        }
    }
}
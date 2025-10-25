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

/**
 * ## NotesPile
 *
 * Composable that renders a centered stack of landed banknotes.
 * Each note is slightly rotated and offset to produce a natural,
 * scattered pile effect, representing received or accumulated
 * notes during the Send animation.
 *
 * This component is typically updated when [NoteFlyer] animations
 * complete, with new bitmaps appended to the [landedNotes] list.
 *
 * @param landedNotes List of note bitmaps that have "landed".
 * @param noteWidthPx Width of each note in pixels (used for size and proportion).
 * @see net.taler.wallet.oim.send.components.NoteFlyer
 */

package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Centered stack of landed notes.
 * Call landedNotes.add(bmp) when a flyer arrives (see NoteFlyer usage).
 */
@Composable
fun NotesPile(
    landedNotes: List<ImageBitmap>,
    noteWidthPx: Float,              // keep same width as flyers
) {
    val density = LocalDensity.current
    val wDp = with(density) { noteWidthPx.toDp() }
    val hDp = with(density) { (noteWidthPx * 0.55f).toDp() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // draw bottom first → newest on top
        landedNotes.forEachIndexed { i, bmp ->
            // stable randoms per item index
            val rot = remember(i) { Random.nextFloat() * 18f - 9f }          // -9..+9 deg
            val dx = remember(i) { (Random.nextInt(-10, 10)).dp }            // tiny shift
            val dy = remember(i) { (Random.nextInt(-6, 6)).dp }

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

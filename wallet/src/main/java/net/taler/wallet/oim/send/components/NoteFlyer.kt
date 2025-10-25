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

package net.taler.wallet.oim.send.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * ## NoteFlyer
 *
 * Animates a “flying banknote” across the screen for visual feedback
 * during a send transaction.
 *
 * The animation sequence:
 * 1. The note fades in and scales from 0.7x to 1x.
 * 2. Moves from [startInRoot] to [endInRoot] coordinates.
 * 3. Fades out and invokes [onArrive] upon completion.
 *
 * @param noteRes Resource ID of the note image to animate.
 * @param startInRoot Starting position of the note in root coordinates (pixels).
 * @param endInRoot Ending position of the note in root coordinates (pixels).
 * @param widthPx Width of the note in pixels. Height is calculated proportionally (≈0.55× width).
 * @param onArrive Callback triggered when the animation finishes.
 */
@Composable
fun NoteFlyer(
    @DrawableRes noteRes: Int,
    startInRoot: Offset,
    endInRoot: Offset,
    widthPx: Float,
    onArrive: () -> Unit
) {
    val x = remember { Animatable(startInRoot.x) }
    val y = remember { Animatable(startInRoot.y) }
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(noteRes, startInRoot, endInRoot) {
        x.snapTo(startInRoot.x)
        y.snapTo(startInRoot.y)
        alpha.animateTo(1f, tween(120))
        scale.animateTo(1f, tween(300))
        x.animateTo(endInRoot.x, tween(420))
        y.animateTo(endInRoot.y, tween(420))
        alpha.animateTo(0f, tween(150))
        onArrive()
    }

    val hPx = widthPx * 0.55f
    val density = LocalDensity.current
    val widthDp = with(density) { widthPx.toDp() }
    val heightDp = with(density) { hPx.toDp() }

    Image(
        painter = painterResource(noteRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .offset {
                IntOffset(
                    (x.value - widthPx / 2f).roundToInt(),
                    (y.value - hPx / 2f).roundToInt()
                )
            }
            .size(widthDp, heightDp)
    )
}
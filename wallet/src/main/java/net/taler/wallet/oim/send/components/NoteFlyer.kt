/*
<<<<<<< HEAD
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

/*
=======
>>>>>>> 5c7011a (fixed preview animations)
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

@Composable
fun NoteFlyer(
    path: String,
    startInRoot: Offset,
    endInRoot: Offset,
    widthPx: Float,                  // flight width in PX
    onArrive: () -> Unit
) {
    // animate x and y separately (no VectorConverter headaches)
    val x = remember { Animatable(startInRoot.x) }
    val y = remember { Animatable(startInRoot.y) }
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0.0f) }

    LaunchedEffect(path, startInRoot, endInRoot) {
        // ensure we start from the provided origin each time
        x.snapTo(startInRoot.x)
        y.snapTo(startInRoot.y)
        alpha.animateTo(1f, tween(120))
        scale.animateTo(1f, tween(300))
        // fly to target
        x.animateTo(endInRoot.x, tween(420))
        y.animateTo(endInRoot.y, tween(420))
        // fade out as it “lands”
        alpha.animateTo(0f, tween(150))
        onArrive()
    }

    val hPx = widthPx * 0.55f
    val density = LocalDensity.current
    val widthDp = with(density) { widthPx.toDp() }
    val heightDp = with(density) { hPx.toDp() }

    Image(
<<<<<<< HEAD
        painter = assetPainterOrPreview(path),
=======
        painter = assetPainterOrPreview(path, PreviewAssets.id(path)),
>>>>>>> 5c7011a (fixed preview animations)
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

package net.taler.wallet.oim.send.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/**
 * ## NoteFlyer
 *
 * Animates a "flying banknote" across the screen for visual feedback
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
 * @param widthPx Width of the note in pixels. Height follows the image's natural aspect ratio.
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
        // Fade in
        alpha.animateTo(1f, tween(300))
        // Scale up
        scale.animateTo(1f, tween(250))
        // Move to destination
        x.animateTo(endInRoot.x, tween(500))
        y.animateTo(endInRoot.y, tween(500))
        // Fade out
        alpha.animateTo(0f, tween(300))
        onArrive()
    }

    val density = LocalDensity.current
    val widthDp = with(density) { widthPx.toDp() }

    Image(
        painter = painterResource(noteRes),
        contentDescription = null,
        contentScale = ContentScale.FillWidth, // Fill width, height follows aspect ratio
        modifier = Modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .offset {
                IntOffset(
                    (x.value - widthPx / 2f).roundToInt(),
                    y.value.roundToInt()
                )
            }
            .width(widthDp) // Only constrain width, height is natural
    )
}
package net.taler.wallet.oim

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Animates the consolidation of notes/coins
 * Shows old notes flying together and transforming into new consolidated notes
 */
@Composable
fun ConsolidationAnimator(
    oldResourceIds: List<Int>,
    newResourceIds: List<Int>,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationPhase by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        // Phase 0: Show old notes
        delay(100)
        // Phase 1: Fly together
        animationPhase = 1
        delay(400)
        // Phase 2: Transform (swap to new notes)
        animationPhase = 2
        delay(200)
        // Phase 3: Complete
        onComplete()
    }

    Box(modifier = modifier) {
        when (animationPhase) {
            0 -> {
                // Show old notes in original positions
                oldResourceIds.forEachIndexed { index, resId ->
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .offset(x = (index * 20).dp)
                    )
                }
            }
            1 -> {
                // Animate old notes flying to center
                oldResourceIds.forEachIndexed { index, resId ->
                    val offsetAnim = remember {
                        Animatable(
                            initialValue = Offset(index * 20f, 0f),
                            typeConverter = Offset.VectorConverter
                        )
                    }

                    LaunchedEffect(Unit) {
                        offsetAnim.animateTo(
                            targetValue = Offset.Zero,
                            animationSpec = tween(300, easing = FastOutSlowInEasing)
                        )
                    }

                    val alpha by animateFloatAsState(
                        targetValue = 1f - (index * 0.2f).coerceIn(0f, 1f),
                        animationSpec = tween(300)
                    )

                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .offset {
                                IntOffset(
                                    offsetAnim.value.x.roundToInt(),
                                    offsetAnim.value.y.roundToInt()
                                )
                            }
                            .graphicsLayer { this.alpha = alpha }
                    )
                }
            }
            else -> {
                // Show new consolidated notes
                newResourceIds.forEachIndexed { index, resId ->
                    val scale by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )

                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .offset(x = (index * 20).dp)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                    )
                }
            }
        }
    }
}
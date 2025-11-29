package net.taler.wallet.oim.notes

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import net.taler.database.data_models.Amount
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.launch

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
        scale.snapTo(0.5f)  // Start small
        alpha.snapTo(0f)

        // Fade in smoothly
        launch {
            alpha.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }

        // Scale up continuously during the entire flight
        launch {
            scale.animateTo(1.0f, tween(800, easing = FastOutSlowInEasing))
        }

        // Create a curved path by adding a control point
        val midX = (startInRoot.x + endInRoot.x) / 2f
        val midY = startInRoot.y - 150f // Arc upward

        // Move to mid point first (creates the curve)
        launch {
            x.animateTo(midX, tween(400, easing = FastOutSlowInEasing))
            x.animateTo(endInRoot.x, tween(400, easing = FastOutSlowInEasing))
        }

        launch {
            y.animateTo(midY, tween(400, easing = FastOutSlowInEasing))
            y.animateTo(endInRoot.y, tween(400, easing = FastOutSlowInEasing))
        }.join()

        // Fade out smoothly
        alpha.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
        onArrive()
    }

    val density = LocalDensity.current
    val widthDp = with(density) { widthPx.toDp() }

    Image(
        painter = painterResource(noteRes),
        contentDescription = null,
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .scale(scale.value)
            .alpha(alpha.value)
            .offset {
                IntOffset(
                    (x.value - widthPx / 2f).roundToInt(),
                    y.value.roundToInt()
                )
            }
            .width(widthDp)
    )
}

/**
 * ## NotesPile
 *
 * Renders a visually scattered pile of banknotes centered on the screen.
 * Each note is slightly rotated and offset for a natural, layered effect.
 *
 * When tapped, opens a [NotesGalleryOverlay] showing all notes in a
 * horizontally scrollable view.
 *
 * This is typically used alongside [NoteFlyer]. When a flyer animation
 * completes, its bitmap is added to [landedNotes] to appear in the pile.
 *
 * Notes are drawn in the order they appear in [landedNotes]: first items
 * appear at the bottom, later items appear on top.
 *
 * @param landedNotes List of bitmaps representing notes that have landed.
 * @param noteWidthPx Width of each note in pixels in pile view.
 * @param expandedNoteWidth Width of each note in the gallery overlay.
 *
 * @see NoteFlyer for animated flying notes.
 * @see NotesGalleryOverlay for the popup gallery component.
 */
@Composable
fun NotesPile(
    landedNotes: List<ImageBitmap>,
    noteWidthPx: Float,
    expandedNoteWidth: Dp = 180.dp
) {
    var showGallery by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val wDp = with(density) { noteWidthPx.toDp() }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Pile view - clickable to open gallery
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = BiasAlignment(0f, -0.25f)
        ) {
            // Clickable wrapper ONLY around the actual pile
            Box(
                modifier = Modifier
                    .wrapContentSize() // Only as big as the content
                    .clickable { showGallery = true }
            ) {
                // Draw notes bottom-to-top
                landedNotes.forEachIndexed { i, bmp ->
                    val rot = remember(i) { Random.nextFloat() * 18f - 9f }
                    val dx = remember(i) { (Random.nextInt(-10, 10)).dp }
                    val dy = remember(i) { (Random.nextInt(-3, 9)).dp }

                    Image(
                        bitmap = bmp,
                        contentDescription = null,
                        modifier = Modifier
                            .offset(dx, dy)
                            .width(wDp)
                            .graphicsLayer {
                                rotationZ = rot
                            },
                        contentScale = ContentScale.FillWidth
                    )
                }
            }
        }

        // Gallery overlay
        NotesGalleryOverlay(
            isVisible = showGallery,
            onDismiss = { showGallery = false },
            bitmaps = landedNotes
        )
    }
}

/**
 * ## NotesStrip
 *
 * Horizontally scrollable strip of note thumbnails representing available denominations.
 * Notes maintain their aspect ratio based on a fixed height.
 *
 * @param noteThumbHeight Fixed height of each note thumbnail.
 * @param notes List of pairs of drawable resource IDs and their associated [Amount].
 * @param enabledStates List of booleans indicating whether each note is affordable.
 * @param onAddRequest Callback invoked when a note is tapped.
 * @param onRemoveLast Callback invoked to remove the last added [Amount].
 */
@Composable
fun NotesStrip(
    noteThumbHeight: Dp,
    notes: List<Pair<Int, Amount>>,
    enabledStates: List<Boolean> = List(notes.size) { true },
    onAddRequest: (Amount, Offset) -> Unit,
    onRemoveLast: (Amount) -> Unit
) {
    val scroll = rememberScrollState()
    var lastAdded: Amount? by remember { mutableStateOf(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(noteThumbHeight + 10.dp)
            .horizontalScroll(scroll)
            .background(
                color = Color(0x55000000),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        notes.forEachIndexed { index, (res, amount) ->
            val isEnabled = enabledStates.getOrElse(index) { true }
            NoteThumb(
                res = res,
                height = noteThumbHeight,
                enabled = isEnabled
            ) { centerInRoot ->
                if (isEnabled) {
                    lastAdded = amount
                    onAddRequest(amount, centerInRoot)
                }
            }
            Spacer(Modifier.width(14.dp))
        }
    }
}

/**
 * ## NoteThumb
 *
 * Single note thumbnail displayed in [NotesStrip].
 * Maintains aspect ratio based on fixed height.
 *
 * @param res Drawable resource ID of the note.
 * @param height Fixed height of the thumbnail in dp.
 * @param enabled Whether the note can be tapped (greys out if false).
 * @param onTapWithPos Callback invoked on tap.
 */
@Composable
private fun NoteThumb(
    @DrawableRes res: Int,
    height: Dp,
    enabled: Boolean = true,
    onTapWithPos: (centerInRoot: Offset) -> Unit
) {
    var center by remember { mutableStateOf(Offset.Zero) }

    val corner = 8.dp
    val border = 3.dp

    val greyMatrix = ColorMatrix().apply {
        setToSaturation(0f)
    }

    Card(
        modifier = Modifier
            .height(height)
            .onGloballyPositioned { lc -> center = lc.boundsInRoot().center }
            .clickable(enabled = enabled) { onTapWithPos(center) }
            .border(
                border,
                if (enabled) Color(0xFF00D9FF) else Color(0x66FFFFFF),
                RoundedCornerShape(corner)
            )
            .padding(4.dp),
        shape = RoundedCornerShape(corner),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 8.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) Color(0x66FFFFFF) else Color(0x33FFFFFF)
        )
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier
                .height(height)
                .wrapContentWidth(),
            contentScale = ContentScale.FillHeight,
            alpha = if (enabled) 1f else 0.4f,
            colorFilter = if (!enabled) ColorFilter.colorMatrix(greyMatrix) else null
        )
    }
}
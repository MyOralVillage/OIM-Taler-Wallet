package net.taler.wallet.oim.send.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.taler.database.data_models.Amount

/**
 * ## NotesStrip
 *
 * Horizontally scrollable strip of note thumbnails representing available denominations.
 * Tapping a note triggers a flying animation (via [NoteFlyer]) and updates the total
 * amount. The Undo button removes the most recently added note from the total.
 *
 * @param noteThumbWidth Width of each note thumbnail (typically 140–180 dp).
 * @param notes List of pairs of drawable resource IDs and their associated [Amount].
 * @param enabledStates List of booleans indicating whether each note is affordable.
 * @param onAddRequest Callback invoked when a note is tapped; supplies the [Amount]
 *                     and the center position of the thumbnail in root coordinates,
 *                     used to determine the animation start for [NoteFlyer].
 * @param onRemoveLast Callback invoked to remove the last added [Amount] (used by Undo).
 *
 * @see NoteFlyer
 * @see NotesPile
 */
@Composable
fun NotesStrip(
    noteThumbWidth: Dp,
    notes: List<Pair<Int, Amount>>,
    enabledStates: List<Boolean> = List(notes.size) { true }, // Default all enabled
    onAddRequest:(Amount, Offset) -> Unit,
    onRemoveLast: (Amount) -> Unit
) {
    val scroll = rememberScrollState()
    var lastAdded: Amount? by remember { mutableStateOf(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (noteThumbWidth * 0.75f) + 36.dp)
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
                width = noteThumbWidth,
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
 * Captures its screen center for flying animation origin.
 *
 * @param res Drawable resource ID of the note.
 * @param width Width of the thumbnail in dp.
 * @param enabled Whether the note can be tapped (greys out if false).
 * @param onTapWithPos Callback invoked on tap, supplying the center position in root coordinates.
 */
@Composable
private fun NoteThumb(
    @DrawableRes res: Int,
    width: Dp,
    enabled: Boolean = true,
    onTapWithPos: (centerInRoot: Offset) -> Unit
) {
    var center by remember { mutableStateOf(Offset.Zero) }

    val corner = 16.dp
    val border = 3.dp

    // Greyscale color matrix for disabled state
    val greyMatrix = ColorMatrix().apply {
        setToSaturation(0f)
    }

    Card(
        modifier = Modifier
            .width(width) // Only constrain width
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
                .fillMaxWidth()
                .wrapContentHeight(), // Let height wrap to content
            contentScale = ContentScale.FillWidth, // Fill width, maintain aspect ratio
            alpha = if (enabled) 1f else 0.4f,
            colorFilter = if (!enabled) ColorFilter.colorMatrix(greyMatrix) else null
        )
    }
}
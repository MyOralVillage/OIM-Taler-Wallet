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
        notes.forEach { (res, amount) ->
            NoteThumb(
                res = res,
                width = noteThumbWidth
            ) { centerInRoot ->
                lastAdded = amount
                onAddRequest(amount, centerInRoot)
            }
            Spacer(Modifier.width(14.dp))
        }

        Button(
            onClick = { lastAdded?.let(onRemoveLast) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3333)),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            Text("Undo", color = Color.White)
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
 * @param onTapWithPos Callback invoked on tap, supplying the center position in root coordinates.
 */
@Composable
private fun NoteThumb(
    @DrawableRes res: Int,
    width: Dp,
    onTapWithPos: (centerInRoot: Offset) -> Unit
) {
    var center by remember { mutableStateOf(Offset.Zero) }

    val corner = 16.dp
    val border = 3.dp
    val height = width * 0.58f

    Card(
        modifier = Modifier
            .width(width)
            .height(height)
            .onGloballyPositioned { lc -> center = lc.boundsInRoot().center }
            .clickable { onTapWithPos(center) }
            .border(border, Color(0xFF00D9FF), RoundedCornerShape(corner))
            .padding(4.dp),
        shape = RoundedCornerShape(corner),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
    ) {
        Image(
            painter = painterResource(res),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

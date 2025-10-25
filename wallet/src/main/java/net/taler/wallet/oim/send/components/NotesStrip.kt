/**
 * ## NotesStrip
 *
 * Horizontally scrollable strip displaying note thumbnails, used in the OIM
 * *Send* flow for visual money selection. Each note represents an available
 * denomination that can be “sent” as an animated flyer.
 *
 * When a thumbnail is tapped, its on-screen position is reported via
 * [onAddRequest] so the animation origin can be determined for [NoteFlyer].
 * An Undo button allows reversing the most recent addition through
 * [onRemoveLast].
 *
 * @param noteThumbWidth Width of each note thumbnail (typically 140–180 dp).
 * @param notes List of image–amount pairs representing available notes.
 * @param onAddRequest Callback triggered when a note is tapped; supplies
 * the [Amount] and its screen center position.
 * @param onRemoveLast Callback to remove the last added note (used by Undo).
 *
 * @see net.taler.wallet.oim.send.components.NoteFlyer
 * @see net.taler.wallet.oim.send.components.NotesPile
 */

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
<<<<<<< HEAD

<<<<<<< HEAD
<<<<<<< HEAD
//@Composable
//fun NotesStrip(
//    noteThumbWidth: Dp,
//    onAddRequest: (value: Int, path: String, startCenterInRoot: Offset) -> Unit,
//    onRemoveLast: (removed: Int) -> Unit
//) {
//    val scroll = rememberScrollState()
//    var lastAdded by remember { mutableStateOf(0) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .horizontalScroll(scroll)
//            .clip(RoundedCornerShape(20.dp))
//            .background(Color(0x55000000))
//            .padding(10.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        SleNotes.forEach { (path, value) ->
//            NoteThumb(
//                path = path,
//                width = noteThumbWidth,
//                onTapWithPos = { centerInRoot ->
//                    lastAdded = value
//                    onAddRequest(value, path, centerInRoot)
//                }
//            )
//            Spacer(Modifier.width(10.dp))
//        }
//
//        Button(
//            onClick = { if (lastAdded != 0) onRemoveLast(lastAdded) },
//            modifier = Modifier.padding(start = 6.dp)
//        ) { Text("Undo") }
//    }
//}
//
//@Composable
//private fun NoteThumb(
//    path: String,
//    width: Dp,
//    onTapWithPos: (centerInRoot: Offset) -> Unit
//) {
//    var center by remember { mutableStateOf(Offset.Zero) }
//
//    Card(
//        modifier = Modifier
//            .width(width)
//            .height(width * 0.55f)
//            .onGloballyPositioned { lc -> center = lc.boundsInRoot().center }
//            .clickable { onTapWithPos(center) },
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
//        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
//    ) {
//        Image(
//            painter = assetPainterOrPreview(path),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//    }
//}
=======
@Composable
fun NotesStrip(
    noteThumbWidth: Dp,
    onAddRequest: (value: Int, path: String, startCenterInRoot: Offset) -> Unit,
=======
/**
 * Horizontal strip of note thumbnails (bitmap, value) that can be tapped
 * to trigger a flying animation toward the amount total.
 */
@Composable
fun NotesStrip(
    noteThumbWidth: Dp,
    notes: List<Pair<ImageBitmap, Int>>,
    onAddRequest: (value: Int, startCenterInRoot: Offset) -> Unit,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
    onRemoveLast: (removed: Int) -> Unit
=======
import net.taler.database.data_models.Amount

@Composable
fun NotesStrip(
    noteThumbWidth: Dp,
    notes: List<Pair<Int, Amount>>,
    onAddRequest: (Amount, Offset) -> Unit,
    onRemoveLast: (Amount) -> Unit
>>>>>>> 321d128 (updated send to be more dynamic)
) {
    val scroll = rememberScrollState()
    var lastAdded: Amount? by remember { mutableStateOf(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = (noteThumbWidth * 0.75f) + 36.dp) // more headroom
            .horizontalScroll(scroll)
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x55000000))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SleNotes.forEach { (path, value) ->
            NoteThumb(
                path = path,
                width = noteThumbWidth,
                onTapWithPos = { centerInRoot ->
                    lastAdded = value
                    onAddRequest(value, path, centerInRoot)
=======
            .background(Color(0x55000000), RoundedCornerShape(20.dp))
            .padding(10.dp),
=======
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF4444AA), Color(0xFFAA4488))
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .border(3.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
            .padding(12.dp),
>>>>>>> 321d128 (updated send to be more dynamic)
=======
            .padding(horizontal = 16.dp, vertical = 12.dp),
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
        verticalAlignment = Alignment.CenterVertically
    ) {
        notes.forEach { (@DrawableRes r, value) ->
            NoteThumb(
                res = r,
                width = noteThumbWidth,
<<<<<<< HEAD
                onTapWithPos = { centerInRoot ->
                    lastAdded = value
                    onAddRequest(value, centerInRoot)
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
                }
            )
            Spacer(Modifier.width(10.dp))
=======
            ) { centerInRoot ->
                lastAdded = value
                onAddRequest(value, centerInRoot)
            }
            Spacer(Modifier.width(14.dp))
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
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

@Composable
private fun NoteThumb(
<<<<<<< HEAD
<<<<<<< HEAD
    path: String,
=======
    bmp: ImageBitmap,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
    @DrawableRes res: Int,
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
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
            .padding(4.dp),               // inner breathing room prevents visual “cut”
        shape = RoundedCornerShape(corner),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x66FFFFFF))
    ) {
        Image(
<<<<<<< HEAD
<<<<<<< HEAD
            painter = assetPainterOrPreview(path, PreviewAssets.id(path)),
=======
            bitmap = bmp,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
            painter = painterResource(res),
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit      // show whole note instead of cropping
        )
    }
}
<<<<<<< HEAD
>>>>>>> 5c7011a (fixed preview animations)
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)

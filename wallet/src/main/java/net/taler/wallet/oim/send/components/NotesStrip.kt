/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
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
    notes: List<Pair<ImageBitmap, Amount>>,
    onAddRequest: (Amount, Offset) -> Unit,
    onRemoveLast: (Amount) -> Unit
>>>>>>> 321d128 (updated send to be more dynamic)
) {
    val scroll = rememberScrollState()
    var lastAdded: Amount? by remember { mutableStateOf(null) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        notes.forEach { (bmp, value) ->
            NoteThumb(
                bmp = bmp,
                width = noteThumbWidth,
                onTapWithPos = { centerInRoot ->
                    lastAdded = value
                    onAddRequest(value, centerInRoot)
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
                }
            )
            Spacer(Modifier.width(10.dp))
        }

        Button(
            onClick = { lastAdded?.let { onRemoveLast(it) } },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDD3333)),
            modifier = Modifier.padding(start = 6.dp)
        ) {
            Text("Undo", color = Color.White)
        }
    }
}

@Composable
private fun NoteThumb(
<<<<<<< HEAD
    path: String,
=======
    bmp: ImageBitmap,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
    width: Dp,
    onTapWithPos: (centerInRoot: Offset) -> Unit
) {
    var center by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .width(width)
            .height(width * 0.55f)
            .onGloballyPositioned { lc -> center = lc.boundsInRoot().center }
            .clickable { onTapWithPos(center) }
            .border(
                width = 4.dp,
                color = Color(0xFF00D9FF),
                shape = RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x55FFFFFF))
    ) {
        Image(
<<<<<<< HEAD
            painter = assetPainterOrPreview(path, PreviewAssets.id(path)),
=======
            bitmap = bmp,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
<<<<<<< HEAD
>>>>>>> 5c7011a (fixed preview animations)
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)

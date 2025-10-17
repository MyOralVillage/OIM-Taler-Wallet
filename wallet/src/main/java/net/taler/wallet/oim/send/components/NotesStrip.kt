/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NotesStrip(
    noteThumbWidth: Dp,
    onAdd: (Int) -> Unit,
    onRemoveLast: (removed: Int) -> Unit
) {
    val scroll = rememberScrollState()
    var lastAdded by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x55000000))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SleNotes.forEach { (path, value) ->
            NoteThumb(
                path = path,
                width = noteThumbWidth,
                onTap = { lastAdded = value; onAdd(value) }
            )
            Spacer(Modifier.width(10.dp))
        }

        Button(
            onClick = { if (lastAdded != 0) onRemoveLast(lastAdded) },
            modifier = Modifier.padding(start = 6.dp)
        ) { Text("Undo") }
    }
}

@Composable
private fun NoteThumb(path: String, width: Dp, onTap: () -> Unit) {
    Card(
        modifier = Modifier
            .width(width)
            .height(width * 0.55f)
            .clickable { onTap() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
    ) {
        Image(
            painter = assetPainter(path),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

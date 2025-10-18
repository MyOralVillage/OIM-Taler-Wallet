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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NotesStrip(
    noteThumbWidth: Dp,
    onAddRequest: (value: Int, path: String, startCenterInRoot: Offset) -> Unit,
    onRemoveLast: (removed: Int) -> Unit
) {
    val scroll = rememberScrollState()
    var lastAdded by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scroll)
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
                }
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
private fun NoteThumb(
    path: String,
    width: Dp,
    onTapWithPos: (centerInRoot: Offset) -> Unit
) {
    var center by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .width(width)
            .height(width * 0.55f)
            .onGloballyPositioned { lc -> center = lc.boundsInRoot().center }
            .clickable { onTapWithPos(center) },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x22FFFFFF))
    ) {
        Image(
            painter = assetPainterOrPreview(path),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

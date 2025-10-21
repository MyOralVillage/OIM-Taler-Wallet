/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.BoxWithConstraints
import net.taler.wallet.oim.send.components.*

<<<<<<< HEAD
// TODO refactor to use res_mapping_extensions


///**
// * Main screen for sending money with a visual note-based interface.
// *
// * Displays the current balance, allows users to add currency notes to build up
// * an amount to send, and provides options to choose a payment purpose and send money.
// * Features animated note flying effects when adding notes to the send amount.
// *
// * @param balance The current account balance in Leones
// * @param amount The current amount being prepared to send in Leones
// * @param onAdd Callback invoked when a note is successfully added, with the note value
// * @param onRemoveLast Callback invoked when the last note should be removed, with the note value
// * @param onChoosePurpose Callback invoked when the user wants to choose a payment purpose
// * @param onSend Callback invoked when the user confirms sending the money
// */
//@Composable
//fun SendScreen(
//    balance: Int,
//    amount: Int,
//    onAdd: (Int) -> Unit,
//    onRemoveLast: (Int) -> Unit,
//    onChoosePurpose: () -> Unit,
//    onSend: () -> Unit
//) {
//    BoxWithConstraints(Modifier.fillMaxSize()) {
//        // Background image
//        Image(
//            painter = assetPainterOrPreview(WOOD_TABLE),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//
//        /**
//         * Represents a note currently being animated from the strip to the amount display.
//         *
//         * @property value The monetary value of the note in Leones
//         * @property path The resource path to the note image
//         * @property start The starting position offset for the animation
//         */
//        data class Pending(val value: Int, val path: String, val start: Offset)
//
//        var pending by remember { mutableStateOf<Pending?>(null) }
//        val density = LocalDensity.current
//
//        // Calculate the end position for flying notes (center of the amount display area)
//        val endCenter = with(density) {
//            Offset(
//                x = (this@BoxWithConstraints.maxWidth / 2).toPx(),
//                y = (this@BoxWithConstraints.maxHeight * 0.38f).toPx()
//            )
//        }
//
//        Column(
//            Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            OimTopBarCentered(balance = balance, onSendClick = onSend)
//
//            Spacer(Modifier.weight(1f))
//
//            // Amount display and action buttons
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 8.dp, vertical = 12.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // Current send amount
//                Column {
//                    Text(
//                        text = amount.toString(),
//                        color = Color.White,
//                        fontWeight = FontWeight.ExtraBold,
//                        fontSize = 84.sp
//                    )
//                    Text(
//                        text = "Leones",
//                        color = Color.White,
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 32.sp
//                    )
//                }
//
//                // Action buttons
//                Column(horizontalAlignment = Alignment.End) {
//                    Button(onClick = onChoosePurpose) { Text("Choose purpose") }
//                    Spacer(Modifier.height(12.dp))
//                    ExtendedFloatingActionButton(
//                        onClick = onSend,
//                        icon = { androidx.compose.material3.Icon(Icons.Filled.Send, null) },
//                        text = { Text("Send") },
//                        containerColor = MaterialTheme.colorScheme.primary,
//                        contentColor = Color.Black
//                    )
//                }
//            }
//
//            // Strip of available currency notes
//            NotesStrip(
//                noteThumbWidth = 120.dp,
//                onAddRequest = { value, path, startCenter ->
//                    pending = Pending(value, path, startCenter)
//                },
//                onRemoveLast = onRemoveLast
//            )
//            Spacer(Modifier.height(8.dp))
//        }
//
//        // Animated flying note effect
//        pending?.let { p ->
//            val widthPx = with(density) { 160.dp.toPx() }
//            NoteFlyer(
//                path = p.path,
//                startInRoot = p.start,
//                endInRoot = endCenter,
//                widthPx = widthPx,
//                onArrive = {
//                    onAdd(p.value)
//                    pending = null
//                }
//            )
//        }
//    }
//}
=======
@Composable
fun SendScreen(
    balance: Int,
    amount: Int,
    onAdd: (Int) -> Unit,
    onRemoveLast: (Int) -> Unit,
    onChoosePurpose: () -> Unit,
    onSend: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        Image(
            painter = assetPainterOrPreview(WOOD_TABLE, PreviewAssets.id(WOOD_TABLE)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        data class Pending(val value: Int, val path: String, val start: Offset)
        var pending by remember { mutableStateOf<Pending?>(null) }
        val density = LocalDensity.current
        val endCenter = remember(maxWidth, maxHeight) {
            val x = with(density) { (maxWidth / 2).toPx() }
            val y = with(density) { (maxHeight * 0.38f).toPx() }
            Offset(x, y)
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OimTopBarCentered(balance = balance, onSendClick = onSend)

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = amount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 84.sp
                    )
                    Text(
                        text = "Leones",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Button(onClick = onChoosePurpose) { Text("Choose purpose") }
                    Spacer(Modifier.height(12.dp))
                    ExtendedFloatingActionButton(
                        onClick = onSend,
                        icon = { androidx.compose.material3.Icon(Icons.Filled.Send, null) },
                        text = { Text("Send") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                }
            }

            NotesStrip(
                noteThumbWidth = 120.dp,
                onAddRequest = { value, path, startCenter ->
                    pending = Pending(value, path, startCenter)
                },
                onRemoveLast = onRemoveLast
            )
            Spacer(Modifier.height(8.dp))
        }

        pending?.let { p ->
            val widthPx = with(density) { 160.dp.toPx() }
            NoteFlyer(
                path = p.path,
                startInRoot = p.start,
                endInRoot = endCenter,
                widthPx = widthPx,
                onArrive = {
                    onAdd(p.value)
                    pending = null
                }
            )
        }
    }
}
>>>>>>> 5c7011a (fixed preview animations)

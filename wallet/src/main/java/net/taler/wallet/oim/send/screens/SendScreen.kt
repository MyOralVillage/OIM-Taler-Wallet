/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.tooling.preview.Preview
import net.taler.wallet.oim.send.components.*
import net.taler.wallet.oim.res_mapping_extensions.SLE_BILLS_CENTS
import net.taler.common.R

<<<<<<< HEAD
<<<<<<< HEAD
// TODO refactor to use res_mapping_extensions
=======
/**
 * Main send screen (drawable-backed UI using res_mapping_extensions).
 */
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
        // Background (dark wood variant)
        WoodTableBackground(modifier = Modifier.fillMaxSize(), light = false)
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)

        // Animated note state
        data class Pending(val value: Int, val bmp: ImageBitmap, val start: Offset)
        var pending by remember { mutableStateOf<Pending?>(null) }
        val density = LocalDensity.current

<<<<<<< HEAD
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
=======
        val endCenter = with(density) {
            Offset(
                x = (this@BoxWithConstraints.maxWidth / 2).toPx(),
                y = (this@BoxWithConstraints.maxHeight * 0.38f).toPx()
            )
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
<<<<<<< HEAD
=======
            // Top bar with balance + send
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
            OimTopBarCentered(balance = balance, onSendClick = onSend)

            Spacer(Modifier.weight(1f))

<<<<<<< HEAD
=======
            // Amount + actions
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
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
<<<<<<< HEAD
=======

>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
                Column(horizontalAlignment = Alignment.End) {
                    Button(onClick = onChoosePurpose) { Text("Choose purpose") }
                    Spacer(Modifier.height(12.dp))
                    ExtendedFloatingActionButton(
                        onClick = onSend,
<<<<<<< HEAD
                        icon = { androidx.compose.material3.Icon(Icons.Filled.Send, null) },
=======
                        icon = { Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null) },
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
                        text = { Text("Send") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                }
            }

<<<<<<< HEAD
            NotesStrip(
                noteThumbWidth = 120.dp,
                onAddRequest = { value, path, startCenter ->
                    pending = Pending(value, path, startCenter)
=======
            // Build note thumbnails in *composable* scope (no remember; imageResource is @Composable)
            val thumbNotes: List<Pair<ImageBitmap, Int>> =
                SLE_BILLS_CENTS
                    .filter { it.first >= 100 }               // >= 1 SLE (values are cents)
                    .take(7)
                    .map { (cents, resId) ->
                        ImageBitmap.imageResource(resId) to (cents / 100)
                    }

            // Fallback bitmap computed in composable scope (not inside lambda)
            val fallbackBmp = ImageBitmap.imageResource(R.drawable.sle_one)

            NotesStrip(
                noteThumbWidth = 120.dp,
                notes = thumbNotes,
                onAddRequest = { value, startCenter ->
                    val bmp = thumbNotes.firstOrNull { it.second == value }?.first ?: fallbackBmp
                    pending = Pending(value, bmp, startCenter)
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
                },
                onRemoveLast = onRemoveLast
            )
            Spacer(Modifier.height(8.dp))
        }

<<<<<<< HEAD
        pending?.let { p ->
            val widthPx = with(density) { 160.dp.toPx() }
            NoteFlyer(
                path = p.path,
=======
        // Flying note animation
        pending?.let { p ->
            val widthPx = with(density) { 160.dp.toPx() }
            NoteFlyer(
                noteBitmap = p.bmp,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
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
<<<<<<< HEAD
>>>>>>> 5c7011a (fixed preview animations)
=======

@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun SendScreenPreview() {
    MaterialTheme {
        SendScreen(
            balance = 25,
            amount = 7,
            onAdd = {},
            onRemoveLast = {},
            onChoosePurpose = {},
            onSend = {}
        )
    }
}
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)

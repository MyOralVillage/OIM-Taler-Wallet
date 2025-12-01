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
package net.taler.wallet.oim.notes

/**
 * NOTES MODULE – SINGLE NOTE FULL-SCREEN PREVIEW
 *
 * Renders a single banknote as a modal, full-screen overlay with a dimmed
 * background and a close affordance. This is used when the user wants to
 * inspect one note at a larger size (e.g., from galleries or stacks).
 *
 * Behaviour:
 *  - Tapping anywhere on the backdrop or close button dismisses the overlay.
 *  - The component is purely visual and stateless beyond the [onDismiss] call.
 *
 * Integrated from:
 *  - Notes galleries (overlay + screen)
 *  - Any future flows that expose a zoomed-in note view.
 */


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import net.taler.wallet.oim.utils.res_mappers.UIIcons

/**
 * A full-screen overlay that displays a single banknote in a larger view.
 *
 * @param noteResId The drawable resource ID of the note to display.
 * @param onDismiss Callback invoked when the overlay is dismissed (tap outside or close button).
 */
@Composable
fun NotePreviewOverlay(
    noteResId: Int,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = noteResId),
            contentDescription = "Expanded banknote",
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .wrapContentHeight(),
            contentScale = ContentScale.Fit
        )

        // Close button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(32.dp)
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = UIIcons("redcross").resourceMapper(),
                contentDescription = "Close",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

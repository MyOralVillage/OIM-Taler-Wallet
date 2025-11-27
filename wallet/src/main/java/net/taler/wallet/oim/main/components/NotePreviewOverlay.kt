package net.taler.wallet.oim.main.components

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
import net.taler.wallet.oim.utils.resourceMappers.UIIcons

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

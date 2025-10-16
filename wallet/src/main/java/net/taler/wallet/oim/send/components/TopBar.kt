/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OimTopBar(
    balance: Int,
    onSendClick: () -> Unit
) {
    val isPreview = LocalInspectionMode.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val chestPainter = if (isPreview) null else assetPainter(ICON_CHEST)
            if (chestPainter == null) {
                Box(
                    Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF7EB2FF))
                )
            } else {
                Image(
                    painter = chestPainter,
                    contentDescription = "Chest",
                    modifier = Modifier.size(64.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "$balance Leones",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        val sendPainter = if (isPreview) null else assetPainter(ICON_SEND)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0x33000000))
                .clickable { onSendClick() },
            contentAlignment = Alignment.Center
        ) {
            if (sendPainter == null) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFD27E))
                )
            } else {
                Image(
                    painter = sendPainter,
                    contentDescription = "Send",
                    modifier = Modifier.size(44.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.res_mapping_extensions.Buttons
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper

/**
 * ## OimTopBarCentered
 *
 * Center-aligned top bar displaying the user’s wallet balance and a *Send* button
 * at the top-right corner. Shows a chest icon for visual anchoring of the balance.
 *
 * The Send and Chest images are resolved via [Buttons] resource mapping.
 *
 * @param balance Current wallet [Amount] displayed in the top-center.
 * @param onSendClick Callback invoked when the Send button is pressed.
 */
@Composable
fun OimTopBarCentered(
    balance: Amount,
    onSendClick: () -> Unit
) {
    val sendBitmap = Buttons("send").resourceMapper()
    val chestBitmap = Buttons("chest_open").resourceMapper()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Top-right SEND button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(64.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0x33000000))
                .clickable { onSendClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = ImageBitmap.imageResource(sendBitmap),
                contentDescription = "Send",
                modifier = Modifier.size(44.dp),
                contentScale = ContentScale.Fit
            )
        }

        // Centered chest + balance
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(chestBitmap),
                contentDescription = "Chest",
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${balance.amountStr} ${balance.spec?.name ?: balance.currency}",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

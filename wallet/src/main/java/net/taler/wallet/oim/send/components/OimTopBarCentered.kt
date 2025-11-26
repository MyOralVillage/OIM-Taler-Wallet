package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.wallet.MainViewModel
import net.taler.wallet.oim.res_mapping_extensions.UIIcons

/**
 * ## OimTopBarCentered
 *
 * Center-aligned top bar displaying the user's wallet balance and a *Send* button
 * at the top-right corner. Shows a chest icon for visual anchoring of the balance.
 *
 * The Send and Chest images are resolved via [UIICons] resource mapping.
 *
 * @param balance Current wallet [Amount] displayed in the top-center.
 * @param onSendClick Callback invoked when the Send button is pressed.
 */
@Composable
fun OimTopBarCentered(
    balance: Amount,
    onSendClick: () -> Unit,
    onChestClick: () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // LEFT: chest + balance
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Image(
                bitmap = UIIcons("chest_open").resourceMapper(),
                contentDescription = "Chest",
                modifier = Modifier
                    .size(60.dp)
                    .clickable { onChestClick() },   // <-- make it do something
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(18.dp))
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = balance.amountStr,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = balance.currency,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // RIGHT: send button
        Box(
            modifier = Modifier
                .size(75.dp)
                .background(
                    color = Color(0xD0C32909),
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { onSendClick() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = UIIcons("send").resourceMapper(),
                contentDescription = "Send",
                modifier = Modifier.size(65.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

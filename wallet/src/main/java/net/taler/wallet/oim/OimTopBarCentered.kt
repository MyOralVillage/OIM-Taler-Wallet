package net.taler.wallet.oim

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.resourceMappers.UIIcons

/**
 * ## OimTopBarCentered
 *
 * Center-aligned top bar displaying the user's wallet balance
 * at the top-right corner. Shows a chest icon for visual anchoring of the balance.
 *
 * @param balance Current wallet [Amount] displayed in the top-center.
 * @param onChestClick function that maps map clicks; default to nothing
 * @param color the background color; alpha is internally set to 0.65f
 */
@Composable
fun OimTopBarCentered(
    balance: Amount,
    onChestClick: () -> Unit = {},
    colour: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colour.copy(alpha=0.65f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Chest icon button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clickable(onClick = onChestClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = UIIcons("chest_open").resourceMapper(),
                    contentDescription = "Chest",
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Balance centered
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = balance.amountStr,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(
                    text = balance.currency,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic
                )
            }
        }
    }
}

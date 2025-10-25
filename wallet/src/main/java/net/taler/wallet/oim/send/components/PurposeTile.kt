package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A tile representing a selectable purpose in the OIM Send flow.
 *
 * Displays a bitmap icon above a label. Clicking the tile triggers [onPick].
 *
 * @param bitmap Icon representing the purpose.
 * @param label Text label for the purpose.
 * @param modifier Optional [Modifier] for styling/layout.
 * @param onPick Callback invoked when the tile is selected.
 */
@Composable
fun PurposeTile(
    bitmap: ImageBitmap,
    label: String,
    modifier: Modifier = Modifier,
    onPick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onPick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xAAFFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                bitmap = bitmap,
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

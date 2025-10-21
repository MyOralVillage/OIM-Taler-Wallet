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

package net.taler.wallet.oim.history.filter

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.taler.common.R.drawable.incoming_transaction
import net.taler.common.R.drawable.outgoing_transaction

/**
 * Reusable card component for displaying a direction filter option with PNG image.
 *
 * @param icon The bitmap of the image to display
 * @param colour The color to use for the selection border and background tint
 * @param isSelected Whether this direction is currently selected
 * @param onClick Callback invoked when the user taps this card
 * @param modifier Optional modifier for customizing the card's layout
 */
@Composable
internal fun DirectionCard(
    icon: ImageBitmap,
    colour: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) colour else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isSelected) colour.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = icon,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Composable that returns a row of two direction filter cards.
 *
 * Displays incoming and outgoing transaction direction cards side by side.
 * Can be used independently in any composable.
 *
 * @param incomingSelected Whether the incoming card is selected
 * @param outgoingSelected Whether the outgoing card is selected
 * @param onIncomingClicked Callback when incoming card is clicked
 * @param onOutgoingClicked Callback when outgoing card is clicked
 * @param modifier Optional modifier for customizing the row's layout
 */
@Composable
fun DirectionFilterRow(
    incomingSelected: Boolean,
    outgoingSelected: Boolean,
    onIncomingClicked: () -> Unit,
    onOutgoingClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val incomingImage = ImageBitmap.imageResource(incoming_transaction)
    val outgoingImage = ImageBitmap.imageResource(outgoing_transaction)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DirectionCard(
            icon = incomingImage,
            colour = Color(0xFF4CAF50),
            isSelected = incomingSelected,
            onClick = onIncomingClicked,
            modifier = Modifier.weight(1f)
        )

        DirectionCard(
            icon = outgoingImage,
            colour = Color(0xFFF44336),
            isSelected = outgoingSelected,
            onClick = onOutgoingClicked,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 200)
@Composable
private fun DirectionFilterRowPreview() {
    MaterialTheme {
        DirectionFilterRow(
            incomingSelected = true,
            outgoingSelected = false,
            onIncomingClicked = { },
            onOutgoingClicked = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}
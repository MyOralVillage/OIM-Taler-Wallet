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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.taler.database.filter.DirectionFilter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.tooling.preview.Preview
import net.taler.database.data_models.FilterableDirection
import net.taler.common.R.drawable.*

// uncomment if you want to load from assets
// import java.io.IOException
// import androidx.compose.ui.platform.LocalContext
// import androidx.compose.ui.graphics.asImageBitmap
// import android.graphics.BitmapFactory
// import android.graphics.Bitmap

/**
 * Reusable card component for displaying a direction filter option with PNG image.
 *
 * The card provides visual feedback through colored borders and background tinting
 * when selected. Designed for touch interaction with low-literacy users in mind.
 *
 * @param icon The bitmap of the image to display
 * @param colour The color to use for the selection border and background tint
 * @param isSelected Whether this direction is currently selected in the filter
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
    // each direction is "clickable" and will be
    // highlighted with associated colour
    Card(

        // create clickable card which will highlight when clicked
        modifier = modifier
            .height(120.dp)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) colour else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),

        // slightly tint background
        colors = CardDefaults.cardColors(
            containerColor =
                if (isSelected) colour.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface
        ),

        // add a small shadow
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
                contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = icon,
                contentDescription = null, // no text
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}
/**
 * Composable section for filtering transactions by direction (incoming/outgoing).
 *
 * Displays two visual cards representing incoming and outgoing transaction types.
 * Users can select one or both directions to filter their transaction history.
 * By default (when [filt] is null), both directions are visually selected, indicating
 * no filtering is applied.
 *
 * Visual feedback:
 * - Selected cards display a colored border and tinted background
 * - Green (#4CAF50) for incoming transactions
 * - Red (#F44336) for outgoing transactions
 *
 * Selection behavior:
 * - Clicking a card toggles only that card
 * - If both directions are deselected, defaults to null (show both)
 *
 * @param filt The current direction filter applied. null indicates no filter (show all),
 * @param onDirectionSelected Callback invoked when the user changes the direction filter selection.
 * @param selectedDirections Set of currently selected filterable directions.
 */
@Composable
internal fun DirectionSection(
    filt: DirectionFilter?,
    onDirectionSelected: (DirectionFilter?) -> Unit,
    selectedDirections: Set<FilterableDirection>,
) {

    // Load images
    val incomingImage = ImageBitmap.imageResource(incoming_transaction)
    val outgoingImage = ImageBitmap.imageResource(outgoing_transaction)

    // Derive which directions are selected based on the filter
    val selectedSet = remember(filt) {
        derivedStateOf {
            when (filt) {
                is DirectionFilter.Exact ->
                    setOf(filt.direction)
                is DirectionFilter.Both ->
                    setOf(FilterableDirection.INCOMING, FilterableDirection.OUTGOING)
                null -> emptySet()
            }
        }
    }.value

    val isIncomingSelected = selectedSet.contains(FilterableDirection.INCOMING)
    val isOutgoingSelected = selectedSet.contains(FilterableDirection.OUTGOING)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // incoming transaction card
            incomingImage.let { image ->
                DirectionCard(
                    icon = image,
                    colour = Color(0xFF4CAF50),
                    isSelected = isIncomingSelected,
                    onClick = {
                        // Toggle incoming in the selected set
                        val newSet = selectedSet.toMutableSet()
                        if (newSet.contains(FilterableDirection.INCOMING)) {
                            newSet.remove(FilterableDirection.INCOMING)
                        } else {
                            newSet.add(FilterableDirection.INCOMING)
                        }

                        // If empty, default to null (show both)
                        val newFilt = when {
                            newSet.isEmpty() -> null
                            newSet.size == 2 -> DirectionFilter.Both
                            else -> DirectionFilter.Exact(newSet.first())
                        }

                        onDirectionSelected(newFilt)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // outgoing transaction card
            outgoingImage.let { image ->
                DirectionCard(
                    icon = image,
                    colour = Color(0xFFF44336),
                    isSelected = isOutgoingSelected,
                    onClick = {
                        // Toggle outgoing in the selected set
                        val newSet = selectedSet.toMutableSet()
                        if (newSet.contains(FilterableDirection.OUTGOING)) {
                            newSet.remove(FilterableDirection.OUTGOING)
                        } else {
                            newSet.add(FilterableDirection.OUTGOING)
                        }

                        // If empty, default to null (show both)
                        val newFilt = when {
                            newSet.isEmpty() -> null
                            newSet.size == 2 -> DirectionFilter.Both
                            else -> DirectionFilter.Exact(newSet.first())
                        }

                        onDirectionSelected(newFilt)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



// previews

@Preview(showBackground = true, widthDp = 400, heightDp = 300)
@Composable
private fun DirectionSectionPreview() {
    // Dummy parameters for preview
    var filt by remember { mutableStateOf<DirectionFilter?>(null) }

    // Replace these with your real image resources
    val incomingImage = ImageBitmap.imageResource(incoming_transaction)
    val outgoingImage = ImageBitmap.imageResource(outgoing_transaction)

    // Wrap in your app theme if you have one
    MaterialTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            DirectionSection(
                filt = filt,
                onDirectionSelected = { filt = it },
                selectedDirections = setOf(
                    FilterableDirection.INCOMING,
                    FilterableDirection.OUTGOING
                )
            )
        }
    }
}
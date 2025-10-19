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

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import net.taler.database.data_models.TranxPurp
import net.taler.database.filter.PurposeFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import previewMapper

/**
 * Loads an image bitmap from a [TranxPurp]
 * @return ImageBitmap
 */
@SuppressLint("DiscouragedApi", "LocalContextResourcesRead")
@Composable
fun TranxPurp.loadBitmap(isPreview: Boolean = false): ImageBitmap {
    return if (isPreview) {
        this.previewMapper()
    } else {
        val context = LocalContext.current
        val resId = context.resources.getIdentifier(
            this.assetPath,
            "drawable",
            context.packageName
        )
        ImageBitmap.imageResource(resId)
    }
}

/**
 * Individual purpose card displaying a bitmap image.
 *
 * @param tranxPurp The transaction purpose object
 * @param isSelected Whether this purpose is currently selected
 * @param onClick Callback invoked when the card is tapped
 * @param modifier Modifier for customizing the card layout
 */
@Composable
internal fun PurposeCard(
    tranxPurp: TranxPurp,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPreview: Boolean = false
) {
    val colour = Color(tranxPurp.colourInt())
    Card(
        modifier = modifier
            .aspectRatio(0.5f)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) colour else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colour.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = tranxPurp.loadBitmap(isPreview),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}


/**
 * Composable grid of purpose filter cards - completely generic.
 * Automatically groups purposes by their TYPE field and sorts within groups.
 * Supports selecting multiple purposes.
 *
 * @param purposeFilter Currently selected purpose filter, or null if none selected
 * @param onPurposeSelected Callback invoked when a purpose is selected or deselected
 * @param purposeMap Map of purpose identifiers to TranxPurp objects (e.g., tranxPurpLookup)
 * @param columns Number of cards per row
 */
@Composable
internal fun PurposeGrid(
    purposeFilter: PurposeFilter?,
    onPurposeSelected: (PurposeFilter?) -> Unit,
    purposeMap: Map<String, TranxPurp>,
    columns: Int = 4,
    isPreview : Boolean
) {
    // Determine which purposes are currently selected
    val selectedPurposes = when (purposeFilter) {
        is PurposeFilter.Exact -> setOf(purposeFilter.purpose)
        is PurposeFilter.OneOrMoreOf -> purposeFilter.purposes
        null -> emptySet()
    }

    // Group by tranxGroup, then sort each group by cmp
    val sortedPurposes = remember(purposeMap) {
        purposeMap.values
            .groupBy { it.tranxGroup }  // Group by TYPE field
            .toSortedMap()  // Sort the groups by TYPE
            .flatMap { (_, purposes) ->
                purposes.sortedBy { it.cmp }  // Sort within each group by cmp
            }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        sortedPurposes.chunked(columns).forEach { rowPurposes ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowPurposes.forEach { tranxPurp ->
                    PurposeCard(
                        tranxPurp = tranxPurp,
                        isSelected = tranxPurp in selectedPurposes,
                        onClick = {
                            val newFilter = togglePurpose(purposeFilter, tranxPurp)
                            onPurposeSelected(newFilter)
                        },
                        modifier = Modifier.weight(1f),
                        isPreview = isPreview
                    )
                }
                repeat(columns - rowPurposes.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Toggles a purpose in the filter, handling the different filter states.
 *
 * @param currentFilter The current purpose filter state
 * @param purpose The purpose to toggle
 * @return The new filter state after toggling
 */
private fun togglePurpose(
    currentFilter: PurposeFilter?,
    purpose: TranxPurp
): PurposeFilter? {
    return when (currentFilter) {
        null -> {
            // No filter, add this purpose
            PurposeFilter.Exact(purpose)
        }
        is PurposeFilter.Exact -> {
            if (currentFilter.purpose == purpose) {
                // Same purpose selected, deselect (no filter)
                null
            } else {
                // Different purpose, now we have two selected
                PurposeFilter.OneOrMoreOf(setOf(currentFilter.purpose, purpose))
            }
        }
        is PurposeFilter.OneOrMoreOf -> {
            if (purpose in currentFilter.purposes) {
                // Remove this purpose from the set
                val newPurposes = currentFilter.purposes - purpose
                when (newPurposes.size) {
                    0 -> null  // No purposes left
                    1 -> PurposeFilter.Exact(newPurposes.first())  // Only one left
                    else -> PurposeFilter.OneOrMoreOf(newPurposes)  // Multiple left
                }
            } else {
                // Add this purpose to the set
                PurposeFilter.OneOrMoreOf(currentFilter.purposes + purpose)
            }
        }
    }
}

// preview map: should be replaced when prod ready

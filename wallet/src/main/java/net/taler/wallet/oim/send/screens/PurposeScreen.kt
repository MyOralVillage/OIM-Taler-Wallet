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
package net.taler.wallet.oim.send.screens

/**
 * SEND MODULE – PURPOSE SELECTION UI
 *
 * This file implements the visual grid used to choose a transaction purpose
 * during the send flow.
 *
 * MAIN COMPOSABLES:
 *  - PurposeCard():
 *      • Displays a single TranxPurp as a square card with its icon.
 *      • Highlights the selected state via border colour and background tint.
 *      • Invokes an `onClick` callback when tapped.
 *
 *  - PurposeScreen():
 *      • Shows all available purposes from `tranxPurpLookup` in a
 *        scrollable LazyVerticalGrid.
 *      • Tracks the currently selected TranxPurp in local state.
 *      • Immediately calls `onDone(purpose)` when a purpose is chosen.
 *
 * INTEGRATION:
 *  - Used from the send flow orchestration in `SendApp` / `SendScreen`,
 *    after the user has picked an amount.
 *  - Uses shared visual assets: `WoodTableBackground` and purpose icons
 *    via `TranxPurp.resourceMapper()`.
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
import net.taler.database.data_models.tranxPurpLookup
import net.taler.wallet.oim.utils.assets.WoodTableBackground
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import net.taler.wallet.oim.utils.res_mappers.resourceMapper

/**
 * Card representing a single transaction purpose.
 *
 * Displays the purpose icon, highlights selection, and triggers [onClick] when tapped.
 *
 * @param tranxPurp The purpose data (icon, label, color).
 * @param isSelected Whether this purpose is currently selected.
 * @param onClick Lambda invoked when the card is tapped.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
fun PurposeCard(
    tranxPurp: TranxPurp,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                width = 5.dp,
                color = if (isSelected) Color.Unspecified else Color(tranxPurp.colourInt()),
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor =
            if (isSelected) Color(tranxPurp.colourInt()).copy(alpha=0.6f)
            else (Color.White.copy(alpha=0.21f))),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(tranxPurp.resourceMapper()),
                contentDescription = tranxPurp.cmp,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Purpose selection screen.
 *
 * Shows all available transaction purposes in a scrollable grid.
 * Only one purpose can be selected at a time. Selected purpose is returned via [onDone].
 *
 * @param balance Current wallet balance displayed at the top.
 * @param onBack Lambda invoked when the back button is pressed.
 * @param onDone Lambda invoked when a purpose is selected; returns the selected [TranxPurp].
 * @param columns Number of columns in the grid layout.
 */
@Composable
fun PurposeScreen(
    balance: Amount,
    onBack: () -> Unit,
    onDone: (TranxPurp) -> Unit,
    columns: Int = 6,
    onHome: () -> Unit = {}
) {
    var selected by remember { mutableStateOf<TranxPurp?>(null) }

    Box(Modifier.fillMaxSize().statusBarsPadding()) {
        WoodTableBackground()

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {

                // Scrollable grid of purposes
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sortedPurposes = tranxPurpLookup.values
                        .sortedBy { it.cmp }

                    items(sortedPurposes) { p ->
                        PurposeCard(
                            tranxPurp = p,
                            isSelected = (p == selected),
                            onClick = {
                                selected = p
                                onDone(p)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                }
            }
        }
    }

/**
 * Preview for PurposeScreen.
 */
@Preview(showBackground = true,device = "spec:width=920dp,height=460dp,orientation=landscape"
)
@Composable
fun PurposeScreenPreview() {
    MaterialTheme {
        PurposeScreen(
            balance = Amount.fromString("KUDOS", "35"),
            onBack = {},
            onDone = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = " Small Landscape Phone 640x360dp (xhdpi)",
    device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape"
)
@Composable
fun PurposeScreenPreview_SmallPhoneXhdpi() {
    PurposeScreenPreview()
}
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

package net.taler.wallet.oim.history.components

import net.taler.wallet.oim.history.components.TransactionCard
import net.taler.common.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TransactionsListView() {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = assetPainterOrPreview(WOOD_TABLE),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Three column layout
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            // First Column - 10%
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.1f)
            ) {
                // Left sidebar content (if needed)
            }

            // Second Column - 70% (Main content with TransactionCards)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.6f)
                    .verticalScroll(rememberScrollState())
            ) {
                TransactionCard(
                    type = "S",
                    amount = "25",
                    currency = "Leones",
                    date = "25 Sept 2025",
                )
                TransactionCard(
                    type = "R",
                    amount = "20",
                    currency = "Leones",
                    date = "25 Sept 2025",
                )
                TransactionCard(
                    type = "S",
                    amount = "5",
                    currency = "Leones",
                    date = "25 Sept 2025",
                )
            }

            // Third Column - 20%
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.3f)
            ) {
                // Right sidebar content (if needed)
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    name = "Transaction List View Preview",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
fun TransactionsListViewPreview() {
    MaterialTheme {
        TransactionsListView()
    }
}
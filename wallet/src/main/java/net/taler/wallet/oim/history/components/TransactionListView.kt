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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwipeDownAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.taler.wallet.oim.history.filter.DirectionFilterRow
import net.taler.database.data_models.*
import net.taler.database.TranxHistory
import net.taler.database.filter.PurposeFilter
import net.taler.wallet.BuildConfig
import net.taler.wallet.oim.history.filter.PurposeGrid
import net.taler.wallet.oim.res_mapping_extensions.Background
import net.taler.wallet.oim.res_mapping_extensions.Buttons
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsListView() {
    var showFilterOverlay by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        BackgroundImage()
        MainLayout()

        // Filter button
        IconButton(
            onClick = {
                isPressed = true
                showFilterOverlay = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .size(100.dp),
        ) {
            Icon(
                painter = painterResource(Buttons("filter").resourceMapper()),
                contentDescription = "null",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = if (isPressed) 1f else 0.8f
                        scaleY = if (isPressed) 1f else 0.8f
                    }
                    .background(
                        color = Color.Black.copy(alpha = 0.3f),
                        shape = MaterialTheme.shapes.medium
                    ),
                tint = Color.Unspecified,
            )
        }

        // Reset pressed state after animation
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(500)
                isPressed = false
            }
        }

        // Filter overlay (shown when button clicked)
        if (showFilterOverlay) {
            FilterOverlay(
                onDismiss = { showFilterOverlay = false }
            )
        }
    }
}

@Composable
private fun FilterOverlay(
    onDismiss: () -> Unit
) {
    // Semi-transparent background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
    ) {
        // Filter content card
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {}

                Spacer(modifier = Modifier.height(16.dp))

                // Filter content (scrollable)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Direction Filter
                    var incomingSelected by remember { mutableStateOf(true) }
                    var outgoingSelected by remember { mutableStateOf(true) }

                    DirectionFilterRow(
                        incomingSelected = incomingSelected,
                        outgoingSelected = outgoingSelected,
                        onIncomingClicked = { incomingSelected = !incomingSelected },
                        onOutgoingClicked = { outgoingSelected = !outgoingSelected }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Purpose Filter
                    var purposeFilter by remember { mutableStateOf<PurposeFilter?>(null) }

                    PurposeGrid(
                        purposeFilter = purposeFilter,
                        onPurposeSelected = { purposeFilter = it },
                        purposeMap = tranxPurpLookup
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close icon button (centered at bottom)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.SwipeDownAlt,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun BackgroundImage() {
    val context = LocalContext.current
    Image(
        painter = painterResource(Background(context).resourceMapper()),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun MainLayout() {
    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        MainContent()
    }
}

@Composable
@OptIn(kotlinx.serialization.InternalSerializationApi::class)
private fun RowScope.MainContent() {
    val context = LocalContext.current
    var dbTransactions by remember { mutableStateOf<List<Tranx>>(emptyList()) }

    LaunchedEffect(Unit) {
        // Initialize database
        if (BuildConfig.DEBUG) TranxHistory.initTest(context)
        else TranxHistory.init(context)

        dbTransactions = TranxHistory.getHistory()
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.1f)
            .verticalScroll(rememberScrollState())
    ){

    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.8f)
            .verticalScroll(rememberScrollState())
    ) {
        dbTransactions.forEach { transaction: Tranx ->
            TransactionCard(
                amount = transaction.amount.toString(false),
                currency = transaction.amount.currency,
                date = transaction.datetime.fmtString(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                purpose = transaction.purpose,
                dir = transaction.direction,
                displayAmount = transaction.amount
            )
        }
//        TransactionCard(
//            amount = "24.5",
//            currency ="SLE",
//            date = "2025-10-28",
//            purpose = tranxPurpLookup["EXPN_GRCR"],
//            dir = FilterableDirection.OUTGOING,
//            displayAmount = Amount(
//                currency = "SLE",
//                value = 24L,
//                fraction = 50_000_000
//            )
//        )
//        TransactionCard(
//            amount = "28.5",
//            currency ="SLE",
//            date = "2025-10-28",
//            purpose = tranxPurpLookup["EXPN_GRCR"],
//            dir = FilterableDirection.OUTGOING,
//            displayAmount = Amount(
//                currency = "SLE",
//                value = 0L,
//                fraction = 50_000_000
//            )
//        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Transaction List View Preview",
    device = "spec:width=411dp,height=891dp,orientation=landscape"
)
@Composable
fun TransactionsListViewPreview() {
    MaterialTheme {
        TransactionsListView()
    }
}

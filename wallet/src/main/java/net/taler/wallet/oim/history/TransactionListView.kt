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

package net.taler.wallet.oim.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.InternalSerializationApi
import net.taler.database.data_models.*
import net.taler.database.TranxHistory
import net.taler.wallet.BuildConfig
import net.taler.wallet.oim.OimColours
import net.taler.wallet.oim.OimTopBarCentered
import net.taler.wallet.oim.resourceMappers.Background
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsListView(balance: Amount, onHome: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Background
        BackgroundImage()

        // Main content in a Column like SendScreen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // OIM Top Bar at the top
            OimTopBarCentered(
                balance = balance,
                onChestClick = onHome,
                colour = OimColours.TRX_HIST_COLOUR,
            )

            // Scrollable content below
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                MainContent()
            }
        }
    }
}

/**
 * Preview: Standard Pixel 5 device
 * Shows the transaction list with top bar
 */
@Preview(
    showBackground = true,
    device = "id:pixel_5",
    name = "Transactions List - Pixel 5"
)
@Composable
fun TransactionsListViewPreview() {
    MaterialTheme {
        val mockBalance = Amount.fromString("EUR", "1250.75")

        TransactionsListView(
            balance = mockBalance,
            onHome = { }
        )
    }
}

/**
 * Preview: Different currency
 * Shows how the top bar looks with different balance
 */
@Preview(
    showBackground = true,
    device = "id:pixel_5",
    name = "Transactions List - SLE"
)
@Composable
fun TransactionsListViewPreviewSLE() {
    MaterialTheme {
        val mockBalance = Amount.fromString("SLE", "45.50")

        TransactionsListView(
            balance = mockBalance,
            onHome = { }
        )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ) {
        MainContent()
    }
}

@Composable
@OptIn(InternalSerializationApi::class)
private fun MainContent() {
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
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // left column kept empty intentionally
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
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                ),
                purpose = transaction.purpose,
                dir = transaction.direction,
                displayAmount = transaction.amount
            )
        }
    }
}


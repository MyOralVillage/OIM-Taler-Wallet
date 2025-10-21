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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.taler.database.data_models.Amount
import net.taler.wallet.oim.history.filter.DirectionFilterRow
import net.taler.wallet.oim.res_mapping_extensions.Buttons
import net.taler.wallet.oim.res_mapping_extensions.Tables
import net.taler.wallet.oim.send.screens.PurposeScreen
import net.taler.database.filter.*                   // TranxFilter, toSQL()
import net.taler.database.data_models.*              // Amount, Tranx, TranxPurp
import net.taler.database.data_access.*              // TransactionDatabase, addTranx, queryTranx

// Constants
private const val SIDEBAR_LEFT_WEIGHT = 0.1f
private const val CONTENT_WEIGHT = 0.6f
private const val SIDEBAR_RIGHT_WEIGHT = 0.3f

// Data Models
data class Transaction(
    val type: String,
    val amount: String,
    val currency: String,
    val date: String,
    val purpose: String
)

// Sample Data
private val sampleTransactions = listOf(
    Transaction(type = "S", amount = "25", currency = "Leones", date = "25 Sept 2025", purpose = ""),
    Transaction(type = "R", amount = "20", currency = "Leones", date = "25 Sept 2025", purpose = ""),
    Transaction(type = "S", amount = "5", currency = "Leones", date = "25 Sept 2025", purpose = ""),
)

@Composable
fun TransactionsListView(
    transactions: List<Transaction> = sampleTransactions
) {
    Box(modifier = Modifier.fillMaxSize()) {
        BackgroundImage()
        MainLayout(transactions = transactions)
    }
}

@Composable
private fun BackgroundImage() {
    Image(
        painter = BitmapPainter(Tables(false).resourceMapper()),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun MainLayout(transactions: List<Transaction>) {
    Row(modifier = Modifier.fillMaxWidth().fillMaxHeight()) {
        MainContent(transactions = transactions)
        RightSidebar()
    }
}

@Composable
private fun RowScope.MainContent(transactions: List<Transaction>) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .weight(0.6f)
            .verticalScroll(rememberScrollState())
    ) {
        transactions.forEach { transaction ->
            TransactionCard(
                type = transaction.type,
                amount = transaction.amount,
                currency = transaction.currency,
                date = transaction.date,
                purpose = transaction.purpose
            )
        }
    }
}

@Composable
private fun RowScope.RightSidebar() {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .weight(0.4f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DirectionFilterRow(
                incomingSelected = true,
                outgoingSelected = false,
                onIncomingClicked = { },
                onOutgoingClicked = { },
                modifier = Modifier.padding(16.dp)
            )
            PurposeScreen(
                balance = Amount.fromString("SLE", "25"),
                onBack = {},
                onDone = {}
            )
        }
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
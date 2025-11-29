package net.taler.wallet.oim.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.serialization.InternalSerializationApi
import net.taler.common.Amount as CommonAmount
import net.taler.database.data_models.*
import java.time.format.DateTimeFormatter

@OptIn(InternalSerializationApi::class)
@Composable
fun TransactionsList(
    transactions: List<Tranx>,
    onTransactionClick: (Tranx) -> Unit,
    onAmountClick: ((Tranx) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(transactions) { transaction ->
            TransactionCard(
                amount = transaction.amount.toString(false),
                currency = transaction.amount.currency,
                date = transaction.datetime.fmtString(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
                ),
                purpose = transaction.purpose,
                dir = transaction.direction,
                displayAmount = transaction.amount,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onTransactionClick(transaction)
                },
                onAmountClick = if (onAmountClick != null) {
                    { onAmountClick(transaction) }
                } else null
            )
        }
    }
}


// ============================================================================
// PREVIEWS
// ============================================================================

@OptIn(InternalSerializationApi::class)
@Preview(
    showBackground = true,
    name = "Transactions List - Multiple Items",
    device = "spec:width=920dp,height=460dp,orientation=landscape"
)
@Composable
fun TransactionsListPreview() {
    MaterialTheme {
        val fakeTranx = listOf(
            Tranx(
                amount = CommonAmount("XOF", 10000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "1"
            ),
            Tranx(
                amount = Amount("EUR", 5000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "2"
            ),
            Tranx(
                amount = Amount("SLE", 15000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "3"
            )
        )

        TransactionsList(
            transactions = fakeTranx,
            onTransactionClick = {}
        )
    }
}
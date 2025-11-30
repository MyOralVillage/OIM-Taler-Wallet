@file:OptIn(InternalSerializationApi::class)

package net.taler.wallet.oim.history.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import net.taler.common.Amount as CommonAmount
import net.taler.database.data_models.Amount
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.Tranx
import net.taler.database.data_models.*
import net.taler.wallet.oim.history.components.*
import net.taler.wallet.oim.notes.NotesGalleryScreen
import net.taler.wallet.oim.top_bar.OimTopBarCentered
import net.taler.wallet.oim.utils.assets.OimColours
import net.taler.wallet.oim.utils.assets.WoodTableBackground
import net.taler.wallet.oim.utils.res_mappers.resourceMapper
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun OimTransactionHistoryScreen(
    transactions: List<Tranx>,
    balanceAmount: Amount,
    showRiver: Boolean,
    onToggleView: () -> Unit,
    onHome: () -> Unit,
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selected by remember { mutableStateOf<Tranx?>(null) }
    var showNotesGallery by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Pre-format dates exactly like TransactionCard
    val formatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
    val dateStrings = remember(transactions) {
        transactions.map { t -> t.datetime.fmtString(formatter) }
    }


    if (showNotesGallery && selected != null) {
        // unchanged
    } else {
        Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
            WoodTableBackground(
                modifier = Modifier.fillMaxSize(),
                light = false
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                OimTopBarCentered(
                    balance = balanceAmount,
                    onChestClick = onHome,
                    colour = OimColours.TRX_HIST_COLOUR
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    if (showRiver) {
                        RiverSceneCanvas(
                            transactions = transactions,
                            dates = dateStrings,              // NEW
                            onTransactionClick = {
                                selected = it
                                scope.launch { sheetState.show() }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 104.dp)
                        )
                    } else {
                        // list view unchanged
                        TransactionsList(
                            transactions = transactions,
                            onTransactionClick = {
                                selected = it
                                scope.launch { sheetState.show() }
                            },
                            onAmountClick = {
                                selected = it
                                showNotesGallery = true
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 104.dp)
                        )
                    }

                    HistorySideButtons(
                        showRiver = showRiver,
                        onToggleView = onToggleView,
                        onSendClick = onSendClick,
                        onReceiveClick = onReceiveClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    if (transactions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No transactions yet", color = Color.White)
                        }
                    }
                }
            }
        }

        selected?.let { t ->
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { selected = null }
                },
                sheetState = sheetState
            ) {
                val dateStr = t.datetime.fmtString(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
                )
                TransactionCard(
                    amount = t.amount.toString(false),
                    currency = t.amount.currency,
                    date = dateStr,
                    purpose = t.purpose,
                    dir = t.direction,
                    displayAmount = t.amount,
                    onAmountClick = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                showNotesGallery = true
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}


// PREVIEWS -------------------------------------------------------------

@OptIn(InternalSerializationApi::class)
@Preview(
    showBackground = true,
    showSystemUi = false,
    name = "Transaction History - River View",
    device = "spec:width=920dp,height=460dp,orientation=landscape"
)
@Composable
fun TransactionHistoryPreview() {
    MaterialTheme {
        val sameDate = FDtm()

        val fakeTranx = listOf(
            Tranx(
                amount = CommonAmount("XOF", 10000L, 0),
                datetime = sameDate,
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "1"
            ),
            Tranx(
                amount = Amount("EUR", 5000L, 0),
                datetime = sameDate,
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
            ),
            Tranx(
                amount = Amount("XOF", 8000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "4"
            ),
            Tranx(
                amount = Amount("SLE", 20000L, 0),
                datetime = FDtm(),
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "5"
            )
        )

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dates = fakeTranx.map { it.datetime.fmtString(formatter) }

        OimTransactionHistoryScreen(
            transactions = fakeTranx,
            balanceAmount = Amount("SLE", 100L, 50_000_000),
            showRiver = true,
            onToggleView = {},
            onHome = {},
            onSendClick = {},
            onReceiveClick = {}
        )
    }
}

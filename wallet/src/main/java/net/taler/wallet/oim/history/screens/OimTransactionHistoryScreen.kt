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

/**
 * Main transaction history screen with river/list toggle.
 *
 * Displays transactions in either river view or list view, with side buttons
 * for send, receive, and view toggle.
 */
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

    // Show notes gallery if triggered
    if (showNotesGallery && selected != null) {
        val noteResIds = remember(selected) {
            try {
                selected!!.amount.resourceMapper()
            } catch (e: IllegalArgumentException) {
                emptyList()
            }
        }

        NotesGalleryScreen(
            onBackClick = {
                showNotesGallery = false
                selected = null
            },
            drawableResIds = noteResIds,
            noteHeight = 115.dp,
            title = "${selected!!.amount.toString(false)} ${selected!!.amount.currency}"
        )
    } else {
        Box(modifier = modifier.fillMaxSize().statusBarsPadding()) {
            // Wood background
            WoodTableBackground(
                modifier = Modifier.fillMaxSize(),
                light = false
            )

            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top bar
                OimTopBarCentered(
                    balance = balanceAmount,
                    onChestClick = onHome,
                    colour = OimColours.TRX_HIST_COLOUR
                )

                // Content area with side buttons
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Main content (river or list)
                    if (showRiver) {
                        RiverSceneCanvas(
                            transactions = transactions,
                            onTransactionClick = {
                                selected = it
                                scope.launch { sheetState.show() }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 104.dp)
                        )
                    } else {
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

                    // Side buttons
                    HistorySideButtons(
                        showRiver = showRiver,
                        onToggleView = onToggleView,
                        onSendClick = onSendClick,
                        onReceiveClick = onReceiveClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )

                    // Empty state
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

        // Transaction details modal
        selected?.let { t ->
            ModalBottomSheet(
                onDismissRequest = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion { selected = null }
                },
                sheetState = sheetState
            ) {
                val dateStr = t.datetime.fmtString(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")
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

// ============================================================================
// PREVIEWS
// ============================================================================

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
        // Create a specific date for testing
        val sameDate = FDtm() // Same date for multiple transactions

        val fakeTranx = listOf(
            Tranx(
                amount = CommonAmount("XOF", 10000L, 0),
                datetime = sameDate, // Same date
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "1"
            ),
            Tranx(
                amount = Amount("EUR", 5000L, 0),
                datetime = sameDate, // Same date as previous
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "2"
            ),
            Tranx(
                amount = Amount("SLE", 15000L, 0),
                datetime = FDtm(), // Different date
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "3"
            ),
            Tranx(
                amount = Amount("XOF", 8000L, 0),
                datetime = FDtm(), // Different date
                direction = FilterableDirection.OUTGOING,
                purpose = EXPN_FARM,
                TID = "4"
            ),
            Tranx(
                amount = Amount("SLE", 20000L, 0),
                datetime = FDtm(), // Different date
                direction = FilterableDirection.INCOMING,
                purpose = EDUC_CLTH,
                TID = "5"
            )
        )

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

@Preview(
    showBackground = true,
    name = "Small Landscape Phone 640x360dp (xhdpi)",
    device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape"
)
@Composable
fun TransactionHistoryPreview_SmallPhoneXhdpi() {
    TransactionHistoryPreview()
}
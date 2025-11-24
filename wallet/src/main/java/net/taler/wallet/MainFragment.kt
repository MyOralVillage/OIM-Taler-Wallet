/*
 * This file is part of GNU Taler
 * (C) 2024 Taler Systems S.A.
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

package net.taler.wallet
import android.view.Menu
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.os.bundleOf
import android.view.LayoutInflater
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import net.taler.wallet.R.drawable.*
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.compose.AndroidFragment
import androidx.fragment.compose.FragmentState
import androidx.fragment.compose.rememberFragmentState
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.taler.wallet.balances.BalanceState
import net.taler.wallet.balances.BalancesComposable
import net.taler.wallet.balances.ScopeInfo
import net.taler.wallet.compose.DemandAttention
import net.taler.wallet.compose.GridMenu
import net.taler.wallet.compose.GridMenuItem
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.compose.collectAsStateLifecycleAware
import net.taler.wallet.oim.main.OIMChestScreen
import net.taler.wallet.oim.main.OIMHomeScreen
import net.taler.wallet.oim.main.OIMPaymentDialog
import net.taler.wallet.oim.main.rememberOimReceiveFlowState
import net.taler.wallet.oim.res_mapping_extensions.UIIcons
import net.taler.wallet.settings.SettingsFragment
import net.taler.wallet.transactions.Transaction
import net.taler.wallet.transactions.TransactionMajorState
import net.taler.wallet.transactions.TransactionPayment
import net.taler.wallet.transactions.TransactionState
import net.taler.wallet.transactions.TransactionStateFilter.Nonfinal
import kotlin.math.roundToInt
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.view.MenuProvider

/**
 * Main fragment of the Taler wallet application.
 * Manages the primary navigation between balances, settings, and OIM screens.
 *
 * This fragment serves as the central hub for:
 * - Viewing wallet balances and transactions
 * - Accessing settings
 * - Switching to OIM mode for portrait-locked payment interactions
 * - Handling QR code scanning and payment actions
 */
class MainFragment: Fragment() {
    private var switchToSettings: (() -> Unit)? = null


    /** Represents the main navigation tabs in the standard wallet view */
    enum class Tab { BALANCES, SETTINGS }

    /** Represents the different screens available in OIM (Online Interactive Mode) */
    private enum class OimScreen { HOME, CHEST, HISTORY, SEND }

    private val model: MainViewModel by activityViewModels()

    /**
     * Creates the main view of the fragment using Jetpack Compose.
     * Sets up the navigation structure and handles switching between standard and OIM modes.
     */
    @SuppressLint("SourceLockedOrientationActivity")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val composeView = ComposeView(requireContext()).apply { id = R.id.main_compose_view }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        switchToSettings?.invoke()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner)
        composeView.setContent {
            TalerSurface {
                var tab by rememberSaveable { mutableStateOf(Tab.BALANCES) }
                var oimScreen by rememberSaveable { mutableStateOf<OimScreen?>(null) }
                var showSheet by remember { mutableStateOf(false) }
                val sheetState = rememberModalBottomSheetState()
                switchToSettings = { tab = Tab.SETTINGS }
                val settingsFragmentState = rememberFragmentState()

                val context = LocalContext.current
                val online by model.networkManager.networkStatus.observeAsState(false)
                val balanceState by
                model
                    .balanceManager
                    .state
                    .observeAsState(BalanceState.None)
                val selectedScope by
                model
                    .transactionManager
                    .selectedScope
                    .collectAsStateLifecycleAware()
                val txStateFilter by
                model
                    .transactionManager
                    .stateFilter
                    .collectAsStateLifecycleAware()
                val txResult by remember(selectedScope, txStateFilter) {
                    model
                        .transactionManager
                        .transactionsFlow(selectedScope, stateFilter = txStateFilter)
                }.collectAsStateLifecycleAware()
                val selectedSpec =
                    remember(selectedScope) {
                        selectedScope?.let {
                            model.exchangeManager.getSpecForScopeInfo(it)
                        }
                    }
                val actionButtonUsed by
                model
                    .settingsManager
                    .getActionButtonUsed(context)
                    .collectAsStateLifecycleAware(initial = true)

                if (oimScreen != null) {
                    // Handle OIM screens with portrait lock
                    val activity = (requireActivity() as AppCompatActivity)
                    LaunchedEffect(Unit) {
                        activity.requestedOrientation =
                            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        activity.supportActionBar?.hide()
                    }
                    BackHandler(true) {
                        oimScreen = null
                        activity.requestedOrientation =
                            android
                                .content
                                .pm
                                .ActivityInfo
                                .SCREEN_ORIENTATION_UNSPECIFIED
                        activity.supportActionBar?.show()
                    }
                    val reviewTos: (String) -> Unit = { exchangeBaseUrl ->
                        val bundle = bundleOf("exchangeBaseUrl" to exchangeBaseUrl)
                        findNavController().navigate(
                            R.id.action_global_reviewExchangeTos,
                            bundle
                        )
                    }

                    when (oimScreen) {
                        OimScreen.HOME -> OIMHomeScreen(
                            model = model,
                            onNavigateToChest = { oimScreen = OimScreen.CHEST },
                            onBackToTaler = {
                                oimScreen = null
                                val activity = (requireActivity() as AppCompatActivity)
                                activity.requestedOrientation =
                                    android
                                        .content
                                        .pm
                                        .ActivityInfo
                                        .SCREEN_ORIENTATION_UNSPECIFIED
                                activity.supportActionBar?.show()
                            },
                            onReviewTos = reviewTos,
                        )
                        OimScreen.CHEST -> run {
                            val receiveFlow = rememberOimReceiveFlowState(
                                model = model,
                                onReviewTos = reviewTos,
                            )

                            Box(modifier = Modifier.fillMaxSize()) {
                                OIMChestScreen(
                                    model = model,
                                    onBackClick = { oimScreen = OimScreen.HOME },
                                    onSendClick = { oimScreen = OimScreen.SEND },
                                    onRequestClick = receiveFlow.launchReceiveScan,
                                    onTransactionHistoryClick = { oimScreen = OimScreen.HISTORY },
                                    onWithdrawTestKudosClick = {
                                        model.withdrawManager.withdrawTestBalance()
                                    },
                                )

                                val terms = receiveFlow.dialogTerms
                                if (terms != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        OIMPaymentDialog(
                                            terms = terms,
                                            onAccept = { receiveFlow.confirmTerms(terms) },
                                            onReject = { receiveFlow.rejectTerms(terms) }
                                        )
                                    }
                                }
                            }
                        }
                        OimScreen.SEND -> {
                            BackHandler(true) { oimScreen = OimScreen.CHEST }

                            net.taler.wallet.oim.send.app.OimSendApp(
                                model = model,
                                onHome = {
                                    oimScreen = OimScreen.HOME
                                }
                            )
                        }

                        OimScreen.HISTORY -> {
                            BackHandler(true) { oimScreen = OimScreen.CHEST }

                            val receiveFlow = rememberOimReceiveFlowState(
                                model = model,
                                onReviewTos = reviewTos,
                            )

                            val bs = balanceState

                            val selectedBalance = when (bs) {
                                is BalanceState.Success -> {
                                    bs.balances.firstOrNull { it.currency == "KUDOS" }
                                        ?: bs.balances.firstOrNull { it.currency == "TESTKUDOS" }
                                        ?: bs.balances.firstOrNull()
                                }
                                else -> null
                            }

                            val amountForNotes = remember(selectedBalance) {
                                val a = selectedBalance?.available
                                if (a != null) {
                                    when (selectedBalance.currency) {
                                        "KUDOS", "TESTKUDOS", "KUD"
                                            -> a.withCurrency("SLE")
                                        else -> a
                                    }
                                } else null
                            }

                            Box(modifier = Modifier.fillMaxSize()) {
                                net.taler.wallet.oim.history.components.TransactionHistoryView(
                                    onHome = { oimScreen = OimScreen.CHEST },
                                    balanceAmount = amountForNotes,
                                    onSendClick = { oimScreen = OimScreen.SEND },
                                    onReceiveClick = receiveFlow.launchReceiveScan,
                                )

                                val terms = receiveFlow.dialogTerms
                                if (terms != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        OIMPaymentDialog(
                                            terms = terms,
                                            onAccept = { receiveFlow.confirmTerms(terms) },
                                            onReject = { receiveFlow.rejectTerms(terms) }
                                        )
                                    }
                                }
                            }
                        }
                        null -> { }
                    }
                } else {
                    // Standard wallet view - NO TopAppBar, using existing supportActionBar
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            bottomBar = {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    // button sizes/scales
                                    val density = LocalDensity.current
                                    val iconSize = with(density) { (40.sp).toDp() }
                                    val buttonSize = with(density) { (48.sp).toDp() }

//                                    // Left ->  Balance view
                                    IconButton(
                                        modifier = Modifier
                                            .size(buttonSize)
                                            .background(
                                                MaterialTheme
                                                .colorScheme
                                                .surfaceVariant.copy(alpha = 0.9f),
                                                CircleShape
                                            ),
                                        onClick = {
                                            tab = Tab.BALANCES
                                            if (selectedScope != null)
                                                model.transactionManager.selectScope(null)
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(accounting_book),
                                            contentDescription = "Switch to balance view",
                                            modifier = Modifier.size(iconSize),
                                            tint = if (tab == Tab.BALANCES) {
                                                MaterialTheme.colorScheme.primary  // Selected color
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant  // Unselected color
                                            }
                                        )
                                    }
//                                    NavigationBarItem(
//                                        icon = {
//                                            Icon(Icons.Default.BarChart, contentDescription = null)
//                                        },
//                                        selected = tab == Tab.BALANCES,
//                                        onClick = {
//                                            tab = Tab.BALANCES
//                                            if (selectedScope != null)
//                                                model.transactionManager.selectScope(null)
//                                        }
//                                    )

                                    // Center -> "Action buttons"
                                    TalerActionButton(
                                        demandAttention = !actionButtonUsed,
                                        onShowSheet = {
                                            showSheet = true
                                            model.settingsManager.saveActionButtonUsed(context)
                                        },
                                        onScanQr = {
                                            this@MainFragment.onScanQr()
                                            model.settingsManager.saveActionButtonUsed(context)
                                        },
                                    )


                                    // Right -> OIM Mode Button
                                    var enterOimAfterLandscape by remember { mutableStateOf(false)}
                                    IconButton(
                                        modifier = Modifier
                                            .size(buttonSize)
                                            .background(
                                                MaterialTheme
                                                .colorScheme
                                                .surfaceVariant.copy(alpha = 0.9f),
                                                CircleShape
                                            ),
                                        onClick = {
                                            val activity = requireActivity() as AppCompatActivity
                                            if (
                                                activity
                                                .resources
                                                .configuration
                                                .orientation
                                                !=
                                                android
                                                .content
                                                .res
                                                .Configuration
                                                .ORIENTATION_LANDSCAPE
                                                )
                                            {
                                                enterOimAfterLandscape = true
                                                activity.requestedOrientation =
                                                    android
                                                    .content
                                                    .pm
                                                    .ActivityInfo
                                                    .SCREEN_ORIENTATION_LANDSCAPE
                                            } else {
                                                oimScreen = OimScreen.HOME
                                            }
                                        }
                                    ) {
                                        Icon(
                                            bitmap = UIIcons("filter").resourceMapper(),
                                            contentDescription = "Switch to OIM Mode",
                                            modifier = Modifier.size(iconSize),
                                            tint = Color.Unspecified
                                        )
                                    }

                                }
                            },
                            contentWindowInsets = WindowInsets.systemBars.only(
                                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                            )
                        ) { innerPadding ->
                            LaunchedEffect(Unit) {
                                if (selectedScope == null) {
                                    model.transactionManager.selectScope(
                                        model.settingsManager.getSelectedScope(context).first()
                                    )
                                }
                            }

                            LaunchedEffect(tab, selectedScope) {
                                this@MainFragment.setTitle(tab, selectedScope)
                            }

                            BackHandler(selectedScope != null) {
                                model.transactionManager.selectScope(null)
                            }

                            when (tab) {
                                Tab.BALANCES -> BalancesComposable(
                                    innerPadding = innerPadding,
                                    state = balanceState,
                                    txResult = txResult,
                                    txStateFilter = txStateFilter,
                                    selectedScope = selectedScope,
                                    selectedCurrencySpec = selectedSpec,
                                    onGetDemoMoneyClicked = {
                                        model.withdrawManager.withdrawTestBalance()
                                        Snackbar.make(
                                            requireView(),
                                            getString(R.string.settings_test_withdrawal),
                                            LENGTH_LONG
                                        ).show()
                                    },
                                    onBalanceClicked = {
                                        model.showTransactions(it.scopeInfo)
                                    },
                                    onPendingClicked = {
                                        model.showTransactions(it.scopeInfo, Nonfinal)
                                    },
                                    onTransactionClicked = { tx ->
                                        this@MainFragment.onTransactionClicked(tx)
                                    },
                                    onTransactionsDelete = { txIds ->
                                        model.transactionManager.deleteTransactions(txIds){ error ->
                                            Toast.makeText(
                                                context,
                                                error.userFacingMsg,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    },
                                    onShowBalancesClicked = {
                                        if (model.transactionManager.selectedScope.value != null) {
                                            model.transactionManager.selectScope(null)
                                        }
                                    },
                                )
                                Tab.SETTINGS -> SettingsView(
                                    innerPadding = innerPadding,
                                    settingsFragmentState = settingsFragmentState,
                                )
                            }
                        }

                    }
                }

                if (oimScreen == null) {
                    val disableActions = remember(balanceState, online) {
                        !online ||
                        (balanceState as? BalanceState.Success)?.balances?.isEmpty() ?: true
                    }
                    TalerActionsModal(
                        showSheet = showSheet,
                        sheetState = sheetState,
                        onDismiss = { showSheet = false },
                        disableActions = disableActions,
                        onSend = this@MainFragment::onSend,
                        onReceive = this@MainFragment::onReceive,
                        onScanQr = this@MainFragment::onScanQr,
                        onDeposit = this@MainFragment::onDeposit,
                        onWithdraw = this@MainFragment::onWithdraw,
                        onEnterUri = this@MainFragment::onEnterUri,
                    )
                }
            }
        }
        return composeView
    }

    /**
     * Sets the title of the activity's action bar based on current tab and scope
     *
     * @param tab The currently selected tab
     * @param scope The currently selected transaction scope, if any
     */
    private fun setTitle(tab: Tab, scope: ScopeInfo?) {
        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.title = when (tab) {
                Tab.BALANCES -> if (scope != null) {
                    getString(R.string.transactions_title)
                } else {
                    getString(R.string.balances_title)
                }
                Tab.SETTINGS -> getString(R.string.menu_settings)
            }
        }
    }
    /**
     * Handles click events on transactions.
     * Routes to appropriate detail screens based on transaction state and type.
     *
     * @param tx The transaction that was clicked
     */
    private fun onTransactionClicked(tx: Transaction) {
        val showTxDetails = {
            if (tx.detailPageNav != 0) {
                model.transactionManager.selectTransaction(tx)
                findNavController().navigate(tx.detailPageNav)
            }
        }
        when (tx.txState) {
            // unfinished transactions (dialog)
            TransactionState(TransactionMajorState.Dialog) -> when (tx) {
                is TransactionPayment -> {
                    model.paymentManager.preparePay(tx.transactionId) {
                        findNavController().navigate(R.id.action_global_promptPayment)
                    }
                }
                else -> showTxDetails()
            }
            else -> showTxDetails()
        }
    }

    /**
     * Called when the fragment becomes visible.
     * Loads wallet balances and pre-selects scope if only one balance exists.
     */
    override fun onStart() {
        super.onStart()
        model.balanceManager.loadBalances()
        model.balanceManager.state.observe(viewLifecycleOwner) { res ->
            if (res is BalanceState.Success) {
                if (res.balances.size == 1) {
                    // pre-select on startup if it's the only one
                    model.transactionManager.selectScope(res.balances.first().scopeInfo)
                }
            }
        }
    }

    /** Navigates to the send funds screen */
    private fun onSend() { findNavController().navigate(R.id.nav_peer_push) }

    /** Navigates to the receive funds screen */
    private fun onReceive() { findNavController().navigate(R.id.nav_peer_pull) }

    /** Navigates to the deposit screen */
    private fun onDeposit() { findNavController().navigate(R.id.nav_deposit) }

    /** Initiates the withdrawal process */
    private fun onWithdraw() {
        model.withdrawManager.resetWithdrawal()
        findNavController().navigate(R.id.promptWithdraw)
    }

    /** Launches QR code scanner */
    private fun onScanQr() {model.scanCode()}

    /** Navigates to manual URI input screen  */
    private fun onEnterUri() {findNavController().navigate(R.id.nav_uri_input)}
}

/**
 * Composable for displaying the settings fragment within the compose UI
 *
 * @param innerPadding Padding values from the scaffold
 * @param settingsFragmentState State of the settings fragment
 */
@Composable
fun SettingsView(
    innerPadding: PaddingValues,
    settingsFragmentState: FragmentState,
) {
    AndroidFragment(
        SettingsFragment::class.java,
        modifier = Modifier.padding(innerPadding),
        fragmentState = settingsFragmentState,
    )
}

/**
 * Central action button in the navigation bar.
 * Provides quick access to common wallet actions with drag-to-scan gesture.
 *
 * @param demandAttention Whether to show attention-grabbing animation
 * @param onShowSheet Callback to show the actions bottom sheet
 * @param onScanQr Callback to launch QR scanner
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TalerActionButton(
    demandAttention: Boolean,
    onShowSheet: () -> Unit,
    onScanQr: () -> Unit,
) {
    val tooltipState = rememberTooltipState()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(stringResource(R.string.actions)) } },
        state = tooltipState,
    ) {
        val offsetY = remember { Animatable(0f) }
        var cancelled by remember { mutableStateOf(false) }

        DemandAttention(demandAttention = demandAttention) {
            LargeFloatingActionButton(
                modifier = Modifier
                    .requiredSize(86.dp)
                    .padding(8.dp)
                    .offset { IntOffset(0, offsetY.value.roundToInt() / 6) }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            runBlocking { offsetY.snapTo(offsetY.value + delta) }
                            if (delta > 0) {
                                cancelled = true
                            }
                        },
                        onDragStopped = {
                            offsetY.animateTo(0.0f)
                            if (!cancelled) {
                                onScanQr()
                            }
                            cancelled = false
                        },
                    ),
                shape = CircleShape,
                onClick = { onShowSheet() },
            ) {
                if (offsetY.value == 0.0f) {
                    Icon(
                        painterResource(R.drawable.ic_actions),
                        modifier = Modifier.size(38.dp),
                        contentDescription = stringResource(R.string.actions),
                    )
                } else {
                    Icon(
                        painterResource(R.drawable.ic_scan_qr),
                        contentDescription = stringResource(R.string.actions),
                    )
                }
            }
        }
    }

    LaunchedEffect(demandAttention) {
        if (demandAttention) {
            tooltipState.show()
        }
    }
}

/**
 * Modal bottom sheet displaying wallet action options.
 * Provides grid menu of common wallet operations.
 *
 * @param showSheet Whether to show the bottom sheet
 * @param sheetState State of the bottom sheet
 * @param disableActions Whether to disable action buttons
 * @param onDismiss Callback when sheet is dismissed
 * @param onSend Callback for send action
 * @param onReceive Callback for receive action
 * @param onScanQr Callback for QR scan action
 * @param onDeposit Callback for deposit action
 * @param onWithdraw Callback for withdraw action
 * @param onEnterUri Callback for manual URI entry
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalerActionsModal(
    showSheet: Boolean,
    sheetState: SheetState,
    disableActions: Boolean,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    onReceive: () -> Unit,
    onScanQr: () -> Unit,
    onDeposit: () -> Unit,
    onWithdraw: () -> Unit,
    onEnterUri: () -> Unit,
) {
    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            GridMenu(
                contentPadding = PaddingValues(
                    start = 8.dp,
                    end = 8.dp,
                    bottom = 16.dp + WindowInsets
                        .systemBars
                        .asPaddingValues()
                        .calculateBottomPadding(),
                ),
            ) {
                GridMenuItem(
                    icon = R.drawable.ic_link,
                    title = R.string.enter_uri_label,
                    onClick = { onEnterUri(); onDismiss() },
                )

                GridMenuItem(
                    icon = R.drawable.transaction_deposit,
                    title = R.string.send_deposit_button_label,
                    onClick = { onDeposit(); onDismiss() },
                    enabled = !disableActions
                )

                GridMenuItem(
                    icon = R.drawable.ic_scan_qr,
                    title = R.string.button_scan_qr_code_label,
                    onClick = { onScanQr(); onDismiss() },
                )

                GridMenuItem(
                    icon = R.drawable.transaction_p2p_incoming,
                    title = R.string.transactions_receive_funds,
                    onClick = { onReceive(); onDismiss() },
                    enabled = !disableActions,
                )

                GridMenuItem(
                    icon = R.drawable.transaction_withdrawal,
                    title = R.string.withdraw_button_label,
                    onClick = { onWithdraw(); onDismiss() },
                    enabled = !disableActions,
                )

                GridMenuItem(
                    icon = R.drawable.transaction_p2p_outgoing,
                    title = R.string.transactions_send_funds,
                    onClick = { onSend(); onDismiss() },
                    enabled = !disableActions,
                )
            }
        }
    }
}
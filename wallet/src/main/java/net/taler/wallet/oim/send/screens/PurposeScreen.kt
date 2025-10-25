<<<<<<< HEAD
/*
<<<<<<< HEAD
<<<<<<< HEAD
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

/*
=======
>>>>>>> 5c7011a (fixed preview animations)
=======
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
 * GPLv3-or-later
=======
/**
 * ## PurposeScreen
 *
 * Allows the user to choose or confirm the *purpose* of a transaction
 * (e.g., groceries, donation, utilities) before initiating a payment
 * in the OIM Send flow.
 *
 * Displays a responsive grid of [TranxPurp] icons categorized by type.
 * The user can select one, return to the previous screen, or navigate home.
 * Internally uses [WoodTableBackground] for visual consistency with the rest
 * of the send interface.
 *
 * @param balance Current wallet balance shown at the top.
 * @param onBack Called when the user presses the "Back" button.
 * @param onDone Called when a purpose is selected, returning the chosen [TranxPurp].
 * @param onHome Optional handler for returning to the app’s home screen.
 *
 * @see net.taler.database.data_models.TranxPurp
 * @see net.taler.wallet.oim.send.components.WoodTableBackground
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
 */

package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
<<<<<<< HEAD
<<<<<<< HEAD
import net.taler.wallet.oim.send.components.*
<<<<<<< HEAD
// TODO refactor to use res_mapping_extensions

///**
// * Screen for selecting or entering the purpose of a payment.
// *
// * Users can pick from predefined purpose icons or enter a custom purpose.
// *
// * @param balance The current balance of the user, displayed in the top bar.
// * @param onBack Callback invoked when the back button is pressed.
// * @param onDone Callback invoked when the user selects a purpose or enters a custom one.
// *               The selected or entered purpose string is passed as a parameter.
// */
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun PurposeScreen(
//    balance: Int,
//    onBack: () -> Unit,
//    onDone: (String) -> Unit
//) {
//    Box(Modifier.fillMaxSize()) {
//        Image(
//            painter = assetPainterOrPreview(WOOD_TABLE),
//            contentDescription = null,
//            modifier = Modifier.fillMaxSize(),
//            contentScale = ContentScale.Crop
//        )
//
//        IconButton(
//            onClick = onBack,
//            modifier = Modifier
//                .padding(8.dp)
//                .size(40.dp)
//                .align(Alignment.TopStart)
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                contentDescription = "Back",
//                tint = androidx.compose.ui.graphics.Color.White
//            )
//        }
//
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 16.dp, vertical = 8.dp)
//        ) {
//            OimTopBarCentered(balance = balance, onSendClick = { })
//
//            Spacer(Modifier.height(8.dp))
//
//            FlowRow(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f),
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                PurposeIcons.forEach { (path, label) ->
//                    PurposeTile(
//                        path = path,
//                        label = label,
//                        modifier = Modifier
//                            .width(140.dp)
//                            .height(140.dp),
//                        onPick = { onDone(label) }
//                    )
//                }
//            }
//
//            var custom by remember { mutableStateOf(TextFieldValue()) }
//            OutlinedTextField(
//                value = custom,
//                onValueChange = { custom = it },
//                label = { Text("Custom purpose") },
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth()
//            )
//            Spacer(Modifier.height(10.dp))
//            Button(
//                onClick = { onDone(custom.text.ifBlank { "Payment" }) },
//                modifier = Modifier.align(Alignment.End)
//            ) { Text("Continue") }
//
//            Spacer(Modifier.height(8.dp))
//        }
//    }
//}
=======

=======
=======
import androidx.compose.ui.unit.sp
<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 321d128 (updated send to be more dynamic)
=======
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
<<<<<<< HEAD
>>>>>>> 9068d57 (got rid of bugs in send apk)
=======
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
<<<<<<< HEAD
<<<<<<< HEAD
import androidx.compose.ui.res.painterResource
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
=======
import androidx.compose.ui.res.painterResource
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)
import net.taler.database.data_models.*
=======
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
import net.taler.database.data_models.tranxPurpLookup
>>>>>>> f82ba56 (UI changes and fix qr code loading for send)
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.oim.send.components.WoodTableBackground

<<<<<<< HEAD
<<<<<<< HEAD
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
@OptIn(ExperimentalLayoutApi::class)
=======
/**
 * Individual purpose card displaying a bitmap image.
 */
=======
>>>>>>> 9068d57 (got rid of bugs in send apk)
@Composable
private fun PurposeCard(
    tranxPurp: TranxPurp,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colour = Color(tranxPurp.colourInt())
    Card(
        modifier = modifier
            .width(150.dp)
            .aspectRatio(0.66f)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 0.dp,
                color = if (isSelected) colour else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colour.copy(alpha = 0.18f)
            else Color(0x55FFFFFF)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier.fillMaxSize().padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(tranxPurp.resourceMapper()),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

<<<<<<< HEAD
/**
 * Purpose selection screen with grid layout.
 * Only one can be selected at a time.
 */
>>>>>>> 321d128 (updated send to be more dynamic)
=======
@OptIn(ExperimentalLayoutApi::class)
>>>>>>> 9068d57 (got rid of bugs in send apk)
@Composable
fun PurposeScreen(
    balance: Amount,
    onBack: () -> Unit,
    onDone: (TranxPurp) -> Unit,
    onHome: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize()) {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
        Image(
            painter = assetPainterOrPreview(WOOD_TABLE, PreviewAssets.id(WOOD_TABLE)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
=======
        WoodTableBackground(Modifier.fillMaxSize())

        // tiny back button
        FilledTonalButton(
            onClick = onBack,
            modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
        ) { Text("Back") }
=======
//        WoodTableBackground(Modifier.fillMaxSize(), light = false)

//        IconButton(
//            onClick = onBack,
//            modifier = Modifier
//                .padding(8.dp)
//                .size(40.dp)
//                .align(Alignment.TopStart)
//        ) {
//            Icon(
//                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
//                contentDescription = "Back",
//                tint = Color.White
//            )
//        }
>>>>>>> 8d7e8e2 (Added purpose filters and direction filters to the same screen (non responsive just for POC))
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
=======
        WoodTableBackground(Modifier.fillMaxSize(), light = false)
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
//        WoodTableBackground(Modifier.fillMaxSize(), light = false)
>>>>>>> e34a007 (Added purpose filters and direction filters to the same screen (non responsive just for POC))
=======
        WoodTableBackground(Modifier.fillMaxSize(), light = false)
>>>>>>> 9068d57 (got rid of bugs in send apk)
=======
        WoodTableBackground(Modifier.fillMaxSize())
>>>>>>> 89f0c7f (refactored svgs to webp, reduced og taler/res by ~80%; total APK size down by ~50%. Needs more fixes/integration)

        // tiny back button
        FilledTonalButton(
            onClick = onBack,
            modifier = Modifier.padding(12.dp).align(Alignment.TopStart)
        ) { Text("Back") }
<<<<<<< HEAD
=======
>>>>>>> c4c1157 (got rid of bugs in send apk)
=======
        WoodTableBackground(modifier = Modifier.fillMaxSize(), light = false)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onHome) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color.White)
            }
            FilledTonalButton(onClick = onBack) { Text("Back") }
        }
>>>>>>> f82ba56 (UI changes and fix qr code loading for send)
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 56.dp)
        ) {
            Text(
                text = "Choose purpose",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))

<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
=======
            val allPurposes = remember {
                listOf(
                    EDUC_CLTH, EDUC_SCHL, EDUC_SUPL,
                    EXPN_CELL, EXPN_DEBT, EXPN_FARM, EXPN_GRCR, EXP_MRKT,
                    EXPN_PTRL, EXPN_RENT, EXPN_TOOL, EXPN_TRPT,
                    HLTH_DOCT, HLTH_MEDS, TRNS_RECV, TRNS_SEND,
                    UTIL_ELEC, UTIL_WATR
                )
            }

>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
            FlowRow(
=======
            val sortedPurposes = remember {
=======
            val allPurposes = remember {
>>>>>>> 9068d57 (got rid of bugs in send apk)
                tranxPurpLookup.values
                    .groupBy { it.tranxGroup }
                    .toSortedMap()
                    .flatMap { (_, ps) -> ps.sortedBy { it.cmp } }
            }

            var selected by remember { mutableStateOf<TranxPurp?>(null) }

<<<<<<< HEAD
            // Scrollable grid that always fits the screen
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
>>>>>>> 321d128 (updated send to be more dynamic)
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
<<<<<<< HEAD
<<<<<<< HEAD
<<<<<<< HEAD
                PurposeIcons.forEach { (path, label) ->
                    PurposeTile(
                        path = path,
=======
                allPurposes.forEach { purp ->
                    val bmp = purp.resourceMapper()
                    val label = purpUiLabel(purp)
                    PurposeTile(
                        bitmap = bmp,
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
                        label = label,
                        modifier = Modifier
                            .width(140.dp)
                            .height(140.dp),
                        onPick = { onDone(label) }
=======
                items(sortedPurposes) { tranxPurp ->
=======
>>>>>>> 938e3e6 (UI changes and fix qr code loading for send)
=======
            // One page, no scroll — FlowRow wraps items to fit space
=======
>>>>>>> f82ba56 (UI changes and fix qr code loading for send)
            FlowRow(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = Int.MAX_VALUE
            ) {
                allPurposes.forEach { p ->
>>>>>>> 9068d57 (got rid of bugs in send apk)
                    PurposeCard(
                        tranxPurp = p,
                        isSelected = (p == selected),
                        onClick = {
<<<<<<< HEAD
                            selectedPurp = tranxPurp
                            onDone(tranxPurp)
                        },
                        modifier = Modifier.fillMaxWidth()
>>>>>>> 321d128 (updated send to be more dynamic)
=======
                            selected = p
                            onDone(p)
                        }
>>>>>>> 9068d57 (got rid of bugs in send apk)
                    )
                }
            }
        }
    }
}
<<<<<<< HEAD
>>>>>>> 5c7011a (fixed preview animations)
=======

<<<<<<< HEAD
<<<<<<< HEAD
private fun purpUiLabel(p: TranxPurp): String = when (p) {
    EDUC_CLTH -> "School Uniforms"
    EDUC_SCHL -> "School"
    EDUC_SUPL -> "School Supplies"
    EXPN_CELL -> "Phone"
    EXPN_DEBT -> "Remittance"
    EXPN_FARM -> "Farming"
    EXPN_GRCR -> "Groceries"
    EXP_MRKT  -> "Market"
    EXPN_PTRL -> "Fuel"
    EXPN_RENT -> "Housing"
    EXPN_TOOL -> "Tools"
    EXPN_TRPT -> "Transport"
    HLTH_DOCT -> "Doctor"
    HLTH_MEDS -> "Medicine"
    TRNS_RECV -> "Receive"
    TRNS_SEND -> "Send"
    UTIL_ELEC -> "Electricity"
    UTIL_WATR -> "Water"
}
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
@Preview(name = "Purpose Screen", showBackground = true, widthDp = 1280, heightDp = 800)
=======
@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
>>>>>>> 9068d57 (got rid of bugs in send apk)
@Composable
private fun PurposeScreenPreview() {
    MaterialTheme {
        PurposeScreen(
            balance = Amount.fromString("KUDOS", "35"),
            onBack = {},
            onDone = {},
            onHome = {}
        )
    }
}
<<<<<<< HEAD
>>>>>>> 321d128 (updated send to be more dynamic)
=======
>>>>>>> 9068d57 (got rid of bugs in send apk)

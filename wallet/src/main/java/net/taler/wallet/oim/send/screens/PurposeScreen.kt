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
 */
package net.taler.wallet.oim.send.screens

/*
 * GPLv3-or-later
 */
/*
 * GPLv3-or-later
 */
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
>>>>>>> 321d128 (updated send to be more dynamic)
import net.taler.database.data_models.*
import net.taler.wallet.oim.res_mapping_extensions.Tables
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.oim.send.components.OimTopBarCentered
import net.taler.wallet.oim.send.components.WoodTableBackground

<<<<<<< HEAD
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
@OptIn(ExperimentalLayoutApi::class)
=======
/**
 * Individual purpose card displaying a bitmap image.
 */
@Composable
internal fun PurposeCard(
    tranxPurp: TranxPurp,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colour = Color(tranxPurp.colourInt())
    Card(
        modifier = modifier
            .aspectRatio(0.6f)
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 4.dp else 0.dp,
                color = if (isSelected) colour else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colour.copy(alpha = 0.25f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = tranxPurp.resourceMapper(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Purpose selection screen with grid layout.
 * Only one can be selected at a time.
 */
>>>>>>> 321d128 (updated send to be more dynamic)
@Composable
fun PurposeScreen(
    balance: Amount,
    onBack: () -> Unit,
    onDone: (TranxPurp) -> Unit,
    columns: Int = 4
) {
    Box(Modifier.fillMaxSize()) {
<<<<<<< HEAD
<<<<<<< HEAD
        Image(
            painter = assetPainterOrPreview(WOOD_TABLE, PreviewAssets.id(WOOD_TABLE)),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
=======
        WoodTableBackground(Modifier.fillMaxSize(), light = false)
>>>>>>> 3e69811 (refactored to use res_mapping and fixed oimsendapp and asset errors)
=======
//        WoodTableBackground(Modifier.fillMaxSize(), light = false)
>>>>>>> e34a007 (Added purpose filters and direction filters to the same screen (non responsive just for POC))

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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
        ) {
//            OimTopBarCentered(balance = balance, onSendClick = { })

            Spacer(Modifier.height(8.dp))

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
                tranxPurpLookup.values
                    .groupBy { it.tranxGroup }
                    .toSortedMap()
                    .flatMap { (_, purposes) ->
                        purposes.sortedBy { it.cmp }
                    }
            }

            // Track which purpose is currently selected
            var selectedPurp by remember { mutableStateOf<TranxPurp?>(null) }

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
                    PurposeCard(
                        tranxPurp = tranxPurp,
                        isSelected = (tranxPurp == selectedPurp),
                        onClick = {
                            selectedPurp = tranxPurp
                            onDone(tranxPurp)
                        },
                        modifier = Modifier.fillMaxWidth()
>>>>>>> 321d128 (updated send to be more dynamic)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
<<<<<<< HEAD
>>>>>>> 5c7011a (fixed preview animations)
=======

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
@Composable
private fun PurposeScreenPreview() {
    MaterialTheme {
        PurposeScreen(
            balance = Amount.fromString("SLE", "25"),
            onBack = {},
            onDone = {}
        )
    }
}
>>>>>>> 321d128 (updated send to be more dynamic)

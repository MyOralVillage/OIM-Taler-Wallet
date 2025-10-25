package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.House
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
import net.taler.database.data_models.tranxPurpLookup
import net.taler.wallet.oim.send.components.WoodTableBackground
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper

/**
 * Card representing a single transaction purpose.
 *
 * Displays the purpose icon, highlights selection, and triggers [onClick] when tapped.
 *
 * @param tranxPurp The purpose data (icon, label, color).
 * @param isSelected Whether this purpose is currently selected.
 * @param onClick Lambda invoked when the card is tapped.
 * @param modifier Optional [Modifier] for layout customization.
 */
@Composable
fun PurposeCard(
    tranxPurp: TranxPurp,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colour = Color(tranxPurp.colourInt())
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 6.dp else 3.dp,
                color = colour,
                shape = RoundedCornerShape(10.dp)
            ),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isSelected) colour.copy(alpha = 0.01f)
                else colour.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .padding(6.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(tranxPurp.resourceMapper()),
                contentDescription = tranxPurp.cmp,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Purpose selection screen.
 *
 * Shows all available transaction purposes in a scrollable grid.
 * Only one purpose can be selected at a time. Selected purpose is returned via [onDone].
 *
 * @param balance Current wallet balance displayed at the top.
 * @param onBack Lambda invoked when the back button is pressed.
 * @param onDone Lambda invoked when a purpose is selected; returns the selected [TranxPurp].
 * @param columns Number of columns in the grid layout.
 */
@Composable
fun PurposeScreen(
    balance: Amount,
    onBack: () -> Unit,
    onDone: (TranxPurp) -> Unit,
    columns: Int = 3,
    onHome: () -> Unit = {}
) {
    var selected by remember { mutableStateOf<TranxPurp?>(null) }

    Box(Modifier.fillMaxSize()) {
        WoodTableBackground()

        // Top-left back button
        FilledTonalButton(
            onClick = onBack,
            modifier = Modifier
                .padding(12.dp)
                .align(Alignment.TopStart)
        ) { Icon(
            Icons.Filled.ArrowBackIosNew,
            contentDescription = "Home",
            tint = Color.White,
            modifier = Modifier.size(34.dp),
        ) }

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 56.dp)
        ) {

            Spacer(Modifier.height(16.dp))

            // Scrollable grid of purposes
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sortedPurposes = tranxPurpLookup.values
                    .sortedBy { it.cmp }

                items(sortedPurposes) { p ->
                    PurposeCard(
                        tranxPurp = p,
                        isSelected = (p == selected),
                        onClick = {
                            selected = p
                            onDone(p)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.66f)
                    )
                }
            }
        }
    }
}

/**
 * Preview for PurposeScreen.
 */
@Preview(showBackground = true)
@Composable
fun PurposeScreenPreview() {
    MaterialTheme {
        PurposeScreen(
            balance = Amount.fromString("KUDOS", "35"),
            onBack = {},
            onDone = {}
        )
    }
}
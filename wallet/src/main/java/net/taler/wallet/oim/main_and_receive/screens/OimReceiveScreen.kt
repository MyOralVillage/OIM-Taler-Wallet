package net.taler.wallet.oim.main_and_receive.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.database.data_models.Amount
import net.taler.database.data_models.tranxPurpLookup
import net.taler.wallet.oim.utils.assets.OimColours
import net.taler.wallet.oim.top_bar.OimTopBarCentered
import net.taler.wallet.oim.notes.NotesGalleryOverlay
import net.taler.wallet.oim.notes.StackedNotes
import net.taler.wallet.oim.utils.assets.WoodTableBackground
import net.taler.wallet.oim.utils.res_mappers.UIIcons
import net.taler.wallet.oim.utils.res_mappers.resourceMapper
import net.taler.wallet.peer.IncomingTerms

@Composable
fun OimReceiveScreen(
    terms: IncomingTerms?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    balance: Amount
) {

        var isStackExpanded by remember { mutableStateOf(false) }
        var showStackPreview by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        // Shake offset state (applies to both buttons)
        var shakeOffset by remember { mutableStateOf(0f) }

        // Shake function: horizontal wobble
        fun onChest() {
            scope.launch {
                val pattern = listOf(-12f, 12f, -8f, 8f, 0f)
                for (x in pattern) {
                    shakeOffset = x
                    delay(45)
                }
            }
        }


        Box(
            Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Background
            WoodTableBackground(modifier = Modifier.fillMaxSize())

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // OIM Top Bar
                OimTopBarCentered(
                    balance = balance,
                    colour = OimColours.INCOMING_COLOUR,
                    onChestClick = { onChest() }
                )

                // Content below the top bar
                if (terms != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))

                            // Top section: Notes + incoming icon
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(250.dp)
                                    .padding(horizontal = 16.dp)
                            ) {
                                // Notes stack
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val amount = terms.amountEffective
                                    val bitmaps = remember(amount) { amount.resourceMapper() }

                                    if (bitmaps.isNotEmpty()) {
                                        StackedNotes(
                                            noteResIds = bitmaps,
                                            noteHeight = 79.dp,
                                            noteWidth = 115.dp,
                                            expanded = isStackExpanded,
                                            onClick = {
                                                if (!isStackExpanded) {
                                                    isStackExpanded = true
                                                    scope.launch {
                                                        delay(400)
                                                        showStackPreview = true
                                                    }
                                                }
                                            }
                                        )
                                    } else {
                                        Text(
                                            text = amount.toString(),
                                            style = MaterialTheme.typography.displayMedium,
                                            color = Color.White
                                        )
                                    }
                                }

                                // Incoming icon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .width(140.dp)
                                        .height(140.dp),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    Image(
                                        bitmap = UIIcons("incoming_transaction").resourceMapper(),
                                        contentDescription = "Incoming transaction",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Spacer(modifier = Modifier.weight(1f))
                        }

                        // Purpose icon (center bottom)
                        val summary = terms.contractTerms.summary
                        val purpose = tranxPurpLookup[summary]

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                if (purpose != null) {
                                    Image(
                                        painter = painterResource(id = purpose.resourceMapper()),
                                        contentDescription = summary,
                                        modifier = Modifier.size(150.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }

                        // Deny button (bottom-left)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 24.dp, bottom = 24.dp)
                                .offset(x = shakeOffset.dp)
                        ) {
                            ActionButton(
                                icon = Icons.Default.Close,
                                color = OimColours.OUTGOING_COLOUR,
                                enabled = true,
                                onClick = onReject
                            )
                        }

                        // Accept button (bottom-right)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 24.dp, bottom = 24.dp)
                                .offset(x = shakeOffset.dp)
                        ) {
                            ActionButton(
                                icon = Icons.Default.Check,
                                color = OimColours.INCOMING_COLOUR,
                                enabled = true,
                                onClick = onAccept
                            )
                        }
                    }
                }
            }

            // Loading overlay
            if (terms == null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable(enabled = true, onClick = {})
                        .zIndex(10f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            // Stack preview overlay
            NotesGalleryOverlay(
                isVisible = showStackPreview,
                onDismiss = {
                    showStackPreview = false
                    scope.launch {
                        delay(200)
                        isStackExpanded = false
                    }
                },
                drawableResIds = terms?.amountEffective?.resourceMapper() ?: emptyList(),
                noteHeight = 115.dp
            )
        }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (enabled) color else color.copy(alpha = 0.5f)
    val contentColor = if (enabled) Color.White else Color.White.copy(alpha = 0.7f)

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor,
            contentColor = contentColor,
            disabledContentColor = contentColor
        ),
        shape = CircleShape,
        modifier = Modifier
            .size(80.dp)
            .shadow(elevation = if (enabled) 12.dp else 0.dp, shape = CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = contentColor
        )
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=920dp,height=460dp,orientation=landscape",
    name = "OIM Receive Screen - Loading"
)
@Composable
fun OIMReceiveScreenLoadingPreview() {
    MaterialTheme {
        OimReceiveScreen(
            terms = null,
            onAccept = { },
            onReject = { },
            balance = Amount.fromString("SLE", "100.00")
        )
    }
}

@Preview(
    showBackground = true,
    name = "Small Landscape Phone 640x360dp (xhdpi)",
    device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape"
)
@Composable
fun OIMReceiveScreenPreview_SmallPhoneXhdpi() {
    OIMReceiveScreenLoadingPreview()
}
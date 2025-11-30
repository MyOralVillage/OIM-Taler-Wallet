package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.database.data_models.Amount
import net.taler.database.data_models.HLTH_MEDS
import net.taler.database.data_models.TranxPurp
import net.taler.wallet.oim.notes.NotePreviewOverlay
import net.taler.wallet.oim.notes.NotesGalleryOverlay
import net.taler.wallet.oim.notes.StackedNotes
import net.taler.wallet.oim.utils.res_mappers.resourceMapper
import net.taler.wallet.oim.utils.assets.WoodTableBackground
import net.taler.wallet.oim.utils.assets.generateQrBitmap
import net.taler.wallet.oim.utils.assets.OimColours
import net.taler.wallet.oim.top_bar.OimTopBarCentered

@Composable
fun QrScreen(
    talerUri: String?,
    amount: Amount,
    balance: Amount,
    purpose: TranxPurp?,
    onBack: () -> Unit,
    onHome: () -> Unit = {}
) {
    var selectedNoteResId by remember { mutableStateOf<Int?>(null) }
    var showStackPreview by remember { mutableStateOf(false) }
    var isStackExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        WoodTableBackground(modifier = Modifier.fillMaxSize())

        // MAIN CONTENT COLUMN
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OimTopBarCentered(
                balance = balance,
                onChestClick = onHome,
                colour = OimColours.OUTGOING_COLOUR
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 20.dp, horizontal = 5.dp),
            ) {
                // FIXED: Larger QR container
                val configuration = LocalConfiguration.current
                val screenHeightDp = configuration.screenHeightDp.dp
                val qrSize = screenHeightDp * 0.32f          // increased from 0.24 → 0.32

                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- QR code box ---
                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 8.dp,
                        modifier = Modifier.size(qrSize)
                    ) {
                        if (talerUri == null) {
                            Column(
                                Modifier.fillMaxSize().padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(12.dp))
                                Text("Preparing payment…", color = Color.Black)
                            }
                        } else {
                            val qrBitmap = remember(talerUri) { generateQrBitmap(talerUri, 1024) }
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Taler QR",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // --- Purpose BELOW QR ---
                    if (purpose != null) {
                        val purposeSize = qrSize * 0.55f      // slightly over half the QR size

                        Surface(
                            modifier = Modifier.size(purposeSize),
                            color = Color(purpose.colourInt()),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 6.dp
                        ) {
                            Image(
                                painter = painterResource(purpose.resourceMapper()),
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                // --- Amount Notes Column ---
                Column(
                    modifier = Modifier
                        .weight(2f)
                        .wrapContentWidth()
                        .padding(vertical = 15.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StackedNotes(
                        noteResIds = amount.resourceMapper(),
                        noteHeight = 60.dp,
                        noteWidth = 90.dp,
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
                }
            }
        }

        // ------------- OVERLAYS (NOW ABOVE EVERYTHING) ---------------

        if (selectedNoteResId != null) {
            NotePreviewOverlay(
                noteResId = selectedNoteResId!!,
                onDismiss = { selectedNoteResId = null }
            )
        }

        NotesGalleryOverlay(
            isVisible = showStackPreview,
            onDismiss = {
                showStackPreview = false
                scope.launch {
                    delay(200)
                    isStackExpanded = false
                }
            },
            drawableResIds = amount.resourceMapper(),
            noteHeight = 115.dp
        )
    }
}

@Preview(device = "spec:width=920dp,height=460dp,orientation=landscape")
@Composable
fun QrScreenLoadingPreview() {
    MaterialTheme {
        QrScreen(
            talerUri = null,
            amount = Amount.fromString("EUR", "999.99"),
            balance = Amount.fromString("EUR", "100.00"),
            purpose = HLTH_MEDS,
            onBack = { },
            onHome = { }
        )
    }
}

@Preview(
    showBackground = true,
    name = " Small Landscape Phone 640x360dp (xhdpi)",
    device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape"
)
@Composable
fun QRLoadingPreview_SmallPhoneXhdpi() {
    QrScreenLoadingPreview()
}

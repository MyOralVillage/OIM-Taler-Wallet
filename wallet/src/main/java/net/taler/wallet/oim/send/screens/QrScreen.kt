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
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/**
 * ## QR Screen
 *
 * Displays a Taler payment QR code for peer-to-peer transfers.
 * Shows the amount, currency, and optional transaction purpose.
 * Handles a loading state while the QR code is being generated.
 *
 * The screen includes navigation controls (Home and Back buttons) and
 * a table-textured background via [WoodTableBackground].
 *
 * @param talerUri The Taler payment URI to encode as a QR code.
 *                 Pass `null` to show a loading state while preparing.
 * @param amount The payment amount and currency to display.
 * @param purpose Optional transaction purpose icon to show next to the QR code.
 * @param onBack Callback invoked when the Back button is pressed.
 * @param onHome Optional callback invoked when the Home button is pressed.
 */
@Composable
fun QrScreen(
    talerUri: String?,
    amount: Amount,
    balance: Amount,
    purpose: TranxPurp?,
    onBack: () -> Unit,
    onHome: () -> Unit = {}
) {
    // States for the note preview and gallery overlays
    var selectedNoteResId by remember { mutableStateOf<Int?>(null) }
    var showStackPreview by remember { mutableStateOf(false) }
    var isStackExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Background
        WoodTableBackground(modifier = Modifier.fillMaxSize())

        // Main column
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
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {

                // LEFT: QR (fixed square)
                val qrSize = (LocalWindowInfo.current.containerSize.height * 0.34f).dp

                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(qrSize)
                        .padding(end = 12.dp)
                ) {
                    if (talerUri == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
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
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                // CENTER: Notes (closer spacing, smaller size, wrap content)
                Column(
                    modifier = Modifier
                        .wrapContentWidth()
                        .padding(end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StackedNotes(
                        noteResIds = amount.resourceMapper(),
                        noteHeight = 32.dp,   // smaller = closer
                        noteWidth = 90.dp,    // smaller width, less horizontal pressure
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

                // RIGHT: Purpose — takes **ALL remaining space**
                if (purpose != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()    // fills everything remaining
                            .aspectRatio(1f),  // stays square
                        color = Color(purpose.colourInt()),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 4.dp
                    ) {
                        Image(
                            painter = painterResource(purpose.resourceMapper()),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

        }
    }
}


/**
 * Preview: QR Screen with loading state
 */
@Preview(
    device = "spec:width=920dp,height=460dp,orientation=landscape"
)
@Composable
fun QrScreenLoadingPreview() {
    MaterialTheme {
        QrScreen(
            talerUri = null,  // Loading state
            amount = Amount.fromString("EUR", "25.50"),
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
package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
import net.taler.wallet.oim.res_mapping_extensions.resourceMapper
import net.taler.wallet.oim.send.components.WoodTableBackground
import net.taler.wallet.oim.send.components.generateQrBitmap

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
    purpose: TranxPurp?,
    onBack: () -> Unit,
    onHome: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize()) {
        // Background
        WoodTableBackground(modifier = Modifier.fillMaxSize(), light = false)

        // Top navigation controls
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
            FilledTonalButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null
                )
                Spacer(Modifier.width(8.dp))
                Text("Back")
            }
        }

        // Main content: QR code and amount/purpose
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR code or loading spinner
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.size(360.dp)
            ) {
                if (talerUri == null) {
                    // Loading state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(12.dp))
                        Text("Preparing payment…", color = Color.Black)
                    }
                } else {
                    // Generate and display the QR code bitmap
                    val qrBitmap = remember(talerUri) { generateQrBitmap(talerUri, 1024) }
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Taler QR",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // Amount and purpose display
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 24.dp)
            ) {
                Text(
                    text = amount.amountStr,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp
                )
                Text(
                    text = amount.spec?.name ?: amount.currency,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                )
                if (purpose != null) {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(12.dp),
                        shadowElevation = 2.dp
                    ) {
                        Image(
                            painter = painterResource(purpose.resourceMapper()),
                            contentDescription = null,
                            modifier = Modifier.size(112.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .alpha(0f)
                    )
                }
            }
        }
    }
}

/**
 * Preview for [QrScreen].
 */
@Preview(showBackground = true, widthDp = 1280, heightDp = 800)
@Composable
private fun QrScreenPreview() {
    MaterialTheme {
        QrScreen(
            talerUri = "ext+taler://pay-push/exchange.demo.taler.net/EXAMPLEPURSEID",
            amount = Amount.fromString("KUDOS", "10"),
            purpose = null,
            onBack = {},
            onHome = {}
        )
    }
}

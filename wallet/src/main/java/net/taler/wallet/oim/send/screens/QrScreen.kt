package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.database.data_models.Amount
import net.taler.database.data_models.TranxPurp
import net.taler.wallet.oim.res_mapping_extensions.UIIcons
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
    onBack: () -> Unit,          // still here for signature compatibility
    onHome: () -> Unit = {}
) {
    Box(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // background
        WoodTableBackground(modifier = Modifier.fillMaxSize(), light = false)

        // TOP-CENTER chest button
        IconButton(
            onClick = onHome,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 12.dp)
        ) {
            Image(
                bitmap = UIIcons("chest_open").resourceMapper(),
                contentDescription = "Done / back to chest",
                modifier = Modifier.size(70.dp),
                contentScale = ContentScale.Fit
            )
        }

        // main row: QR left, info right
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: QR
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.size(300.dp)
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
                    val qrBitmap = remember(talerUri) { generateQrBitmap(talerUri, 2048) }
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Taler QR",
                        modifier = Modifier
                            .fillMaxSize()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // RIGHT: centered amount + purpose
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(start = 24.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = amount.amountStr,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 56.sp
                )
                Text(
                    text = amount.spec?.name ?: amount.currency,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 26.sp
                )

                Spacer(Modifier.height(24.dp))

                if (purpose != null) {
                    Surface(
                        color = Color(0x33000000),
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp
                    ) {
                        Image(
                            painter = painterResource(purpose.resourceMapper()),
                            contentDescription = null,
                            modifier = Modifier.size(150.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .alpha(0f)
                    )
                }
            }
        }
    }
}









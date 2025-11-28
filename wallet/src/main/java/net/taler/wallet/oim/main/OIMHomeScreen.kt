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
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GNU Taler; see the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.taler.wallet.oim.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.taler.database.data_models.Amount
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.oim.resourceMappers.Background
import net.taler.wallet.oim.resourceMappers.UIIcons

/**
 * Stateless OIM home screen that shows the wooden background, QR scan shortcut, and the
 * navigation chest button.
 *
 * @param modifier layout modifier applied by the caller.
 * @param onScanQrClick invoked when the scan shortcut is pressed.
 * @param onChestClick invoked when the central chest is tapped.
 * @param onBackToTalerClick optional callback for the back-to-Taler affordance.
 */
@Composable
fun OIMHomeScreenContent(
    modifier: Modifier = Modifier,
    onScanQrClick: () -> Unit,
    onChestClick: () -> Unit,
    onBackToTalerClick: () -> Unit = {}
) {
    // OIM mode only valid in portrait -> scale to 50% of width
    val configuration = LocalWindowInfo.current
    val width = configuration.containerSize.width
    val chestSize = (width * 0.5f).dp

    TalerSurface {
        Box(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Background
            Image(
                painter = painterResource(Background(LocalContext.current).resourceMapper()),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Content layer
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // Chest
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(chestSize)
                        .clickable(onClick = onChestClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = UIIcons("chest_closed").resourceMapper(),
                        contentDescription = "Chest",
                        modifier = Modifier.size(chestSize),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    device = "spec:width=920dp,height=460dp,orientation=landscape",
    name = "OIM Home Screen"
)
@Composable
fun OIMHomeScreenPreview() {
    MaterialTheme {
        OIMHomeScreenContent(
            onScanQrClick = { },
            onChestClick = { },
            onBackToTalerClick = { }
        )
    }
}

@Preview(
    showBackground = true,
    name = "Small Landscape Phone 640x360dp (xhdpi)",
    device = "spec:width=640dp,height=360dp,dpi=320,orientation=landscape"
)
@Composable
fun OIMHomeScreenPreview_SmallPhoneXhdpi() {
    OIMHomeScreenPreview()
}
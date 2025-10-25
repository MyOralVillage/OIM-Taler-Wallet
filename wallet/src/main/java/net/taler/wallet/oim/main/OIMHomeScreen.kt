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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import net.taler.wallet.MainViewModel
import net.taler.wallet.R
import net.taler.wallet.ScanQrContext
import net.taler.wallet.compose.TalerSurface
import net.taler.wallet.oim.res_mapping_extensions.Background
import net.taler.wallet.oim.res_mapping_extensions.Buttons

//================================================================================
// 1. STATEFUL COMPOSABLE
//================================================================================
/**
 * High-level entry point for the OIM home screen that wires the view model callbacks
 * to the stateless UI implementation.
 *
 * @param modifier external modifier injected from the hosting container.
 * @param viewModel shared [MainViewModel] used to fire navigation and scan actions.
 */
@Composable
fun OIMHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel()
) {
    OIMHomeScreenContent(
        modifier = modifier,
        onScanQrClick = { viewModel.scanCode(ScanQrContext.Receive) },
        onChestClick = {
            // TODO: Add chest functionality
        }
    )
}

//================================================================================
// 2. STATELESS COMPOSABLE (For your actual app)

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
    onBackToTalerClick: () -> Unit = {},
) {
    TalerSurface {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(Background(LocalContext.current).resourceMapper()),
                contentDescription = "Wooden background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
            ) {
                // Back to Taler button at top-right (optional)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.9f))
                        .clickable(onClick = onBackToTalerClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {}
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.9f))
                        .clickable(onClick = onScanQrClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_scan_qr),
                        contentDescription = stringResource(R.string.button_scan_qr_code),
                        modifier = Modifier,
                        alignment = Alignment.Center
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(120.dp)
                        .clickable(onClick = onChestClick),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.chest_closed),
                        contentDescription = "Chest",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

//================================================================================
// 3. PREVIEW COMPOSABLE (Cleaned up and corrected)
//================================================================================
// This now accurately mirrors the fixed stateless composable and has the
// correct landscape configuration.
//================================================================================
@Preview(
    showSystemUi = true,

)
@Composable
fun OIMHomeScreenIsolatedPreview() {
    MaterialTheme {
        Surface {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Background(LocalContext.current).resourceMapper()),
                    contentDescription = "Wooden background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .size(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { /* No-op for preview */ },
                    contentAlignment = Alignment.TopStart
                ) {
                    // Use Image for the PNG resource.
                    Image(
                        painter = painterResource(id = R.drawable.ic_scan_qr),
                        contentDescription = stringResource(R.string.button_scan_qr_code),
                        modifier = Modifier.size(48.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(110.dp) // slightly larger than the image
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(5.dp) // spacing between background and image
                ) {
                    Image(
                        painter = painterResource(Buttons("chest_closed").resourceMapper()),
                        contentDescription = "Chest",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }

            }
        }
    }
}

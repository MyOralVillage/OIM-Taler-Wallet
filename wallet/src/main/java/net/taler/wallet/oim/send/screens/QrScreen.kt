/*
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

package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.wallet.oim.send.components.WOOD_TABLE
import net.taler.wallet.oim.send.components.assetPainter
import net.taler.wallet.oim.send.components.generateQrBitmap

@Composable
fun QrScreen(
    amount: Int,
    currencyCode: String,
    displayLabel: String,
    purpose: String,
    onBack: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (LocalInspectionMode.current) {
            Box(Modifier.fillMaxSize().background(Color(0xFF3A2F28)))
        } else {
            Image(
                painter = assetPainter(WOOD_TABLE),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Wallet-friendly payload, URL-encoded
        val payload = remember(amount, currencyCode, purpose) {
            val a = java.net.URLEncoder.encode(amount.toString(), Charsets.UTF_8.name())
            val c = java.net.URLEncoder.encode(currencyCode, Charsets.UTF_8.name())
            val p = java.net.URLEncoder.encode(purpose, Charsets.UTF_8.name())
            "oim://pay?amount=$a&currency=$c&purpose=$p"
        }
        val qr = remember(payload) { generateQrBitmap(payload, 720) }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onBack)
                )
                Spacer(Modifier.height(24.dp))
                Surface(color = Color.White, shape = RoundedCornerShape(24.dp)) {
                    Image(
                        bitmap = qr.asImageBitmap(),
                        contentDescription = "QR",
                        modifier = Modifier.size(300.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = amount.toString(),
                    color = Color.White,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = displayLabel,
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

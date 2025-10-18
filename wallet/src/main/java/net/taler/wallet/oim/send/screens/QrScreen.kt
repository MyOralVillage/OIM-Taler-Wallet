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

/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.wallet.oim.send.components.WOOD_TABLE
import net.taler.wallet.oim.send.components.assetPainterOrPreview
import net.taler.wallet.oim.send.components.generateQrBitmap
import androidx.core.net.toUri

/**
 * QR screen with wood background.
 *
 * You can pass display fields (amount/currency/purpose) separately while the QR
 * is generated from [talerUri]. If any display field is null, we try to infer it
 * from the URI (amount=SLE:3, summary=...).
 */
@Composable
fun QrScreen(
    talerUri: String,
    amountText: String? = null,
    currencyCode: String? = null,
    displayLabel: String? = null,
    purpose: String? = null,
    onBack: () -> Unit,
) {
    // Parse what we can from the URI for display fallbacks
    val parsed = remember(talerUri) { parseFromTalerUri(talerUri) }
    val uiAmount = amountText ?: parsed.amountNumber
    val uiCurrency = currencyCode ?: parsed.currency
    val uiLabel = displayLabel ?: uiCurrency
    val uiPurpose = purpose ?: parsed.summary

    val qr = remember(talerUri) { generateQrBitmap(talerUri, 720) }

    Box(Modifier.fillMaxSize()) {
        // SAME WOOD BACKGROUND AS BEFORE
        Image(
            painter = assetPainterOrPreview(WOOD_TABLE),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(8.dp)
                .size(40.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 6.dp
            ) {
                Image(
                    bitmap = qr.asImageBitmap(),
                    contentDescription = "QR",
                    modifier = Modifier.size(320.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                uiAmount?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        fontSize = 64.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                uiLabel?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                uiPurpose?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private data class ParsedDisplay(
    val amountNumber: String? = null,
    val currency: String? = null,
    val summary: String? = null,
)

/** Minimal parser to recover amount/currency/summary from common Taler URIs. */
private fun parseFromTalerUri(talerUri: String): ParsedDisplay {
    val uri = runCatching { talerUri.toUri() }.getOrNull() ?: return ParsedDisplay()
    val amountParam = uri.getQueryParameter("amount") // "SLE:3" etc.
    val summary = uri.getQueryParameter("summary") ?: uri.getQueryParameter("subject")

    val (currency, number) = amountParam
        ?.split(':', limit = 2)
        ?.let { it.getOrNull(0) to it.getOrNull(1) }
        ?: (null to null)

    return ParsedDisplay(
        amountNumber = number,
        currency = currency,
        summary = summary
    )
}


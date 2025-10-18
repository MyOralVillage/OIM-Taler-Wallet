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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import net.taler.wallet.oim.send.components.*

/**
 * Screen for selecting or entering the purpose of a payment.
 *
 * Users can pick from predefined purpose icons or enter a custom purpose.
 *
 * @param balance The current balance of the user, displayed in the top bar.
 * @param onBack Callback invoked when the back button is pressed.
 * @param onDone Callback invoked when the user selects a purpose or enters a custom one.
 *               The selected or entered purpose string is passed as a parameter.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurposeScreen(
    balance: Int,
    onBack: () -> Unit,
    onDone: (String) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
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
                tint = androidx.compose.ui.graphics.Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OimTopBarCentered(balance = balance, onSendClick = { })

            Spacer(Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PurposeIcons.forEach { (path, label) ->
                    PurposeTile(
                        path = path,
                        label = label,
                        modifier = Modifier
                            .width(140.dp)
                            .height(140.dp),
                        onPick = { onDone(label) }
                    )
                }
            }

            var custom by remember { mutableStateOf(TextFieldValue()) }
            OutlinedTextField(
                value = custom,
                onValueChange = { custom = it },
                label = { Text("Custom purpose") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onDone(custom.text.ifBlank { "Payment" }) },
                modifier = Modifier.align(Alignment.End)
            ) { Text("Continue") }

            Spacer(Modifier.height(8.dp))
        }
    }
}
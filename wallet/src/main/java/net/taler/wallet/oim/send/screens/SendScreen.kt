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
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.taler.wallet.oim.send.components.*

@Composable
fun SendScreen(
    balance: Int,
    amount: Int,
    onAdd: (Int) -> Unit,
    onRemoveLast: (Int) -> Unit,
    onChoosePurpose: () -> Unit,
    onSend: () -> Unit
) {
    Box(Modifier.fillMaxSize()) {
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

        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            OimTopBar(balance = balance, onSendClick = onSend)

            Spacer(Modifier.height(24.dp))

            NotesStrip(
                noteThumbWidth = 120.dp,
                onAdd = onAdd,
                onRemoveLast = onRemoveLast
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = amount.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 84.sp
                    )
                    Text(
                        text = "Leones",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    androidx.compose.material3.Button(onClick = onChoosePurpose) {
                        Text("Choose purpose")
                    }
                    Spacer(Modifier.height(12.dp))
                    ExtendedFloatingActionButton(
                        onClick = onSend,
                        icon = { androidx.compose.material3.Icon(Icons.Filled.Send, null) },
                        text = { Text("Send") },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    )
                }
            }
        }
    }
}

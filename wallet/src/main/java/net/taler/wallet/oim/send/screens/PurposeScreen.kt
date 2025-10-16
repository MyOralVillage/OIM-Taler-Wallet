package net.taler.wallet.oim.send.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import net.taler.wallet.oim.send.components.PurposeIcons
import net.taler.wallet.oim.send.components.PurposeTile
import net.taler.wallet.oim.send.components.WOOD_TABLE
import net.taler.wallet.oim.send.components.assetPainter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurposeScreen(
    balance: Int,
    onBack: () -> Unit,
    onDone: (String) -> Unit
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

        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text("$balance Leones", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PurposeIcons.forEach { (path, label) ->
                    PurposeTile(
                        path = path,
                        label = label,
                        modifier = Modifier
                            .width(120.dp)
                            .height(120.dp),
                        onPick = { onDone(label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            var custom by remember { mutableStateOf(TextFieldValue()) }
            OutlinedTextField(
                value = custom,
                onValueChange = { custom = it },
                label = { Text("Custom purpose") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { onDone(custom.text.ifBlank { "Payment" }) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Continue")
            }
        }
    }
}

package net.taler.wallet.oim.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.taler.database.data_models.tranxPurpLookup
import net.taler.wallet.oim.utils.resourceMappers.Background
import net.taler.wallet.oim.utils.resourceMappers.UIIcons
import net.taler.wallet.oim.utils.resourceMappers.resourceMapper
import net.taler.wallet.oim.send.components.NotesGalleryOverlay
import net.taler.wallet.oim.send.components.StackedNotes
import net.taler.wallet.oim.send.components.WoodTableBackground
import net.taler.wallet.peer.IncomingTerms

@Composable
fun OIMReceiveScreen(
    terms: IncomingTerms?,
    onAccept: () -> Unit,
    onReject: () -> Unit,
) {
    var isStackExpanded by remember { mutableStateOf(false) }
    var showStackPreview by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Background
        WoodTableBackground(modifier = Modifier.fillMaxSize(), light = false)

        // 2. Content
        if (terms != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(20.dp))

                    // --- TOP SECTION: Notes (Center) + Incoming Icon (Right) ---
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        // Center: Notes Stack
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter) // Start from top middle
                                .padding(top = 20.dp), // Add some top padding
                            contentAlignment = Alignment.Center
                        ) {
                            val amount = terms.amountEffective
                            val bitmaps = remember(amount) { amount.resourceMapper() }
                            if (bitmaps.isNotEmpty()) {
                                StackedNotes(
                                    noteResIds = bitmaps,
                                    noteHeight = 79.dp,
                                    noteWidth = 115.dp,
                                    expanded = isStackExpanded,
                                    onClick = {
                                        if (!isStackExpanded) {
                                            isStackExpanded = true
                                            scope.launch {
                                                delay(400) // Wait for unstack animation
                                                showStackPreview = true
                                            }
                                        }
                                    }
                                )
                            } else {
                                Text(
                                    text = amount.toString(),
                                    style = MaterialTheme.typography.displayMedium,
                                    color = Color.White
                                )
                            }
                        }

                        // Right: Receive Money Icon
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .width(140.dp) // Increased size slightly
                                .height(140.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Image(
                                bitmap = UIIcons("incoming_transaction").resourceMapper(),
                                contentDescription = "Incoming transaction",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                // --- MIDDLE SECTION: Spacer to push content up ---
                    Spacer(modifier = Modifier.weight(1f))
                }

                // --- BOTTOM SECTION: Buttons & Purpose ---
                
                // Purpose (Bottom Center)
                val summary = terms.contractTerms.summary
                val purpose = tranxPurpLookup[summary]
                
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp) // Align with buttons roughly
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (purpose != null) {
                            Image(
                                painter = painterResource(id = purpose.resourceMapper()),
                                contentDescription = summary,
                                modifier = Modifier.size(150.dp), // Larger icon
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        // Removed text display as requested
                    }
                }

                // Deny Button (Bottom Left)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 24.dp, bottom = 24.dp) // Adjusted padding
                ) {
                    ActionButton(
                        icon = Icons.Default.Close,
                        color = MaterialTheme.colorScheme.error,
                        enabled = true,
                        onClick = onReject
                    )
                }

                // Accept Button (Bottom Right)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 24.dp) // Adjusted padding
                ) {
                    ActionButton(
                        icon = Icons.Default.Check,
                        color = Color(0xFF4CAF50), // Green
                        enabled = true,
                        onClick = onAccept
                    )
                }
            }
        }

        // Loading Overlay (only if terms are null)
        if (terms == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)) // Grey out
                    .clickable(enabled = true, onClick = {}) // Consume touches
                    .zIndex(10f), // Ensure on top
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
        
        // Stack Preview Overlay (Gallery)
        NotesGalleryOverlay(
            isVisible = showStackPreview,
            onDismiss = {
                showStackPreview = false
                scope.launch {
                    delay(200)
                    isStackExpanded = false
                }
            },
            drawableResIds = terms?.amountEffective?.resourceMapper() ?: emptyList(),
            noteHeight = 115.dp
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    // "Greyed out" visual when disabled, but keep visible
    val containerColor = if (enabled) color else color.copy(alpha = 0.5f)
    val contentColor = if (enabled) Color.White else Color.White.copy(alpha = 0.7f)

    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = containerColor, // Keep color but dimmed
            contentColor = contentColor,
            disabledContentColor = contentColor
        ),
        shape = CircleShape, // Circular shape
        modifier = Modifier
            .size(80.dp) // Fixed size for circle
            .shadow(elevation = if (enabled) 12.dp else 0.dp, shape = CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp), // Larger icon
            tint = contentColor
        )
    }
}



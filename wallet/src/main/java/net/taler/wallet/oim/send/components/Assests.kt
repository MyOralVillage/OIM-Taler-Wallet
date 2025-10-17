/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import net.taler.wallet.oim.res_mapping_extensions.Tables

/**
 * Simple helper to paint the wooden table background using drawable mappers.
 * @param light true → light wood, false → dark wood
 */
@Composable
fun WoodTableBackground(
    modifier: Modifier = Modifier,
    light: Boolean = false
) {
    val bmp = Tables(light).resourceMapper()
    Image(
        bitmap = bmp,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

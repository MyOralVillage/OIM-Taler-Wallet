/*
 * GPLv3-or-later
 */
package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import net.taler.wallet.oim.res_mapping_extensions.Background

/**
 * Simple helper to paint the wooden table background using drawable mappers.
 */
@Composable
fun WoodTableBackground(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(Background(LocalContext.current).resourceMapper()),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

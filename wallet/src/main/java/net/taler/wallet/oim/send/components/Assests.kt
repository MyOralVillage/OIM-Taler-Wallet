/**
 * ## Asser loader - WoodTableBackground
 *
 * Composable utility for rendering the wooden table texture beneath
 * send screen in the OIM Send flow. Uses the `Tables` drawable
 * mapper to load either a light or dark wood variant.
 *
 * Typically serves as a static visual base for the Send screen and
 * is layered behind animated components such as [NoteFlyer] and [NotesPile].
 *
 * @param modifier Layout modifier for positioning or scaling the background.
 * @param light When `true`, uses a light wood texture; otherwise uses dark wood.
 * @see net.taler.wallet.oim.res_mapping_extensions.Tables
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
 * Paints the wooden table background using drawable mappers.
 * @param modifier Optional [Modifier] for styling, sizing, or positioning the background.
 * @param light Deprecated; previously used for legacy behavior. Pass `null`.
 */
@Composable
fun WoodTableBackground(
    modifier: Modifier = Modifier,
    light: Boolean? = null
) {
    Image(
        painter = painterResource(Background(LocalContext.current).resourceMapper()),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

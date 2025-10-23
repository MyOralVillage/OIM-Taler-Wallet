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
package net.taler.wallet.oim.send.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import net.taler.wallet.oim.res_mapping_extensions.Background

/**
 * Drawable-backed wooden table background.
 * @param light true → light wood, false → dark wood
 */
@Composable
fun OimWoodBackground(
    modifier: Modifier = Modifier,
    light: Boolean = false
) {
    Image(
        painter = painterResource(Background(LocalContext.current).resourceMapper()),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}

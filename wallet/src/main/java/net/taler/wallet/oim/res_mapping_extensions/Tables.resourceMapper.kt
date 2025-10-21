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

package net.taler.wallet.oim.res_mapping_extensions

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.imageResource
import net.taler.common.R.drawable.*

/**

Provides access to table image resources based on the given type.
This class is marked as internal, meaning it is only visible within the same module.
It maps a boolean flag to the appropriate image resource (light or dark wood table)
for use in Jetpack Compose UI components.

@param type Boolean flag indicating which table resource to use.

true → light wood table
false → dark wood table */
internal class Tables(val type: Boolean) {

    /**
    Returns the appropriate [ImageBitmap] for the table type.
    @return The [ImageBitmap] corresponding to either the light or dark wood table.
     */
    @Composable
    fun resourceMapper(): ImageBitmap =
        if (this.type)
            ImageBitmap.imageResource(table_light_wood_andrey_haimin)
        else
            ImageBitmap.imageResource(table_dark_wood_tara_meinczinger)
}
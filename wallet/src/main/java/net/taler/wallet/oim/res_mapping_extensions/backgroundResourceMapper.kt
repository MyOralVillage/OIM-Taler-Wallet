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

import android.content.Context
import androidx.compose.runtime.Composable
import net.taler.common.R.drawable.*
import android.content.res.Configuration
import androidx.annotation.DrawableRes

private fun isDarkModeOn(context: Context): Boolean {
    val nightModeFlags
    = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
}
/**
 Returns the background for oim based on configured light/dark mode.
@param type Boolean flag indicating which table resource to use.
 */
internal class Background(val ctx: Context) {

    /**
    Returns the appropriate [DrawableRes] for the table type.
    @return The [DrawableRes] corresponding to either the light or dark wood table.
     */
    @Composable
    @DrawableRes
    fun resourceMapper(): Int =
        if (isDarkModeOn(this.ctx))
            table_dark
        else
            table_light
}
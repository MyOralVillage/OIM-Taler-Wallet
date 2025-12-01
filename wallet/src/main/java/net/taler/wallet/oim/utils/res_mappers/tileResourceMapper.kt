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
package net.taler.wallet.oim.utils.res_mappers

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import net.taler.wallet.R.drawable.*
/** @param tile the case insensitive name of the tile */
internal class Tile(val tile: String) {

    /**
     * Maps a string-based button identifier into the corresponding [DrawableRes].
     *
     * ### Supported tile names and their corresponding resources:
     * - `"blank"`              → `tile_blank.webp`
     * - `"farm"`               → `tile_farm.webp`
     * - `"lake"`               → `tile_lake.webp`
     * - `"river"`              → `tile_river.webp`
     * - `"river_with_outlet"`  → `tile_river_with_inlet.webp`
     * @return The corresponding [DrawableRes] for the given tile name.
     * @throws IllegalArgumentException if the name does not match any of the supported types.
     */
    @Composable
    @DrawableRes
    fun resourceMapper() : Int =
        when (tile.lowercase().trim()) {
            "blank"             -> tile_blank
            "farm"              -> tile_farm
            "lake"              -> tile_lake
            "river"             -> tile_river
            "river_with_inlet"  -> tile_river_with_inlet
            else ->
                throw IllegalArgumentException("Invalid tile: ${tile.lowercase().trim()}")
        }
}
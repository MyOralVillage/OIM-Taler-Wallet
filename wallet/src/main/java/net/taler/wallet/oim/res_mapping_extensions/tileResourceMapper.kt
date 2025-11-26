package net.taler.wallet.oim.res_mapping_extensions

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
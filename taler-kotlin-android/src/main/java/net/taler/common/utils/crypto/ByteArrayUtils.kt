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

package net.taler.utils.crypto

/**
 * Utility functions for working with byte arrays.
 */
object ByteArrayUtils {

    /** Hexadecimal characters used for conversion. */
    private const val HEX_CHARS = "0123456789ABCDEF"

    /**
     * Converts a hexadecimal string to a [ByteArray].
     *
     * The input string must contain an even number of characters, each representing a hex digit
     * (0–9, A–F). Lowercase letters are not supported in this version.
     *
     * @param data Hexadecimal string (e.g., "4A6F686E").
     * @return ByteArray corresponding to the hex string.
     * @throws IllegalArgumentException if the input contains invalid hex.
     */
    fun hexStringToByteArray(data: String): ByteArray {
        require(data.length % 2 == 0) { "Hex string must have even length" }

        val result = ByteArray(data.length / 2)

        for (i in data.indices step 2) {
            val firstIndex = HEX_CHARS.indexOf(data[i])
            val secondIndex = HEX_CHARS.indexOf(data[i + 1])
            require(firstIndex >= 0 && secondIndex >= 0) { "Invalid hex character at position $i" }

            val octet = firstIndex.shl(4).or(secondIndex)
            result[i.shr(1)] = octet.toByte()
        }
        return result
    }
}

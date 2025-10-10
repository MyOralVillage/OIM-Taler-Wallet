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

package net.taler.common.utils.crypto

import kotlin.math.floor

/**
 * Utility functions for cryptographic conversions and encodings.
 *
 * Currently supports decoding Crockford’s Base32 encoding.
 */
object CryptoUtils {

    /**
     * Converts a single character in Crockford Base32 encoding to its numeric value.
     *
     * Handles common visual ambiguities:
     * - 'O' or 'o' → 0
     * - 'I', 'i', 'L', 'l' → 1
     * - 'U' or 'u' → 'V' (to match Crockford Base32)
     *
     * @param c Character to decode.
     * @return Integer value (0–31) corresponding to the character.
     * @throws Error if the character is not a valid Crockford Base32 symbol.
     */
    internal fun getValue(c: Char): Int {
        val a = when (c) {
            'o', 'O' -> '0'
            'i', 'I', 'l', 'L' -> '1'
            'u', 'U' -> 'V'
            else -> c
        }
        if (a in '0'..'9') {
            return a - '0'
        }
        val A = if (a in 'a'..'z') a.uppercaseChar() else a
        var dec = 0
        if (A in 'A'..'Z') {
            if ('I' < A) dec++
            if ('L' < A) dec++
            if ('O' < A) dec++
            if ('U' < A) dec++
            return A - 'A' + 10 - dec
        }
        throw Error("Encoding error: invalid character '$c'")
    }

    /**
     * Decodes a Crockford Base32 encoded string into a [ByteArray].
     *
     * Crockford Base32 uses 32 symbols to encode binary data. This function
     * handles padding automatically and processes the input efficiently.
     *
     * @param e The encoded string.
     * @return The decoded bytes as a [ByteArray].
     * @throws Error if the input contains invalid characters.
     */
    fun decodeCrock(e: String): ByteArray {
        val size = e.length
        var bitpos = 0
        var bitbuf = 0
        var readPosition = 0
        val outLen = floor((size * 5f) / 8).toInt() // each character encodes 5 bits
        val out = ByteArray(outLen)
        var outPos = 0

        while (readPosition < size || bitpos > 0) {
            if (readPosition < size) {
                val v = getValue(e[readPosition++])
                bitbuf = bitbuf.shl(5).or(v)
                bitpos += 5
            }
            while (bitpos >= 8) {
                val d = bitbuf.shr(bitpos - 8).and(0xff).toByte()
                out[outPos++] = d
                bitpos -= 8
            }
            if (readPosition == size && bitpos > 0) {
                // Left-align remaining bits
                bitbuf = bitbuf.shl(8 - bitpos).and(0xff)
                bitpos = if (bitbuf == 0) 0 else 8
            }
        }

        return out
    }
}
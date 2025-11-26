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

package net.taler.common.data_models

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents the specification of a currency, including formatting rules
 * and alternative unit names.
 *
 * @property name The canonical name of the currency (e.g., "Euro").
 * @property numFractionalInputDigits Number of fractional digits allowed for user input.
 * @property numFractionalNormalDigits Number of fractional digits normally displayed.
 * @property numFractionalTrailingZeroDigits Number of trailing zero digits displayed, if any.
 * @property altUnitNames Map of alternative unit names keyed by index
 *                        (0 = primary unit, 1+ = secondary units).
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class CurrencySpecification(
    val name: String,
    @SerialName("num_fractional_input_digits")
    val numFractionalInputDigits: Int,
    @SerialName("num_fractional_normal_digits")
    val numFractionalNormalDigits: Int,
    @SerialName("num_fractional_trailing_zero_digits")
    val numFractionalTrailingZeroDigits: Int,
    @SerialName("alt_unit_names")
    val altUnitNames: Map<Int, String>,
) {
    /**
     * Returns the primary symbol for the currency, if available.
     *
     * Currently this returns the unit at index 0 in [altUnitNames].
     * TODO: add support for additional alternative units.
     */
    val symbol: String? get() = altUnitNames[0]
}

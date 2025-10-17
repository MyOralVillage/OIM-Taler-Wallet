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

package net.taler.database.filter

import net.taler.database.data_models.Amount

/** Filters transactions by amount. */
sealed class AmountFilter {

    /** Matches a specific amount. */
    data class Exact(val amount: Amount) : AmountFilter()

    /**
     * Matches amounts within the inclusive range [min]..[max].
     * @throws IllegalArgumentException if [min] > [max]
     */
    data class Range(
        val min: Amount,
        val max: Amount
    ) : AmountFilter() {
        init {
            require(min <= max) { "Min must be less than or equal to max" }
        }
    }

    /**
     * Matches any of the given amounts.
     * @throws IllegalArgumentException if [amounts] is empty
     */
    data class OneOrMoreOf(val amounts: Set<Amount>) : AmountFilter() {
        init {
            require(amounts.isNotEmpty()) { "Must have at least one amount" }
        }
    }
}

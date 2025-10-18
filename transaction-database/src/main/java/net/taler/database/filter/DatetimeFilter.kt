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

import net.taler.database.data_models.FDtm

/** Filters transactions by datetime. */
sealed class DatetimeFilter {

    /** Matches a single datetime. */
    data class Exact(val datetime: FDtm) : DatetimeFilter()

    /**
     * Matches a datetime range (inclusive).
     * @throws IllegalArgumentException if [start] > [end]
     */
    data class Range(
        val start: FDtm,
        val end: FDtm
    ) : DatetimeFilter() {
        init {
            require(start <= end) { "Start must be less than or equal to end" }
        }
    }

}
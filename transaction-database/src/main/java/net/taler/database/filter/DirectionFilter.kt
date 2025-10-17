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

import net.taler.database.data_models.FilterableDirection

/** Filters transactions by direction (incoming or outgoing). */
sealed class DirectionFilter {

    /** Matches a specific direction. */
    data class Exact(val direction: FilterableDirection) : DirectionFilter()

    /** Matches both incoming and outgoing directions (no filtering). */
    data object Both : DirectionFilter()
}

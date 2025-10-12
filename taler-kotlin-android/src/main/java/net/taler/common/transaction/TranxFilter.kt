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

import android.os.Build
import androidx.annotation.RequiresApi
import net.taler.common.transaction.Amount
import net.taler.common.transaction.TranxPurp
import net.taler.common.utils.directionality.FilterableDirection
import net.taler.common.utils.time.FilterableLocalDateTime
import android.database.sqlite.*

/**
 * Represents a composite transaction filter with optional criteria.
 * Each filter type is nullable - null means no filtering for that criterion.
 *
 * ## Examples:
 *
 * ```kotlin
 * // Filter by a single datetime
 * val filter1 = TranxFilter(
 *     datetime = DatetimeFilter.Exact(FilterableLocalDateTime("2023-01-01T00:00"))
 * )
 *
 * // Filter by amount range and direction
 * val filter2 = TranxFilter(
 *     amount = AmountFilter.Range(amountA, amountB),
 *     direction = DirectionFilter.Exact(FilterableDirection.INCOMING)
 * )
 *
 * // Filter by three criteria: amount, direction, and transaction purpose
 * val filter3 = TranxFilter(
 *     amount = AmountFilter.Exact(someAmount),
 *     direction = DirectionFilter.Both,
 *     purpose = PurposeFilter.OneOrMoreOf(setOf(TranxPurp.RENT, TranxPurp.GROCERIES))
 * )
 * ```
 */
@RequiresApi(Build.VERSION_CODES.O)
data class TranxFilter (
    val datetime: DatetimeFilter? = null,
    val direction: DirectionFilter? = null,
    val amount: AmountFilter? = null,
    val purpose: PurposeFilter? = null
) {
    /** Returns true if no filters are active */
    fun isEmpty():
    Boolean = datetime == null && direction == null
    && amount == null && purpose == null
}

/** Filters transactions by datetime. */
@RequiresApi(Build.VERSION_CODES.O)
sealed class DatetimeFilter {

    /** Matches a single datetime. */
    data class Exact(val datetime: FilterableLocalDateTime) : DatetimeFilter()

    /**
     * Matches a datetime range (inclusive).
     * @throws IllegalArgumentException if [start] > [end]
     */
    data class Range(
        val start: FilterableLocalDateTime,
        val end: FilterableLocalDateTime
    ) : DatetimeFilter() {
        init {
            require(start <= end) { "Start must be less than or equal to end" }
        }
    }

}

/** Filters transactions by direction (incoming or outgoing). */
sealed class DirectionFilter {

    /** Matches a specific direction. */
    data class Exact(val direction: FilterableDirection) : DirectionFilter()

    /** Matches both incoming and outgoing directions (no filtering). */
    data object Both : DirectionFilter()
}

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

/** Filters transactions by purpose. */
sealed class PurposeFilter {

    /** Matches a specific transaction purpose. */
    data class Exact(val purpose: TranxPurp) : PurposeFilter()

    /**
     * Matches any of the given purposes.
     * @throws IllegalArgumentException if [purposes] is empty
     */
    data class OneOrMoreOf(val purposes: Set<TranxPurp>) : PurposeFilter() {
        init {
            require(purposes.isNotEmpty()) { "Must have at least one purpose" }
        }
    }
}
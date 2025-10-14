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

import android.os.Build
import androidx.annotation.RequiresApi
import net.taler.database.schema.Schema

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

/**
 * Takes a [TranxFilter] and returns it as a SQL query.
 * Default (when [TranxFilter.isEmpty] is true) returns
 * table organized by datetime values
 * @receiver [TranxFilter]
 * @return a SQL query
 */
@RequiresApi(Build.VERSION_CODES.O)
fun TranxFilter.toSQL(): String {

    // i don't feel like rewriting this whole thing
    val f = this

    // Default case: apply no filter, index on datetime
    if (f.isEmpty()) return "SELECT * FROM ${Schema.TABLE_NAME} ORDER BY ${Schema.EPOCH_MILLI_COL};\n"
    require(f.amount != null || f.datetime != null || f.purpose != null || f.direction != null)

    val whereClauses = mutableListOf<String>()

    // Build datetime query
    val dtmQuery: String? = when (f.datetime) {
        is DatetimeFilter.Exact ->
            "${Schema.EPOCH_MILLI_COL} = ${f.datetime.datetime.epochMillis()}"
        is DatetimeFilter.Range ->
            "${Schema.EPOCH_MILLI_COL} BETWEEN ${f.datetime.start.epochMillis()} " +
                    "AND ${f.datetime.end.epochMillis()}"
        null -> null
    }
    if (dtmQuery != null) whereClauses.add(dtmQuery)

    // Build amount query
    val amtQuery: String? = when (f.amount) {
        is AmountFilter.Exact -> {
            val storedAmount = f.amount.amount.value + f.amount.amount.fraction
            "${Schema.AMOUNT_COL} = $storedAmount"
        }

        is AmountFilter.OneOrMoreOf -> {
            val amounts = f.amount.amounts.joinToString(", ") { amount ->
                (amount.value + amount.fraction).toString()
            }
            "${Schema.AMOUNT_COL} IN ($amounts)"
        }

        is AmountFilter.Range -> {
            val minStored = f.amount.min.value + f.amount.min.fraction
            val maxStored = f.amount.max.value + f.amount.max.fraction
            "${Schema.AMOUNT_COL} BETWEEN $minStored AND $maxStored"
        }
        null -> null
    }
    if (amtQuery != null) whereClauses.add(amtQuery)

    // Build transaction purpose query
    val purpQuery: String? = when (f.purpose) {
        is PurposeFilter.Exact ->
            "${Schema.TRNX_PURPOSE_COL} = '${f.purpose.purpose.cmp}'"
        is PurposeFilter.OneOrMoreOf -> {
            val purps = f.purpose.purposes.joinToString(", ") { purp -> "'${purp.cmp}'" }
            "${Schema.TRNX_PURPOSE_COL} IN ($purps)"
        }
        null -> null
    }
    if (purpQuery != null) whereClauses.add(purpQuery)

    // Build direction query (note: must convert boolean -> int)
    // boolean TRUE  (incoming) == 1
    // boolean FALSE (outgoing) == 0
    // if they are set to "both", we will ignore them
    val dirQuery: String? = when (f.direction) {
        DirectionFilter.Both -> null
        is DirectionFilter.Exact -> {
            val value = when (f.direction.direction.getValue()) {
                true -> "1"
                false -> "0"
            }
            "${Schema.TRNX_INCOMING_COL} = $value"
        }
        null -> null
    }
    if (dirQuery != null) whereClauses.add(dirQuery)

    // Build up query
    val whereClause = if (whereClauses.isNotEmpty()) {
        "WHERE ${whereClauses.joinToString(" AND ")}"
    } else {"WHERE 1"}
    return "SELECT * FROM ${Schema.TABLE_NAME} $whereClause ORDER BY ${Schema.EPOCH_MILLI_COL};\n"
}

/**
 * Compares this [TranxFilter] with another [TranxFilter] for equality.
 *
 * @receiver The [TranxFilter] to compare.
 * @param that The other [TranxFilter] to compare against.
 * @return `true` if both filters produce the same SQL query string, `false` otherwise.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun TranxFilter.isEqual(that: TranxFilter) : Boolean {
    val thisQuery = this.toSQL()
    val thatQuery = that.toSQL()
    return thisQuery == thatQuery
}

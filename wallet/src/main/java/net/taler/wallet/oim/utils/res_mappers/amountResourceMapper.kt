package net.taler.wallet.oim.utils.res_mappers
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

import androidx.annotation.DrawableRes
import net.taler.common.R
import net.taler.database.data_models.*
import java.math.BigDecimal
import kotlin.collections.mutableListOf

/**
 * Extension function to consolidate a list of amounts into their optimal denomination representation.
 *
 * For example:
 * - 5x0.01 SLE → 1x0.05 SLE
 * - 10x0.01 SLE → 1x0.10 SLE
 * - 2x0.05 SLE → 1x0.10 SLE
 *
 * The consolidation process:
 * 1. Sum all amounts to get total value
 * 2. Call Amount.resourceMapper() which automatically returns the optimal denomination breakdown
 * 3. Convert each denomination back to Amount objects
 *
 * @return List of consolidated Amount objects, sorted from largest to smallest denomination
 */
internal fun List<Amount>.consolidate(): List<Amount> {
    if (this.isEmpty()) return emptyList()

    // Get currency from first amount
    val currency = this.first().spec?.name ?: this.first().currency

    // Sum all amounts
    val totalValue = this.fold(BigDecimal.ZERO) { acc, amount ->
        acc + BigDecimal(amount.amountStr)
    }

    // Create a single Amount representing the total
    val totalAmount = Amount.fromString(currency, totalValue.stripTrailingZeros().toPlainString())

    // Get the optimal denomination breakdown using resourceMapper
    val optimalResIds = totalAmount.resourceMapper()

    // Convert resource IDs back to Amount objects
    val consolidated = mutableListOf<Amount>()

    // We need to map each resId back to its denomination value
    // This requires knowing which currency we're working with
    when (currency) {
        "CHF" -> {
            optimalResIds.forEach { resId ->
                val denomValue = CHF_BILLS.find { it.second == resId }?.first
                if (denomValue != null) {
                    val francs = denomValue / 2
                    val half = denomValue % 2
                    val amountStr = if (half == 0) "$francs.00" else "$francs.50"
                    consolidated.add(Amount.fromString(currency, amountStr))
                }
            }
        }
        "XOF" -> {
            optimalResIds.forEach { resId ->
                val denomValue = XOF_BILLS.find { it.second == resId }?.first
                if (denomValue != null) {
                    consolidated.add(Amount.fromString(currency, denomValue.toString()))
                }
            }
        }
        "EUR", "SLE", "KUDOS", "KUD" -> {
            val denomList = when (currency) {
                "EUR" -> EUR_BILLS_CENTS
                else -> SLE_BILLS_CENTS // SLE, KUDOS, KUD all use same structure
            }

            optimalResIds.forEach { resId ->
                val denomValue = denomList.find { it.second == resId }?.first
                if (denomValue != null) {
                    val whole = denomValue / 100
                    val cents = denomValue % 100
                    val amountStr = "$whole.${cents.toString().padStart(2, '0')}"
                    consolidated.add(Amount.fromString(currency, amountStr))
                }
            }
        }
    }

    // Sort from largest to smallest denomination
    return consolidated.sortedByDescending { BigDecimal(it.amountStr) }
}

/**
 * Helper function to check if consolidation would reduce the number of notes/coins.
 * Useful for determining when to trigger consolidation animation.
 */
fun List<Amount>.canConsolidate(): Boolean {
    val consolidated = this.consolidate()
    return consolidated.size < this.size
}

/**
 * Extension to get the total value of a list of amounts
 */
internal fun List<Amount>.sumAmounts(): Amount {
    if (this.isEmpty()) return Amount.fromString("", "0")

    val currency = this.first().spec?.name ?: this.first().currency
    val totalValue = this.fold(BigDecimal.ZERO) { acc, amount ->
        acc + BigDecimal(amount.amountStr)
    }

    return Amount.fromString(currency, totalValue.stripTrailingZeros().toPlainString())
}

/** KUDOS mapped to Leones.
 * toCurrencyFrame returns a composable table
 * representation of a transaction. */
@DrawableRes
internal fun Amount.resourceMapper(): List<Int> {
    val result = mutableListOf<Int>()

    when (currency) {
        "CHF" -> {
            val totalHalfFrancs = value * 2 + (fraction * 2L / Amount.FRACTIONAL_BASE)
            result.addAll(mapToBills(totalHalfFrancs, CHF_BILLS))
        }
        "XOF" -> {
            if (fraction != 0)
                throw IllegalArgumentException("XOF does not support fractional amounts")
            result.addAll(mapToBills(value, XOF_BILLS))
        }
        "EUR" -> {
            val totalCents = value * 100 + (fraction * 100L / Amount.FRACTIONAL_BASE)
            result.addAll(mapToBills(totalCents, EUR_BILLS_CENTS))
        }

         "SLE", "KUDOS", "KUD", "TESTKUDOS" -> {
            val totalCents = value * 100 + (fraction * 100L / Amount.FRACTIONAL_BASE)
            result.addAll(mapToBills(totalCents, SLE_BILLS_CENTS))
        }
        else -> throw IllegalArgumentException("Unsupported currency: $currency")
    }

    return result
}

/** Map amount to bills/coins using provided denominations (descending order). */
@DrawableRes
private fun mapToBills(amount: Long, bills: List<Pair<Int, Int>>): List<Int> {
    val result = mutableListOf<Int>()
    var remaining = amount

    for ((billValue, resId) in bills) {
        while (remaining >= billValue) {
            remaining -= billValue
            result.add(resId)
        }
    }

    if (remaining > 0) {
        throw IllegalArgumentException(
            "Cannot represent remaining amount $remaining with available bills"
        )
    }

    return result
}

// === CHF denominations in half-franc units ===
val CHF_BILLS = listOf(
    2_000 to R.drawable.chf_hundred_thousand,
    400  to R.drawable.chf_twenty_thousand,
    200  to R.drawable.chf_ten_thousand,
    100  to R.drawable.chf_five_thousand,
    40   to R.drawable.chf_two_thousand,
    20   to R.drawable.chf_one_thousand,
    10   to R.drawable.chf_five_hundred,
    4     to R.drawable.chf_two_hundred,
    2     to R.drawable.chf_one_hundred,
    1       to R.drawable.chf_zero_point_five
)

// === XOF denominations (integer values only) ===
val XOF_BILLS = listOf(
    10_000 to R.drawable.xof_ten_thousand,
    5_000  to R.drawable.xof_five_thousand,
    2_000  to R.drawable.xof_two_thousand,
    1_000  to R.drawable.xof_one_thousand,
    500    to R.drawable.xof_five_hundred,
    200    to R.drawable.xof_two_hundred,
    100    to R.drawable.xof_one_hundred,
    25     to R.drawable.xof_twenty_five,
    10     to R.drawable.xof_ten,
    5      to R.drawable.xof_five,
    1      to R.drawable.xof_one
)

// === EUR denominations (in cents) ===
val EUR_BILLS_CENTS = listOf(
    20_000 to R.drawable.eur_two_hundred,
    10_000 to R.drawable.eur_one_hundred,
    5_000  to R.drawable.eur_fifty,
    2_000  to R.drawable.eur_twenty,
    1_000  to R.drawable.eur_ten,
    500    to R.drawable.eur_five,
    200    to R.drawable.eur_two,
    100    to R.drawable.eur_one,
    50     to R.drawable.eur_zero_point_five,
    20     to R.drawable.eur_zero_point_two,
    10     to R.drawable.eur_zero_point_one,
    5      to R.drawable.eur_zero_point_zero_five,
    2      to R.drawable.eur_zero_point_zero_two,
    1      to R.drawable.eur_zero_point_zero_one
)

// === SLE denominations (in cents, supports fractions) ===
val SLE_BILLS_CENTS = listOf(
    100_000 to R.drawable.sle_one_thousand,
    10_000  to R.drawable.sle_one_hundred,
    4_000   to R.drawable.sle_forty,
    2_000   to R.drawable.sle_twenty,
    1_000   to R.drawable.sle_ten,
    500     to R.drawable.sle_five,
    200     to R.drawable.sle_two,
    100     to R.drawable.sle_one,
    50      to R.drawable.sle_zero_point_five,
    25      to R.drawable.sle_zero_point_twenty_five,
    10      to R.drawable.sle_zero_point_one,
    5       to R.drawable.sle_zero_point_zero_five,
    1       to R.drawable.sle_zero_point_zero_one
)

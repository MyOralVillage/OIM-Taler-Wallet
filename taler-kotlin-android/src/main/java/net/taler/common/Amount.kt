/*
 * This file is part of GNU Taler
 * (C) 2020 Taler Systems S.A.
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

package net.taler.common

import android.os.Build
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Exception thrown when parsing or constructing an [Amount] fails due to invalid format,
 * illegal currency identifier, or fractional overflow.
 * @param msg Optional error message for debugging.
 * @param cause Underlying exception, if any.
 */
class AmountParserException(msg: String? = null, cause: Throwable? = null) :
    Exception(msg, cause)

/**
 * Exception thrown when arithmetic operations (addition, subtraction, multiplication)
 * cause an [Amount]'s integral part to exceed [Amount.Companion.MAX_VALUE].
 *
 * @param msg Optional error message for debugging.
 * @param cause Underlying exception, if any.
 */
class AmountOverflowException(msg: String? = null, cause: Throwable? = null) :
    Exception(msg, cause)

/**
 * Represents an exact monetary amount within the GNU Taler wallet system.
 *
 * Each [Amount] consists of:
 * - a currency identifier (ISO-4217 or local token-like name),
 * - an integral base value (in units of the base currency),
 * - a fractional component (in hundred-millionths of the base unit),
 * - an optional [CurrencySpecification] describing how to format or interpret it.
 *
 * Arithmetic and parsing operations are strict and overflow-protected.
 * Serialization is compact, using the format `"CURRENCY:value.fraction"`.
 */
@Serializable(with = KotlinXAmountSerializer::class)
data class Amount(
    /**
     * name of the currency using either a three-character ISO 4217 currency code,
     * or a regional currency identifier starting with a "*" followed by at most 10 characters.
     * ISO 4217 exponents in the name are not supported,
     * although the "fraction" is corresponds to an ISO 4217 exponent of 6.
     */
    val currency: String,

    /**
     * The integer part may be at most 2^52.
     * Note that "1" here would correspond to 1 EUR or 1 USD, depending on currency, not 1 cent.
     */
    val value: Long,

    /**
     * Unsigned 32 bit fractional value to be added to value representing
     * an additional currency fraction, in units of one hundred millionth (1e-8)
     * of the base currency value.  For example, a fraction
     * of 50_000_000 would correspond to 50 cents.
     */
    val fraction: Int,

    /**
     * Currency specification for amount
     */
    val spec: CurrencySpecification? = null,
) : Comparable<Amount> {

    companion object {

        /** Default number of decimal digits accepted for
         * manual input if no [spec] is provided. */
        const val DEFAULT_INPUT_DECIMALS = 2

        /** Internal fractional scaling base (1e8). */
        private const val FRACTIONAL_BASE: Int = 100_000_000 // 1e8

        /** Currency validation regex: allows letters, digits,
         * underscores, and hyphens. */
        private val REGEX_CURRENCY = Regex("""^[-_*A-Za-z0-9]{1,12}$""")

        /** Maximum allowable integral amount (2^52). */
        val MAX_VALUE: Long = 2.0.pow(52).toLong()

        /** Maximum supported length of the fractional part (8 digits). */
        private const val MAX_FRACTION_LENGTH = 8

        /** Maximum valid fractional value (99_999_999). */
        const val MAX_FRACTION: Int = 99_999_999

        /**
         * Constructs a zero-valued [Amount] for a given [currency].
         * @throws AmountParserException if [currency] is invalid.
         */
        fun zero(currency: String): Amount {
            return Amount(checkCurrency(currency), 0, 0)
        }

        /**
         * Parses a serialized [Amount] string of the form `"CURRENCY:VALUE.FRACTION"`.
         * @throws AmountParserException if parsing fails or format is invalid.
         */
        fun fromJSONString(str: String): Amount {
            val split = str.split(":")
            if (split.size != 2) throw AmountParserException("Invalid Amount Format")
            return fromString(split[0], split[1])
        }

        /**
         * Parses an amount given separate [currency] and numeric string [str].
         * The string may include an optional fractional component.
         * Example: `fromString("USD", "3.1415")`.
         * @param currency the specified currency
         * @param str a string representation of the value
         * @throws AmountParserException if fraction is invalid
         */
        fun fromString(currency: String, str: String): Amount {
            // value
            val valueSplit = str.split(".")
            val value = checkValue(valueSplit[0].toLongOrNull())
            // fraction
            val fraction: Int = if (valueSplit.size > 1) {
                val fractionStr = valueSplit[1]
                if (fractionStr.length > MAX_FRACTION_LENGTH)
                    throw AmountParserException("Fraction $fractionStr too long")
                checkFraction(fractionStr.getFraction())
            } else 0
            return Amount(checkCurrency(currency), value, fraction)
        }

        /**
         * Checks if a numeric string can represent a valid amount.
         * @param str the amount to check
         * @return true if valid, false otherwise
         */
        fun isValidAmountStr(str: String): Boolean {
            if (str.count { it == '.' } > 1) return false
            val split = str.split(".")
            try {
                checkValue(split[0].toLongOrNull())
            } catch (e: AmountParserException) {
                return false
            }
            // also check fraction, if it exists
            if (split.size > 1) {
                val fractionStr = split[1]
                if (fractionStr.length > MAX_FRACTION_LENGTH) return false
                val fraction = fractionStr.getFraction() ?: return false
                return fraction <= MAX_FRACTION
            }
            return true
        }

        /** Converts a fractional string (e.g. `"50"`) to an
         *  integer in base [FRACTIONAL_BASE]. */
        private fun String.getFraction(): Int? {
            return "0.$this".toDoubleOrNull()
                ?.times(FRACTIONAL_BASE)
                ?.roundToInt()
        }

        /** Minimum representable nonzero amount (1e-8 of a unit). */
        fun min(currency: String): Amount = Amount(currency, 0, 1)

        /** Maximum representable amount before overflow. */
        fun max(currency: String): Amount = Amount(
            currency, MAX_VALUE, MAX_FRACTION
        )

        /** Validates the currency string syntax. */
        internal fun checkCurrency(currency: String): String {
            if (!REGEX_CURRENCY.matches(currency))
                throw AmountParserException("Invalid currency: $currency")
            return currency
        }

        /** Ensures the value is within valid bounds. */
        internal fun checkValue(value: Long?): Long {
            if (value == null || value > MAX_VALUE)
                throw AmountParserException("Value $value greater than $MAX_VALUE")
            return value
        }

        /** Ensures the fractional component is valid and not overflowing. */
        internal fun checkFraction(fraction: Int?): Int {
            if (fraction == null || fraction > MAX_FRACTION)
                throw AmountParserException(
                    "Fraction $fraction greater than $MAX_FRACTION"
                )
            return fraction
        }

    }

    /** normalizes a currency amount
     *
     * ex: `"3.50"` or `"2"`
     * @return string representation of normalized amount  */
    val amountStr: String
        get() = if (fraction == 0) "$value" else {
            var f = fraction
            var fractionStr = ""
            while (f > 0) {
                fractionStr += f / (FRACTIONAL_BASE / 10)
                f = (f * 10) % FRACTIONAL_BASE
            }
            "$value.$fractionStr"
        }

    /**
     * Adds another [Amount] of the same currency.
     * @throws IllegalStateException if other currency does not match this currency.
     * @throws AmountOverflowException if the resulting value exceeds [MAX_VALUE].
     */
    operator fun plus(other: Amount): Amount {

        // assert currencies match
        check(currency == other.currency) { "Can only subtract from same currency" }

        // adds the integer values (value) and the integer components
        // of the addition of the fractional components (fraction),
        // rounded to the nearest hundred millionth
        val resultValue =
            value + other.value
            + floor(
                (fraction + other.fraction).toDouble() / FRACTIONAL_BASE
            ).toLong()

        // assert result is not g.t. maximum allowed value
        if (resultValue > MAX_VALUE)
            throw AmountOverflowException()

        // store the resulting fraction component
        val resultFraction = (fraction + other.fraction) % FRACTIONAL_BASE

        // return
        return Amount(currency, resultValue, resultFraction)
    }

    /**
     * Multiplies this amount by an integer factor.
     *
     * @param factor integer multiplier.
     * @return new [Amount] with scaled value.
     */
    operator fun times(factor: Int): Amount {
        // TODO consider replacing with a faster implementation
        if (factor == 0) return zero(currency)
        var result = this
        for (i in 1 until factor) result += this
        return result
    }

    /**
     * Returns a copy of this amount with a different currency code.
     *
     * @param currency the new currency.
     * @return modified [Amount] with updated currency.
     * @throws AmountParserException if currency is invalid.
     */
    fun withCurrency(currency: String): Amount {
        return Amount(checkCurrency(currency), this.value, this.fraction)
    }

    /**
     * Returns a copy of this amount with an updated [CurrencySpecification].
     *
     * @param spec new specification (nullable).
     * @return modified [Amount].
     */
    fun withSpec(spec: CurrencySpecification?) = copy(spec = spec)

    /**
     * Subtracts another amount from this one.
     * @param other the amount to subtract.
     * @return the resulting difference.
     * @throws IllegalStateException if other currency does not match this currency.
     * @throws AmountOverflowException if subtraction underflows.
     */
    operator fun minus(other: Amount): Amount {
        check(currency == other.currency) { "Can only subtract from same currency" }
        var resultValue = value
        var resultFraction = fraction
        if (resultFraction < other.fraction) {
            if (resultValue < 1L)
                throw AmountOverflowException()
            resultValue--
            resultFraction += FRACTIONAL_BASE
        }
        check(resultFraction >= other.fraction)
        resultFraction -= other.fraction
        if (resultValue < other.value)
            throw AmountOverflowException()
        resultValue -= other.value
        return Amount(currency, resultValue, resultFraction)
    }

    /** @return true if both value and fraction are zero. */
    fun isZero(): Boolean {
        return value == 0L && fraction == 0
    }

    /** Serializes this [Amount] as `"CUR:value.fraction"` string. */
    fun toJSONString(): String {
        return "$currency:$amountStr"
    }

    override fun toString() = toString(
        showSymbol = true,
        negative = false,
    )

    /**
     * Adds a digit to the current numeric amount (for user input).
     * @param c digit character to append.
     * @return new [Amount] or null if invalid.
     */
    fun addInputDigit(c: Char): Amount? = c.digitToIntOrNull()?.let { digit ->
        try {
            val value = amountStr.toBigDecimal()
            val decimals = spec?.numFractionalInputDigits ?: DEFAULT_INPUT_DECIMALS
            fromString(
                currency,
                // some real math!
                ((value * 10.0.toBigDecimal().setScale(decimals))
                        + (digit.toBigDecimal().setScale(decimals)
                        / 10.0.toBigDecimal().pow(decimals))).toString()
            )
        } catch (e: AmountParserException) { null }
    }

    /**
     * Removes the least significant input digit.
     * @return new [Amount] or null if invalid.
     */
    fun removeInputDigit(): Amount? = try {
        val decimals = spec?.numFractionalInputDigits ?: DEFAULT_INPUT_DECIMALS
        val value = amountStr.toBigDecimal().setScale(decimals + 1, RoundingMode.FLOOR)
        fromString(
            currency,
            // more math!
            (value / "10.0".toBigDecimal()).setScale(decimals, RoundingMode.FLOOR).toString()
        )
    } catch (e: AmountParserException) { null }


    /**
     * Converts this [Amount] to a formatted display string using locale-aware
     * number and currency formatting.
     * @param showSymbol if true, includes the currency symbol or code.
     * @param negative if true, prefixes the amount with a minus sign.
     * @param symbols optional decimal formatting symbols.
     * @return formatted string representation of the amount.
     */
    fun toString(
        showSymbol: Boolean = true,
        negative: Boolean = false,
        symbols: DecimalFormatSymbols = DecimalFormat().decimalFormatSymbols,
    ): String {
        // We clone the object to safely/cleanly modify it
        val s = symbols.clone() as DecimalFormatSymbols
        val amount = (if (negative) "-$amountStr" else amountStr).toBigDecimal()

        // No currency spec, so we render normally
        if (spec == null) {
            val format = NumberFormat.getInstance()
            format.maximumFractionDigits = MAX_FRACTION_LENGTH
            format.minimumFractionDigits = 2
            if (Build.VERSION.SDK_INT >= 34) {
                s.groupingSeparator = s.monetaryGroupingSeparator
            }
            s.decimalSeparator = s.monetaryDecimalSeparator
            (format as DecimalFormat).decimalFormatSymbols = s

            val fmt = format.format(amount)
            return if (showSymbol) "$fmt $currency" else fmt
        }

        // There is currency spec, so we can do things right
        val format = NumberFormat.getCurrencyInstance()
        format.minimumFractionDigits = spec.numFractionalTrailingZeroDigits
        format.maximumFractionDigits = MAX_FRACTION_LENGTH
        s.currencySymbol = spec.symbol ?: ""
        (format as DecimalFormat).decimalFormatSymbols = s

        val fmt = format.format(amount)
        return if (showSymbol) {
            // If no symbol, then we use the currency string
            (if (spec.symbol != null) fmt else "$fmt $currency").trim()
        } else {
            // We should do better than manually removing the symbol here
            fmt.replace(s.currencySymbol, "").trim()
        }
    }


    /**
     * Compares this amount with another.
     * @param other the amount to compare against.
     * @return -1 if less than, 0 if equal, 1 if greater.
     * @throws IllegalStateException if currencies differ.
     */
    override fun compareTo(other: Amount): Int {
        check(currency == other.currency) {
            "Can only compare amounts with the same currency"
        }
        when {
            value == other.value -> {
                if (fraction < other.fraction) return -1
                if (fraction > other.fraction) return 1
                return 0
            }
            value < other.value -> return -1
            else -> return 1
        }
    }
}

/**
 * Custom serializer for [Amount] that encodes it
 * as a single JSON string (`"CUR:value.fraction"`).
 */
@OptIn(ExperimentalSerializationApi::class)
@Suppress("EXPERIMENTAL_API_USAGE")
@Serializer(forClass = Amount::class)
internal object KotlinXAmountSerializer : KSerializer<Amount> {

    /**
     * Serializes an [Amount] into its compact JSON string representation.
     * @param encoder the [Encoder] to write into.
     * @param value the [Amount] to encode.
     */
    override fun serialize(encoder: Encoder, value: Amount) {
        encoder.encodeString(value.toJSONString())
    }

    /**
     * Deserializes an [Amount] from its JSON string representation.
     * @param decoder the [Decoder] to read from.
     * @return decoded [Amount] object.
     * @throws AmountParserException if the string format is invalid.
     */
    override fun deserialize(decoder: Decoder): Amount {
        return Amount.fromJSONString(decoder.decodeString())
    }
}

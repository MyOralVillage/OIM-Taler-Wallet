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

package net.taler.database.data_access

import android.database.Cursor
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import net.taler.database.data_models.FilterableDirection
import net.taler.database.data_models.FilterableLocalDateTime
import net.taler.database.data_models.Amount
import net.taler.database.data_models.CurrencySpecification
import net.taler.database.data_models.Tranx
import net.taler.database.data_models.TranxPurp
import net.taler.database.data_models.tranxPurpLookup
import net.taler.database.schema.Schema
import java.time.Instant
import java.time.LocalDateTime
import kotlin.math.floor

/**
 * Parses and deserializes the currency specification JSON from [Schema.CURRENCY_SPEC_COL].
 *
 * If the specification column is `NULL`, this internal function returns `null`.
 * Otherwise, the JSON string is deserialized into a [CurrencySpecification] object.
 *
 * @receiver [Cursor] positioned at a valid row.
 * @return a [CurrencySpecification] instance, or `null` if no specification data exists.
 *
 * @throws IllegalArgumentException if [Schema.CURRENCY_SPEC_COL] does not exist.
 * @throws SerializationException if the JSON is malformed or cannot be decoded.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun Cursor.getSpecs(): CurrencySpecification? {
    // if no spec existed, will be null; else deserialize data
    return when (val json = this.getString(getColumnIndexOrThrow(Schema.CURRENCY_SPEC_COL))) {
        null -> null
        else -> Json.decodeFromString<CurrencySpecification>(json)
    }
}

/**
 * Parses an [Amount] value from the current [Cursor] row.
 *
 * The amount is reconstructed from [Schema.AMOUNT_COL] using the standard divisor
 * defined in [Schema.CURRENCY_DIVISOR]. The result includes both the integral and
 * fractional components, along with currency metadata.
 *
 * @receiver [Cursor] positioned at a valid row.
 * @return an [Amount] instance containing the parsed currency, value, and optional specification.
 *
 * @throws IllegalArgumentException if any of the required columns
 * ([Schema.CURRENCY_COL], [Schema.AMOUNT_COL], or [Schema.CURRENCY_SPEC_COL]) are missing.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun Cursor.getAmt(): Amount {
    val storedAmount = this.getLong(this.getColumnIndexOrThrow(Schema.AMOUNT_COL))
    return Amount(
        this.getString(this.getColumnIndexOrThrow(Schema.CURRENCY_COL)),
        floor(storedAmount.toDouble() / Schema.CURRENCY_DIVISOR).toLong(),
        (storedAmount % Schema.CURRENCY_DIVISOR.toLong()).toInt(),
        this.getSpecs()
    )
}


/**
 * Parses the transaction purpose from the current [Cursor] row.
 *
 * This internal function retrieves the purpose string stored in [Schema.TRNX_PURPOSE_COL]
 * and looks it up in the global [tranxPurpLookup] table to obtain the corresponding
 * [TranxPurp] enumeration value.
 *
 * @receiver [Cursor] positioned at a valid transaction row.
 * @return the [TranxPurp] value corresponding to the stored purpose string or null if none exists.
 *
 * @throws IllegalArgumentException if [Schema.TRNX_PURPOSE_COL] does not exist.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun Cursor.getPurp(): TranxPurp? =
    try {
        tranxPurpLookup[this.getString(this.getColumnIndexOrThrow(Schema.TRNX_PURPOSE_COL))]
    } catch (_: Exception) { null }
/**
 * Parses the transaction direction from the current [Cursor] row.
 *
 * The value of [Schema.TRNX_INCOMING_COL] determines whether the transaction
 * is marked as [FilterableDirection.INCOMING] or [FilterableDirection.OUTGOING].
 * A nonzero value represents an incoming transaction, while zero represents outgoing.
 *
 * @receiver [Cursor] positioned at a valid transaction row.
 * @return a [FilterableDirection] value indicating whether the transaction is incoming or outgoing.
 *
 * @throws IllegalArgumentException if [Schema.TRNX_INCOMING_COL] does not exist.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun Cursor.getDir(): FilterableDirection {
    return if (this.getInt(this.getColumnIndexOrThrow(Schema.TRNX_INCOMING_COL)) != 0) {
        FilterableDirection.INCOMING
    } else {
        FilterableDirection.OUTGOING
    }
}

/**
 * @receiver [Cursor] positioned at a valid transaction row.
 * @return a [FilterableLocalDateTime] value initialized to [Schema.DEFAULT_TIME_ZONE].
 * @throws IllegalArgumentException if [Schema.EPOCH_MILLI_COL] does not exist.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun Cursor.getDtm(): FilterableLocalDateTime {
    return FilterableLocalDateTime(
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this.getLong(this.getColumnIndexOrThrow(Schema.EPOCH_MILLI_COL))),
            Schema.DEFAULT_TIME_ZONE
        ),
        Schema.DEFAULT_TIME_ZONE
    )
}

/**
 * Converts the current row of the [Cursor] into a [Tranx] object.
 *
 * @receiver Cursor positioned at the row to convert.
 * @return a [Tranx] object representing the current row.
 * @throws IllegalStateException if the required columns are missing or invalid.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun Cursor.toTranx(): Tranx =
    Tranx(this.getTID(), this.getDtm(), this.getPurp(), this.getAmt(), this.getDir())

/**
 * Converts all rows in the [Cursor] into a list of [Tranx] objects.
 *
 * @receiver Cursor containing the query results.
 * @return a [List] of [Tranx] objects for all rows in the cursor.
 *         Returns an empty list if the cursor has no rows.
 */
@RequiresApi(Build.VERSION_CODES.O)
internal fun Cursor.toTranxList(): List<Tranx> {
    val result = mutableListOf<Tranx>()
    if (this.moveToFirst()) { do {result += this.toTranx()} while (this.moveToNext()) }
    return result
}
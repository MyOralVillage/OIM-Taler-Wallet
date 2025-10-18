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

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.taler.database.data_models.FilterableLocalDateTime
import net.taler.database.data_models.Amount
import net.taler.database.data_models.Tranx
import net.taler.database.schema.Schema


/**
 * Retrieves the minimum and maximum datetimes and amounts from the transaction table.
 *
 * This function queries the database to find:
 * 1. The earliest and latest transaction timestamps.
 * 2. The smallest and largest transaction amounts, along with their associated
 *    currency and currency specification.
 *
 * Each query returns a single row. If the table is empty, the function returns `null`.
 *
 * @param db The [SQLiteDatabase] instance containing the transaction table.
 * @return A [Pair] containing:
 *   - First: a [Pair] of [FilterableLocalDateTime] representing (minDatetime, maxDatetime)
 *   - Second: a [Pair] of [Amount] representing (minAmount, maxAmount)
 *   Returns `null` if the table is empty.
 *
 * @throws IllegalArgumentException if any required columns are missing.
 * @throws SQLiteException if a database access error occurs.
 */
internal fun getExtrema(db: SQLiteDatabase) :
        Pair<Pair<FilterableLocalDateTime, FilterableLocalDateTime>, Pair<Amount, Amount>>? {

    // initialize variables
    var _minDtm : FilterableLocalDateTime
    var _maxDtm : FilterableLocalDateTime
    var _minAmt : Amount
    var _maxAmt : Amount

    // query minimum datetime
    val minDatetimeQuery =
        """
        SELECT ${Schema.EPOCH_MILLI_COL}, ${Schema.AMOUNT_COL}, 
        ${Schema.CURRENCY_COL}, ${Schema.CURRENCY_SPEC_COL}, ${Schema.TRNX_PURPOSE_COL}, ${Schema.TRNX_INCOMING_COL}
        FROM ${Schema.TABLE_NAME}
        WHERE ${Schema.EPOCH_MILLI_COL} = (SELECT MIN(${Schema.EPOCH_MILLI_COL}) FROM ${Schema.TABLE_NAME})
        LIMIT 1
        """.trimIndent()
    db.rawQuery(minDatetimeQuery, null).use{
            cursor ->
        if (cursor.moveToFirst()) {_minDtm = cursor.getDtm() }
        else return null
    }

    // query maximum datetime
    val maxDatetimeQuery =
        """
        SELECT ${Schema.EPOCH_MILLI_COL}, ${Schema.AMOUNT_COL}, 
        ${Schema.CURRENCY_COL}, ${Schema.CURRENCY_SPEC_COL}, ${Schema.TRNX_PURPOSE_COL}, ${Schema.TRNX_INCOMING_COL}
        FROM ${Schema.TABLE_NAME}
        WHERE ${Schema.EPOCH_MILLI_COL} = (SELECT MAX(${Schema.EPOCH_MILLI_COL}) FROM ${Schema.TABLE_NAME})
        LIMIT 1
        """.trimIndent()
    db.rawQuery(maxDatetimeQuery, null).use{
            cursor ->
        if (cursor.moveToFirst()) {_maxDtm = cursor.getDtm() }
        else return null
    }

    // query minimum amount (with currency/spec)
    val minAmountQuery =
        """
        SELECT ${Schema.AMOUNT_COL}, ${Schema.CURRENCY_COL}, ${Schema.CURRENCY_SPEC_COL} 
        FROM ${Schema.TABLE_NAME} 
        WHERE ${Schema.AMOUNT_COL} = (SELECT MIN(${Schema.AMOUNT_COL}) FROM ${Schema.TABLE_NAME})
        LIMIT 1;
        """.trimIndent()
    db.rawQuery(minAmountQuery, null).use { cursor ->
        if (cursor.moveToFirst()) { _minAmt = cursor.getAmt() }
        else return null
    }

    // query maximum amount (with currency/spec)
    val maxAmountQuery =
        """
        SELECT ${Schema.AMOUNT_COL}, ${Schema.CURRENCY_COL}, ${Schema.CURRENCY_SPEC_COL} 
        FROM ${Schema.TABLE_NAME} 
        WHERE ${Schema.AMOUNT_COL} = (SELECT MAX(${Schema.AMOUNT_COL}) FROM ${Schema.TABLE_NAME})
        LIMIT 1;
        """.trimIndent()
    db.rawQuery(maxAmountQuery, null).use { cursor ->
        if (cursor.moveToFirst()) { _maxAmt = cursor.getAmt() }
        else return null
    }
    return Pair(Pair(_minDtm, _maxDtm), Pair(_minAmt, _maxAmt))
}

/**
 * Executes the given SQL query on the provided [SQLiteDatabase]
 * and returns a list of [Tranx] objects matching the query.
 *
 * @param db The [SQLiteDatabase] to query.
 * @param sql The SQL query string to execute.
 * @param selectionArgs Optional arguments to bind to the query placeholders (`?`).
 * @return a [List] of [Tranx] objects matching the query.
 *         Returns an empty list if no rows match.
 */
internal fun queryTranx(
    db: SQLiteDatabase,
    sql: String,
    selectionArgs: Array<String>? = null
): List<Tranx> {
    val cursor = db.rawQuery(sql, selectionArgs)
    return cursor.use { it.toTranxList() }
}

/** inserts a transaction into the database
 * @param trxn a transaction to insert
 * @return the index of the inserted value
 * @throw SQLiteException if insertion failed */
internal fun addTranx(db: SQLiteDatabase, trxn: Tranx) : Long {
    val values = ContentValues().apply {
        put(Schema.EPOCH_MILLI_COL, trxn.datetime.epochMillis())
        put(Schema.CURRENCY_COL, trxn.amount.currency)
        put(Schema.TRNX_PURPOSE_COL, trxn.purpose?.cmp)
        put(Schema.TRNX_INCOMING_COL,  if (trxn.direction.getValue()) 1 else 0)

        // if null, infer no spec exists; else encode spec and put in db
        put(
            Schema.CURRENCY_SPEC_COL,
            if (trxn.amount.spec!=null) Json.encodeToString(trxn.amount.spec) else null
        )

        // amount is stored as value + fraction (raw combined value)
        put(Schema.AMOUNT_COL, trxn.amount.value + trxn.amount.fraction.toLong())
    }
    val result = db.insert(Schema.TABLE_NAME, null, values)
    if (result == -1L) {
        throw SQLiteException("Failed to insert transaction")
    }
    return result
}

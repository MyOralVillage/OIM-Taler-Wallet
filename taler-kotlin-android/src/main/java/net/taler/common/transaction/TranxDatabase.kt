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

package net.taler.common.transaction

import TranxFilter
import android.content.ContentValues
import android.content.Context
import android.database.*
import android.database.sqlite.*
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.taler.common.utils.directionality.FilterableDirection
import net.taler.common.utils.time.FilterableLocalDateTime
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.floor
import kotlinx.serialization.SerializationException

/**
 * Represents a financial transaction with a timestamp, purpose, and amount.
 * @property datetime The UTC timestamp of the transaction.
 * @property purpose The purpose of the transaction, represented by a [TranxPurp].
 * @property amount The amount involved in the transaction as an [Amount].
 * @property direction Indicates outgoing or ingoing transactions
 * @constructor Creates a [Tranx] with the given timestamp, purpose, and amount.
 * @param datetime The time the transaction occurred.
 * @param purp The purpose of the transaction.
 * @param amnt The transaction amount.
 * @param dir direction of the transaction relative to the wallet
 */
@RequiresApi(Build.VERSION_CODES.O)
data class Tranx (
    val dateTimeUTC: FilterableLocalDateTime,
    val purpose: TranxPurp,
    val amount: Amount,
    val direction: FilterableDirection
)

@RequiresApi(Build.VERSION_CODES.O)
/** Holds constants for the transaction history database schema */
internal object Schema {
    const val VERSION = 1
    const val DATABASE_NAME = "transaction_history.db"
    const val TABLE_NAME = "TRANSACTION_HISTORY"
    const val INDEX_COL = "id"
    const val EPOCH_MILLI_COL = "epoch_milliseconds"
    const val AMOUNT_COL = "amount"
    const val CURRENCY_COL = "currency"
    const val CURRENCY_SPEC_COL = "currency_specifications" // if null, store as empty string
    const val TRNX_PURPOSE_COL = "tranx_purpose"
    const val TRNX_INCOMING_COL = "incoming" // incoming == true (1 in sql)
    val CURSOR_FACTORY = null
    val DEFAULT_TIME_ZONE = ZoneId.of("UTC")
    const val DATETIME_INDEX = "idx_dtm"
    const val AMOUNT_INDEX = "idx_amt"
    const val PURPOSE_INDEX = "idx_purp"

    /** for an indexed value amount_i:
     * Amount.value = floor(amount_i / CURRENCY_DIVISOR).toLong()
     * Amount.fraction = (amount_i % CURRENCY_DIVISOR).toInt() */
    const val CURRENCY_DIVISOR = 1e8
}


/**
 * Parses and deserializes the currency specification JSON from [Schema.CURRENCY_SPEC_COL].
 *
 * If the specification column is `NULL`, this function returns `null`.
 * Otherwise, the JSON string is deserialized into a [CurrencySpecification] object.
 *
 * @receiver [Cursor] positioned at a valid row.
 * @return a [CurrencySpecification] instance, or `null` if no specification data exists.
 *
 * @throws IllegalArgumentException if [Schema.CURRENCY_SPEC_COL] does not exist.
 * @throws SerializationException if the JSON is malformed or cannot be decoded.
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Cursor.getSpecs(): CurrencySpecification? {
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
fun Cursor.getAmt(): Amount {
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
 * This function retrieves the purpose string stored in [Schema.TRNX_PURPOSE_COL]
 * and looks it up in the global [tranxPurpLookup] table to obtain the corresponding
 * [TranxPurp] enumeration value.
 *
 * @receiver [Cursor] positioned at a valid transaction row.
 * @return the [TranxPurp] value corresponding to the stored purpose string.
 *
 * @throws IllegalArgumentException if [Schema.TRNX_PURPOSE_COL] does not exist.
 * @throws NullPointerException if the retrieved purpose string is not present in [tranxPurpLookup].
 */
@RequiresApi(Build.VERSION_CODES.O)
fun Cursor.getPurp(): TranxPurp =
    tranxPurpLookup[this.getString(this.getColumnIndexOrThrow(Schema.TRNX_PURPOSE_COL))]!!

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
fun Cursor.getDir(): FilterableDirection {
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
fun Cursor.getDtm(): FilterableLocalDateTime {
    return FilterableLocalDateTime(
        LocalDateTime.ofInstant(
            Instant.ofEpochMilli(this.getLong(this.getColumnIndexOrThrow(Schema.EPOCH_MILLI_COL))),
            Schema.DEFAULT_TIME_ZONE
        ),
        Schema.DEFAULT_TIME_ZONE
    )
}

/**
 * SQLite database helper for managing transaction history storage.
 *
 * This class provides persistent storage for financial transactions with support for both
 * file-based and in-memory database configurations. The database schema includes a main
 * transaction table with indexed columns for efficient querying by datetime, amount, and
 * transaction purpose.
 *
 * ## Database Schema
 * The transaction table includes the following columns:
 * - **id**: Auto-incrementing primary key
 * - **epoch_milliseconds**: Transaction timestamp (UTC)
 * - **amount**: Combined value and fraction as a single integer
 * - **currency**: Currency code (e.g., "USD", "EUR")
 * - **currency_specifications**: Optional JSON-encoded currency metadata
 * - **tranx_purpose**: Transaction purpose identifier
 * - **incoming**: Direction flag (1 for incoming, 0 for outgoing)
 *
 * ## Indexes
 * Three custom indexes are created to optimize query performance:
 * - DateTime index on `epoch_milliseconds`
 * - Amount index on `amount`
 * - Purpose index on `tranx_purpose`
 *
 * ## Storage Modes
 * - **File-based**: When a non-null [Context] is provided, the database is persisted to disk
 *   using [Schema.DATABASE_NAME].
 * - **In-memory**: When `null` is provided as the context, an ephemeral in-memory database
 *   is created that exists only for the lifetime of the application session.
 *
 * @param context The Android context used to create the database file, or `null` to create
 *                an in-memory database. In-memory databases are useful for testing or
 *                temporary storage scenarios.
 *
 * @constructor Creates a new [TransactionDatabase] helper instance.
 *
 * @see SQLiteOpenHelper
 * @see Schema
 *
 * @since API 26 (Android 8.0)
 */
@RequiresApi(Build.VERSION_CODES.O)
class TransactionDatabase(context: Context?)
    : SQLiteOpenHelper(
    context,
    if (context == null) null else Schema.DATABASE_NAME,
    Schema.CURSOR_FACTORY,
    Schema.VERSION
) {

    /**
     * Called when the database is created for the first time.
     *
     * This method initializes the transaction history table and creates three custom indexes
     * to optimize query performance for common access patterns (datetime, amount, and purpose).
     *
     * ## Created Schema
     * - **Main table**: [Schema.TABLE_NAME] with all transaction columns
     * - **DateTime index**: [Schema.DATETIME_INDEX] on [Schema.EPOCH_MILLI_COL]
     * - **Amount index**: [Schema.AMOUNT_INDEX] on [Schema.AMOUNT_COL]
     * - **Purpose index**: [Schema.PURPOSE_INDEX] on [Schema.TRNX_PURPOSE_COL]
     *
     * @param db The database instance being created. Must not be null.
     *
     * @throws SQLException if any SQL statement fails to execute.
     */
    override fun onCreate(db: SQLiteDatabase) {

        // main table
        val createTableQuery =
            """
            CREATE TABLE ${Schema.TABLE_NAME} (
                ${Schema.INDEX_COL} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Schema.EPOCH_MILLI_COL} INTEGER NOT NULL,
                ${Schema.AMOUNT_COL} INTEGER NOT NULL,
                ${Schema.CURRENCY_COL} TEXT NOT NULL,
                ${Schema.CURRENCY_SPEC_COL} TEXT, 
                ${Schema.TRNX_PURPOSE_COL} TEXT NOT NULL,
                ${Schema.TRNX_INCOMING_COL} INTEGER NOT NULL) 
            """.trimIndent()

        // create custom index based on amount (chained; first checks int, then frac)
        val createAmountIndexQuery =
            """
            CREATE INDEX ${Schema.AMOUNT_INDEX}
            ON ${Schema.TABLE_NAME}(${Schema.AMOUNT_COL})
            """.trimIndent()

        // create custom index on datetime (epoch milliseconds)
        val createTimeIndexQuery =
            """
             CREATE INDEX ${Schema.DATETIME_INDEX}
             ON ${Schema.TABLE_NAME}(${Schema.EPOCH_MILLI_COL})
            """.trimIndent()

        // create custom index on transaction purpose
        val createPurposeIndexQuery =
            """
             CREATE INDEX ${Schema.PURPOSE_INDEX}
             ON ${Schema.TABLE_NAME}(${Schema.TRNX_PURPOSE_COL})
            """.trimIndent()

        db.execSQL(createTableQuery)
        db.execSQL(createAmountIndexQuery)
        db.execSQL(createTimeIndexQuery)
        db.execSQL(createPurposeIndexQuery)
    }

    /**
     * Performs a destructive upgrade by dropping the existing transaction
     * table and recreating it with a new schema.
     *
     * ## Warning
     * **All existing transaction data will be permanently deleted during the upgrade process.**
     * Consider implementing a data migration strategy before deploying schema changes to
     * production environments.
     *
     * @param db The database instance being upgraded. Must not be null.
     * @param oldVersion The current schema version of the database before upgrade.
     * @param newVersion The target schema version after upgrade (typically [Schema.VERSION]).
     *
     * @throws SQLException if the drop or create operations fail.
     *
     * @see onCreate
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS ${Schema.TABLE_NAME}")
        onCreate(db)
    }
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
    Tranx(this.getDtm(), this.getPurp(), this.getAmt(), this.getDir())

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
@RequiresApi(Build.VERSION_CODES.O)
fun queryTranx(
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
@RequiresApi(Build.VERSION_CODES.O)
fun addTranx(db: SQLiteDatabase, trxn: Tranx) : Long {
    val values = ContentValues().apply {
        put(Schema.EPOCH_MILLI_COL, trxn.dateTimeUTC.epochMillis())
        put(Schema.CURRENCY_COL, trxn.amount.currency)
        put(Schema.TRNX_PURPOSE_COL, trxn.purpose.cmp)
        put(Schema.TRNX_INCOMING_COL,  if (trxn.direction.getValue()) 1 else 0)

        // if null, infer no spec exists; else encode spec and put in db
        put(Schema.CURRENCY_SPEC_COL,
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
@RequiresApi(Build.VERSION_CODES.O)
fun getExtrema(db: SQLiteDatabase) :
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
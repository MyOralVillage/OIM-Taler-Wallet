/*
 * This file is part of GNU Taler
 * (C) 2025 Taler Systems S.A.
 *
 * GNU Taler is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3, or (at your option) any later version.
 */

package net.taler.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import kotlinx.serialization.InternalSerializationApi
import net.taler.database.filter.*                   // TranxFilter, toSQL()
import net.taler.database.data_models.*              // Amount, Tranx, TranxPurp
import net.taler.database.data_access.*              // TransactionDatabase, addTranx, queryTranx
import net.taler.database.schema.Schema

/**
 * Singleton that manages transaction history metadata, cached results, and filter state.
 */
@OptIn(InternalSerializationApi::class)
object TranxHistory {

    // ---- Internal state -----------------------------------------------------

    private var _isStale: Boolean = true
    private var _isIniti: Boolean = false
    private var _history: List<Tranx> = emptyList()
    private var _filtTyp: TranxFilter = TranxFilter() // initial state
    private var _miniDtm: FDtm? = null
    private var _maxiDtm: FDtm? = null
    private var _miniAmt: Amount? = null
    private var _maxiAmt: Amount? = null
    private lateinit var _db: SQLiteDatabase

    // ---- Read-only accessors ------------------------------------------------

    /** The current transaction filter applied to queries. */
    val filtTyp: TranxFilter get() = _filtTyp

    /** Minimum timestamp of transactions in the cache or filter. */
    val miniDtm: FDtm? get() = _miniDtm

    /** Maximum timestamp of transactions in the cache or filter. */
    val maxiDtm: FDtm? get() = _maxiDtm

    /** Minimum amount of transactions in the cache or filter. */
    val miniAmt: Amount? get() = _miniAmt

    /** Maximum amount of transactions in the cache or filter. */
    val maxiAmt: Amount? get() = _maxiAmt

    /** Path to the test database. */
    const val TRXN_HIST_TEST_DB_PATH =
        "transaction-history-test/1/transaction_history.db"


    // ---- API ----------------------------------------------------------------

    /**
     * Initialize the transactions database.
     *
     * @param context Android context for database access; may be null for testing.
     */
    fun init(context: Context?) = synchronized(this) {
        if (!_isIniti) {

            // load database
            _db = TransactionDatabase(context).readableDatabase

            // query extrema from db
            val extrema : Pair<Pair<FDtm, FDtm>, Pair<Amount, Amount>>? = getExtrema(_db)

            // set extrema values; if db empty extrema is null
            when (extrema) {
                null -> { _miniDtm = null; _maxiDtm = null; _miniAmt = null; _maxiAmt = null }
                else -> { _miniDtm = extrema.first.first;  _maxiDtm = extrema.first.second
                          _miniAmt = extrema.second.first; _maxiAmt = extrema.second.second }
            }

            // initialize database
            _isIniti = true
        }
    }

    /**
     * Initializes the transaction history database using a prebuilt test database.
     *
     * Copies the bundled test DB from assets into the app's internal database directory,
     * overwriting any existing version, and initializes [TranxHistory].
     *
     * **This should only be used for testing or preview purposes.**
     *
     * @param context Android context used to access assets and database paths.
     * @param version Optional database version; defaults to [Schema.VERSION].
     */
    fun initTest(context: Context, version: Int? = null) = synchronized(this) {

//        // for use ONLY when this is properly tested!
//        if (!BuildConfig.DEBUG) {
//            throw UnsupportedOperationException("initTest() is only available in debug builds.")
//        }
        if (!_isIniti) {
            val dbFile = context.getDatabasePath("transaction_history.db")
            dbFile.parentFile?.mkdirs()

            val versionStr = (version ?: Schema.VERSION).toString()
            val assetPath = "transaction-history-test/v$versionStr/transaction_history.db"

            try {
                // Always overwrite for predictable test behavior
                context.assets.open(assetPath).use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                // Initialize TranxHistory with the new database
                init(context)

            } catch (e: Exception) {
                throw IllegalStateException(
                    "Failed to initialize test database from assets: $assetPath", e
                )
            }
        }
    }

    /**
     * Updates the internal filter and marks cached history as stale if the filter changes.
     *
     * @param filter The new filter for history queries.
     */
    fun setFilter(filter: TranxFilter) = synchronized(this) {
        if(filter != _filtTyp) {
            _isStale = true
            _filtTyp = filter
        }
    }

    /**
     * Add a new transaction to the database and update cached bounds.
     *
     * @param tid Unique transaction identifier.
     * @param purp Optional transaction purpose.
     * @param amt Transaction amount.
     * @param dir Transaction direction (incoming/outgoing).
     * @param tms Transaction timestamp.
     * @throws IllegalStateException if the database has not been initialized.
     */
    fun newTransaction(
        tid: String,
        purp: TranxPurp?,
        amt: Amount,
        dir: FilterableDirection,
        tms: Timestamp,
    ) = synchronized(this) {
        check(_isIniti) { "Database is not initialized" }

        // update amount bounds
        val newAmtScalar = amt.value + amt.fraction.toLong()
        val minScalar = _miniAmt?.let { it.value + it.fraction.toLong() } ?: Long.MAX_VALUE
        val maxScalar = _maxiAmt?.let { it.value + it.fraction.toLong() } ?: Long.MIN_VALUE
        if (newAmtScalar >= maxScalar) _maxiAmt = amt
        if (newAmtScalar <= minScalar) _miniAmt = amt

        // update datetime bounds
        val newDtm = FDtm(tms, null)
        _maxiDtm = when (_maxiDtm) {
            null -> newDtm
            else -> if (_maxiDtm!! <= newDtm) newDtm else _maxiDtm
        }
        _miniDtm = when (_miniDtm) {
            null -> newDtm
            else -> if (_miniDtm!! >= newDtm) newDtm else _miniDtm
        }

        // insert and mark history stale
        addTranx(_db, Tranx(tid, newDtm, purp, amt, dir))
        _isStale = true
    }

    /**
     * Returns cached transaction history; refreshes from the database if marked stale.
     *
     * @return List of transactions according to the current filter.
     */
    fun getHistory(): List<Tranx> = synchronized(this) {
        if (!_isStale) return _history
        _history = queryTranx(_db, _filtTyp.toSQL())
        _isStale = false
        return _history
    }

    /**
     * Resets the filter to default (all values) and marks cached history as stale.
     * Next call to [getHistory] will refresh the data.
     */
    fun clearHistory() = synchronized(this) {
        _filtTyp = TranxFilter()
        _isStale = true
    }
}
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
import android.os.Build
import androidx.annotation.RequiresApi

// Local database module
import net.taler.database.filter.*                   // TranxFilter, toSQL()
import net.taler.database.data_models.*              // Amount, Tranx, TranxPurp
import net.taler.database.data_access.*              // TransactionDatabase, addTranx, queryTranx



/**
 * Singleton that manages transaction history metadata and cached results.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object TranxHistory {

    // ---- Internal state -----------------------------------------------------

    private var _isStale: Boolean = true
    private var _isIniti: Boolean = false
    private var _history: List<Tranx> = emptyList()

    private var _filtTyp: TranxFilter = TranxFilter() // initial state

    private var _miniDtm: FilterableLocalDateTime? = null
    private var _maxiDtm: FilterableLocalDateTime? = null

    private var _miniAmt: Amount? = null
    private var _maxiAmt: Amount? = null

    private lateinit var _db: SQLiteDatabase

    // ---- Read-only accessors ------------------------------------------------

    val isStale: Boolean get() = _isStale
    val isIniti: Boolean get() = _isIniti

    val filtTyp: TranxFilter get() = _filtTyp

    val miniDtm: FilterableLocalDateTime? get() = _miniDtm
    val maxiDtm: FilterableLocalDateTime? get() = _maxiDtm

    val miniAmt: Amount? get() = _miniAmt
    val maxiAmt: Amount? get() = _maxiAmt

    // ---- API ----------------------------------------------------------------

    /**
     * Initialize the transactions database (must be called exactly once).
     */
    fun init(context: Context?) {
        check(!_isIniti) { "TranxHistory already initialized" }
        _db = TransactionDatabase(context).readableDatabase
        _isIniti = true
    }

    /**
     * Update the active filter; marks history as stale so next read refreshes.
     */
    fun setFilter(filter: TranxFilter) {
        _filtTyp = filter
        _isStale = true
    }

    /**
     * Add a transaction and update cached bounds.
     */
    fun newTransaction(
        tid: String,
        purp: TranxPurp?,
        amt: Amount,
        dir: FilterableDirection,
        tms: Timestamp,
    ) {
        check(_isIniti) { "Database is not initialized" }

        // update amount bounds
        val newAmtScalar = amt.value + amt.fraction.toLong()
        val minScalar = _miniAmt?.let { it.value + it.fraction.toLong() } ?: Long.MAX_VALUE
        val maxScalar = _maxiAmt?.let { it.value + it.fraction.toLong() } ?: Long.MIN_VALUE
        if (newAmtScalar >= maxScalar) _maxiAmt = amt
        if (newAmtScalar <= minScalar) _miniAmt = amt

        // update datetime bounds
        val newDtm = FilterableLocalDateTime(tms)
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
     * Return cached history; refresh if marked stale.
     */
    fun getHistory(): List<Tranx> {
        if (!_isStale) return _history
        _history = queryTranx(_db, _filtTyp.toSQL())
        _isStale = false
        return _history
    }
}

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
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import net.taler.common.transaction.TranxHistory._filtTyp
import net.taler.common.utils.directionality.FilterableDirection
import net.taler.common.utils.time.FilterableLocalDateTime
import net.taler.common.utils.time.Timestamp
import okhttp3.internal.toImmutableList

/**
 * Singleton object that manages transaction history and related metadata.
 * Provides functions to initialize the database, add new transactions,
 * set filters, and retrieve the cached transaction history.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object TranxHistory {

    // ==== INTERNAL VARIABLES====
    private var _isStale : Boolean = true
    private var _isIniti : Boolean = false
    private var _history : List<Tranx> = emptyList()
    private var _filtTyp : TranxFilter = TranxFilter() // initial state
    private var _miniDtm : FilterableLocalDateTime? = null
    private var _maxiDtm : FilterableLocalDateTime? = null
    private var _miniAmt : Amount? = null
    private var _maxiAmt : Amount? = null
    private lateinit var _trxn_db : SQLiteDatabase

    //====PUBLIC READ ONLY VARIABLES====

    /** state of history; updated if stale */
    val isStale : Boolean  get() = _isStale

    /** internal of transaction history;
     * must be true before any operations are called  */
    val isIniti : Boolean  get() = _isIniti

    /** returns the filter type */
    val filtTyp : TranxFilter get() = _filtTyp

    /** returns the minimum datetime */
    val miniDtm : FilterableLocalDateTime? get()  = _miniDtm

    /** returns the maximum datetime */
    val maxiDtm : FilterableLocalDateTime? get()  = _maxiDtm

    /** returns the minimum amount */
    val miniAmt : Amount? get()  = _miniAmt

    /** returns the maximum amount */
    val maxiAmt : Amount? get()  = _maxiAmt

    /**
     * Initializes the transaction database for reading.
     *
     * If [context] is non-null, this function opens a readable instance
     * of the transaction database.
     * If [context] is null, it creates an empty database stored on disk.
     *
     * **Note:** **This function must only be called once!**
     *
     * @param context The Android [Context] used to open the database. If null,
     *                an empty database is created on disk.
     * @throws IllegalStateException if this function is called more than once.
     *
     */
    fun init(context: Context?) {
        if (_isIniti) throw IllegalStateException("Cannot reinitialize!")
        _trxn_db = TransactionDatabase(context).readableDatabase
        _isIniti = true
    }

    /**
     * Sets the transaction filter to a new filter type.
     *
     * This function updates the current filter (`_filtTyp`) and marks
     * the transaction history as stale (`_isStale = true`), so that
     * the next call to [getHistory] will refresh the cached list
     * based on the new filter.
     *
     * **Note:** Changing the filter invalidates cached history
     * triggers a new database query on the next read. Call this function
     * only when it is necessary to apply a new filter.
     * Use [TranxFilter.isEqual] to test if the new filter
     * is identical to the current one.
     *
     * @param filter The new [TranxFilter] to apply.
     */
    fun setFilter(filter: TranxFilter) {
        _filtTyp = filter
        _isStale = true
    }

    /**
     * Adds a new transaction to the database and updates cached bounds.
     *
     * This function performs the following steps:
     * 1. Updates the minimum and maximum amount bounds (_miniAmt, _maxiAmt)
     *    based on the new transaction amount.
     * 2. Updates the minimum and maximum datetime bounds (_miniDtm, _maxiDtm)
     *    based on the new transaction timestamp.
     * 3. Inserts the new [Tranx] into the database using [addTranx].
     * 4. Marks the transaction history as stale (_isStale = true) so that
     *    cached lists will be refreshed on the next read.
     *
     * @param purp The purpose of the transaction ([TranxPurp]).
     * @param amt The transaction amount ([Amount]).
     * @param dir The transaction direction ([FilterableDirection].
     * @param tms The timestamp of the transaction ([Timestamp]).
     * @throws IllegalStateException if [TranxHistory] is not initialized
     */
    fun newTransaction(
        purp: TranxPurp,
        amt: Amount,
        dir: FilterableDirection,
        tms: Timestamp) {

        if (!_isIniti) throw IllegalStateException("Database is not initialized!")

        // check amount bounds; update if needed
        val new_amt = amt.value + amt.fraction.toLong()
        val old_min = when (_miniAmt) {
            null -> Long.MAX_VALUE // must compare <= since null -> max_value
            else -> _miniAmt!!.value +  _miniAmt!!.fraction.toLong()
        }
        val old_max = when (_maxiAmt) {
            null -> Long.MIN_VALUE // must compare >= since null -> min_value
            else -> _maxiAmt!!.value +  _maxiAmt!!.fraction.toLong()
        }
        if (new_amt >= old_max) _maxiAmt = amt
        if (new_amt <= old_min) _miniAmt = amt

        // check datetime bounds; update if needed
        val new_dtm = FilterableLocalDateTime(tms)
        if (_maxiDtm == null) _maxiDtm = new_dtm
        else (if (_maxiDtm!! <= new_dtm) _maxiDtm = new_dtm else Unit)
        if (_miniDtm == null) _miniDtm = new_dtm
        else {(if (_miniDtm!! >= new_dtm) _miniDtm = new_dtm else Unit) }

        // add transaction to database
        addTranx(_trxn_db, Tranx(new_dtm, purp, amt, dir))
        _isStale = true
    }

    /**
     * Retrieves the transaction history as a [List] of [Tranx].
     *
     * This function checks whether the cached history (`_history`) is stale:
     * - If the history is **not stale**, it returns the cached list.
     * - If the history **is stale**, it queries the database using the current
     *   filter (`filtTyp.toSQL()`) and returns the updated list.
     *
     * @return a [List] of [Tranx] representing the transaction history,
     *         or `emptyList()` if the history is not yet loaded.
     */
    fun getHistory() : List<Tranx> {

        // if history is not stale, return it
        if (!(_isStale)) return _history

        // if it is stale, we have to update the history
        return queryTranx(_trxn_db, filtTyp.toSQL())
    }
}
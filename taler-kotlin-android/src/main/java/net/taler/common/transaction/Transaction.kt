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
import android.database.sqlite.*
import android.os.Build
import androidx.annotation.RequiresApi
import net.taler.common.utils.directionality.FilterableDirection
import net.taler.common.utils.time.FilterableLocalDateTime

/**
 * Represents a financial transaction with a timestamp, purpose, and amount.
 * @property datetime The UTC timestamp of the transaction.
 * @property purpose The purpose of the transaction, represented by a [TranxPurp].
 * @property amount The amount involved in the transaction as an [Amount].
 * @property direction Indicates outgoing or ingoing transactions
 * @constructor Creates a [Transaction] with the given timestamp, purpose, and amount.
 * @param datetime The time the transaction occurred.
 * @param purp The purpose of the transaction.
 * @param amnt The transaction amount.
 * @param dir direction of the transaction relative to the wallet
 */
@RequiresApi(Build.VERSION_CODES.O)
data class Transaction (
    val dateTimeUTC: FilterableLocalDateTime,
    val purpose: TranxPurp,
    val amount: Amount,
    val direction: FilterableDirection
)

// loads transaction history indexes
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object TransactionHistory {

    // ==== INTERNAL VARIABLES====
    private var _isStale : Boolean = true
    private var _isIniti : Boolean = false
    private var _history : List<Transaction> = emptyList()
    private var _filtTyp : TranxFilter = TranxFilter()
    private var _miniDtm : FilterableLocalDateTime? = null
    private var _maxiDtm : FilterableLocalDateTime? = null
    private var _miniAmt : Amount? = null
    private var _maxiAmt : Amount? = null
    private var _trxn_db : SQLiteDatabase? = null

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

}

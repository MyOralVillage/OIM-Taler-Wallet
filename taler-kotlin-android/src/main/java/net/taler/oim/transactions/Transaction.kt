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

package net.taler.oim.transactions

import android.os.Build
import androidx.annotation.RequiresApi
import net.taler.common.Amount
import net.taler.common.Timestamp
import java.util.*
import java.time.*

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
 * @param dir true is incoming to wallet, false is outgoing from wallet
 */
@RequiresApi(Build.VERSION_CODES.O)
data class Transaction (
    val dateTimeUTC: LocalDateTime,
    val purpose: TranxPurp,
    val amount: Amount,
    val direction: Boolean
    )
// loads transaction history indexes
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
object TransactionHistory {

    /** initialized state of transaction history */
    private var state : Boolean = false

    /** transactions organized by purpose */
    private lateinit var indexedTranxPurp :  MutableMap<TranxPurp, Transaction>

    /** transactions organized by UTC date */
    private lateinit var indexedDatetime : MutableMap<LocalDateTime, Transaction>

    /** transactions organized by amount (absolute value) */
    private lateinit var indexedAmount : MutableMap<Amount, Transaction>

    /** transactions organized by direction flow */
    private lateinit var indexedDirection : MutableMap<Boolean, Transaction>

    /**
     * Initializes the internal indices of the transaction store.
     * This must be called **before any other operations** are performed
     * on the singleton object to ensure the data structures are properly set up.
     * @param  byPurpose      Map organizing transactions by their purpose.
     * @param  byDateTime     Map organizing transactions by UTC date and time.
     * @param  byAmount       Map organizing transactions by absolute amount.
     * @param  byDirection    Map organizing transactions by direction (Boolean key).
     * @throws IllegalStateException if state is already initialized
     */
    fun init(
        byPurpose:  MutableMap<TranxPurp, Transaction>,
        byDateTime: MutableMap<LocalDateTime, Transaction>,
        byAmount:   MutableMap<Amount, Transaction>,
        byDirection:   MutableMap<Boolean, Transaction>,
    ) {
        if(state) throw IllegalStateException("Cannot reinitialize transaction history!")

        // initialize directions
        indexedTranxPurp = byPurpose
        indexedDatetime = byDateTime
        indexedAmount = byAmount
        indexedDirection = byDirection

        // initialize state
        state = true
    }

    /** updates indices of transactions */
    private fun updateIndices(trxn: Transaction) {
        indexedTranxPurp.put(trxn.purpose, trxn)
        indexedDatetime.put(trxn.dateTimeUTC, trxn)
        indexedAmount.put(trxn.amount, trxn)
        indexedDirection.put(trxn.direction, trxn)
    }

    /**
     * Creates a new transaction instance
     * @param purpose a [TranxPurp] enum
     * @param amnt an [Amount] representing the magnitude of the transaction
     * @param dir boolean false represents outgoing, true represents incoming
     */
    fun newTransaction(purpose: TranxPurp, amnt: Amount, dir: Boolean) {
        if (!state) throw IllegalStateException("Transaction history is not initialized!");

        // create new time instance (in epoch)
        val time = Timestamp.now().ms
        val it = Instant.ofEpochMilli(time)
        val tz = ZoneId.of("UTC")

        // date time in UTC (yyyy-MM-dd-HH-mm-ss-UTC)
        val dtm = LocalDateTime.ofInstant(it, tz)

        // create new transaction
        val trxn = Transaction(dtm, purpose, amnt, dir)

        // add to indices
        updateIndices(trxn)
    }

    // add method for serializing updated transactions and saving to disk
}

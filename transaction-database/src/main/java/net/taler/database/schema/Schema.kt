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

package net.taler.database.schema

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZoneId

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
    const val TID_COL = "transaction_identity"
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
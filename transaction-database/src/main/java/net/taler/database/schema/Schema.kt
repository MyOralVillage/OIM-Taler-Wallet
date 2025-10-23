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

import java.time.ZoneId

/** Holds constants for the transaction history database schema */
object Schema {

    /** Current database version used for schema migrations. */
    const val VERSION = 1

    /** Path to the test database file, versioned for consistency across migrations. */
    const val TRXN_HIST_TEST_DB_PATH =
        "transaction-history-test/v${VERSION}/transaction_history.db"

    /** File name of the database used in production and testing environments. */
    const val DATABASE_NAME = "transaction_history.db"

    /** Name of the primary SQLite table used to store transaction history entries. */
    const val TABLE_NAME = "TRANSACTION_HISTORY"

    /** Column name for the unique index or identifier of each transaction record. */
    const val INDEX_COL = "id"

    /** Column name storing the transaction timestamp in epoch milliseconds (UTC). */
    const val EPOCH_MILLI_COL = "epoch_milliseconds"

    /** Column name representing the transaction amount in minor currency units. */
    const val AMOUNT_COL = "amount"

    /** Column name storing the ISO 4217 currency code (e.g., "USD", "EUR"). */
    const val CURRENCY_COL = "currency"

    /**
     * Column name for storing currency-specific metadata or formatting information.
     * If no additional specification exists, this field should contain an empty string.
     */
    const val CURRENCY_SPEC_COL = "currency_specifications"

    /** Column name describing the transaction purpose (e.g., "groceries", "salary"). */
    const val TRNX_PURPOSE_COL = "tranx_purpose"

    /**

    Column name indicating whether the transaction is incoming or outgoing.
    Stored as an integer: 1 for true (incoming), 0 for false (outgoing).
     */
    const val TRNX_INCOMING_COL = "incoming"

    /** Column name representing the unique transaction identity or hash. */
    const val TID_COL = "transaction_identity"

    /** Cursor factory used when creating readable
     * or writable database connections. */
    val CURSOR_FACTORY = null

    /** Default time zone used for all date-time conversions in the database (UTC). */
    val DEFAULT_TIME_ZONE: ZoneId = ZoneId.of("UTC")

    /** Index name for optimizing queries by transaction datetime. */
    const val DATETIME_INDEX = "idx_dtm"

    /** Index name for optimizing queries by transaction amount. */
    const val AMOUNT_INDEX = "idx_amt"

    /** Index name for optimizing queries by transaction direction */
    const val TRXN_DIR_INDEX = "incoming_index"

    /** Index name for optimizing queries by transaction purpose. */
    const val PURPOSE_INDEX = "idx_purp"
}
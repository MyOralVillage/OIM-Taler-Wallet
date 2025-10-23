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

import android.content.Context
import android.database.*
import android.database.sqlite.*
import net.taler.database.schema.Schema


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
internal class TransactionDatabase(context: Context?)
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
            CREATE TABLE IF NOT EXISTS ${Schema.TABLE_NAME} (
                ${Schema.INDEX_COL} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${Schema.TID_COL} TEXT UNIQUE NOT NULL,
                ${Schema.EPOCH_MILLI_COL} INTEGER NOT NULL,
                ${Schema.AMOUNT_COL} INTEGER NOT NULL,
                ${Schema.CURRENCY_COL} TEXT NOT NULL,
                ${Schema.CURRENCY_SPEC_COL} TEXT, 
                ${Schema.TRNX_PURPOSE_COL} TEXT,
                ${Schema.TRNX_INCOMING_COL} INTEGER NOT NULL) 
            """.trimIndent()

        // create custom index based on amount (chained; first checks int, then frac)
        val createAmountIndexQuery =
            """
            CREATE INDEX IF NOT EXISTS ${Schema.AMOUNT_INDEX}
            ON ${Schema.TABLE_NAME}(${Schema.AMOUNT_COL})
            """.trimIndent()

        // create custom index on datetime (epoch milliseconds)
        val createTimeIndexQuery =
            """
             CREATE INDEX IF NOT EXISTS ${Schema.DATETIME_INDEX}
             ON ${Schema.TABLE_NAME}(${Schema.EPOCH_MILLI_COL})
            """.trimIndent()

        // create custom index on direction (boolean integers)
        val createDirectionIndexQuery =
            """
            CREATE INDEX IF NOT EXISTS ${Schema.TRXN_DIR_INDEX}
            ON ${Schema.TABLE_NAME}(${Schema.TRNX_INCOMING_COL})
            """.trimIndent()

        // create custom index on transaction purpose
        val createPurposeIndexQuery =
            """
             CREATE INDEX IF NOT EXISTS ${Schema.PURPOSE_INDEX}
             ON ${Schema.TABLE_NAME}(${Schema.TRNX_PURPOSE_COL})
            """.trimIndent()

        // build indices
        db.execSQL(createTableQuery)
        db.execSQL(createAmountIndexQuery)
        db.execSQL(createTimeIndexQuery)
        db.execSQL(createDirectionIndexQuery)
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

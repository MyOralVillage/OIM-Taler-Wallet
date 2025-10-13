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

package net.taler.common

import DirectionFilter
import TranxFilter
import android.database.sqlite.SQLiteDatabase
import org.junit.runner.*
import org.junit.*;
import  org.junit.Assert.*
import org.junit.experimental.runners.*
import androidx.test.ext.junit.runners.*
import android.content.*
import androidx.test.core.app.*
import net.taler.common.transaction.*
import net.taler.common.utils.directionality.*
import net.taler.common.utils.time.*
import org.junit.*
import java.time.*

@RunWith(Enclosed::class)
class TranxDatabaseTest {

    @RunWith(AndroidJUnit4::class)
    class TransactionDatabaseTest {

        private lateinit var db: SQLiteDatabase
        private lateinit var context: Context

        @Before
        fun setup() {
            context = ApplicationProvider.getApplicationContext()
            // Use in-memory database for tests
            db = TransactionDatabase(null).writableDatabase
        }

        @After
        fun teardown() {
            db.close()
        }

        @Test
        fun testAddTransaction() {
            val tranx = Tranx(
                dateTimeUTC = FilterableLocalDateTime(
                    LocalDateTime.now(),
                    Schema.DEFAULT_TIME_ZONE
                ),
                purpose = HLTH_DOCT,
                amount = Amount("USD", 100, 50, null),
                direction = FilterableDirection.OUTGOING
            )

            val id = addTranx(db, tranx)
            assertTrue(id > 0)
        }

        @Test
        fun testGetExtremaEmptyDatabase() {
            val result = getExtrema(db)
            assertNull(result)
        }

        @Test
        fun testGetExtremaWithData() {
            // Add test transactions
            val tranx1 = createTestTranx(amount = 100, epochMilli = 1000L)
            val tranx2 = createTestTranx(amount = 500, epochMilli = 5000L)

            addTranx(db, tranx1)
            addTranx(db, tranx2)

            val result = getExtrema(db)
            assertNotNull(result)

            val (dtmPair, amtPair) = result!!
            assertEquals(1000L, dtmPair.first.epochMillis())
            assertEquals(5000L, dtmPair.second.epochMillis())
        }

        @Test
        fun testToSQLFiltering() {
            // Add test data
            val tranx1 = createTestTranx(
                purpose = EDUC_SCHL,
                direction = FilterableDirection.INCOMING
            )
            addTranx(db, tranx1)

            // Test filtering
            val filter = TranxFilter(
                direction = DirectionFilter.Exact(FilterableDirection.INCOMING)
            )

            val query = filter.toSQL()
            val cursor = db.rawQuery(query, null)

            assertTrue(cursor.count > 0)
            cursor.close()
        }

        private fun createTestTranx(
            amount: Long = 100,
            epochMilli: Long = System.currentTimeMillis(),
            purpose: TranxPurp = UTIL_ELEC,
            direction: FilterableDirection = FilterableDirection.OUTGOING
        ): Tranx {
            return Tranx(
                FilterableLocalDateTime(
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(epochMilli),
                        Schema.DEFAULT_TIME_ZONE
                    ),
                    Schema.DEFAULT_TIME_ZONE
                ),
                purpose,
                Amount("USD", amount, 0, null),
                direction
            )
        }
    }
}
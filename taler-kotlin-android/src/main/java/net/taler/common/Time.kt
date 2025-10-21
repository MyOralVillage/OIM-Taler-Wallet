/*
 * This file is part of GNU Taler
 * (C) 2020 Taler Systems S.A.
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

<<<<<<< HEAD:taler-kotlin-android/src/main/java/net/taler/common/Time.kt
/** **This has been kept for API compatibility only.**
 *
 * see: [net.taler.database.data_models.Timestamp] */
typealias Timestamp = net.taler.database.data_models.Timestamp
=======
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.taler.common.transaction.Amount

@Serializable
data class RefundRequest(
    /**
     * Amount to be refunded
     */
    val refund: Amount,
>>>>>>> eb37a10 (finally got gradle working):merchant-lib/src/main/java/net/taler/merchantlib/Refunds.kt

/** **This has been kept for API compatibility only.**
 *
 * see: [net.taler.database.data_models.RelativeTime] */
typealias RelativeTime = net.taler.database.data_models.RelativeTime

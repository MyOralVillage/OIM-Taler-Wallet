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

package net.taler.database.data_models

/**
 * Represents a financial transaction with a timestamp, purpose, and amount.
 * @property TID the transaction ID
 * @property datetime The UTC timestamp of the transaction.
 * @property purpose The purpose of the transaction, represented by a [TranxPurp]; optional.
 * @property amount The amount involved in the transaction as an [Amount].
 * @property direction Indicates outgoing or ingoing transactions
 * @constructor Creates a [Tranx] with the given timestamp, purpose, and amount.
 * @param datetime The time the transaction occurred.
 * @param purp The purpose of the transaction.
 * @param amnt The transaction amount.
 * @param dir direction of the transaction relative to the wallet
 */
data class Tranx (
    val TID: String,
    val dateTimeUTC: FilterableLocalDateTime,
    val purpose: TranxPurp?,
    val amount: Amount,
    val direction: FilterableDirection
)
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

package net.taler.wallet.oim.send

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import net.taler.database.data_models.Amount

/**
 * Thin façade you can later wire to PeerManager.
 * For Previews / local UI it’s a no-op.
 */
class PeerBridge {
    // expose wallet's push state if you wire it later
    val pushState: Flow<Any?> = MutableStateFlow(null)

    suspend fun checkFees(amount: Amount): Any? = null
    suspend fun send(amount: Amount, purpose: String, hours: Long) { /* no-op */ }
    fun reset() {}
}

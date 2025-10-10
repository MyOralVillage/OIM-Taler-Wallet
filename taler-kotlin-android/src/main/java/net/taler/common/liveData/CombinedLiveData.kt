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

package net.taler.common.liveData

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

/**
 * A specialized [MediatorLiveData] that combines the emissions of two
 * [LiveData] sources into a single output using a custom combination function.
 *
 * This class simplifies reactive transformations where two independent data
 * sources must be observed together (for example, combining user preferences
 * and remote configuration data into a unified UI model).
 *
 * Whenever either [source1] or [source2] emits a new value, the [combine]
 * function is invoked with the latest values from both sources (either of
 * which may be `null`), and its return value is posted to the `value`
 * property of this `CombinedLiveData`.
 *
 * @param T the type of data emitted by the first source
 * @param K the type of data emitted by the second source
 * @param S the type of data produced by the combination function
 * @param source1 the first [LiveData] source
 * @param source2 the second [LiveData] source
 * @param combine a lambda function that defines how to combine the latest
 *                values of [source1] and [source2] into a single result
 *
 * @constructor Creates a new [CombinedLiveData] that observes both sources
 *              and emits combined results whenever either source updates.
 *
 * @see MediatorLiveData
 */
class CombinedLiveData<T, K, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    private val combine: (data1: T?, data2: K?) -> S
) : MediatorLiveData<S>() {

    /** Holds the latest value from [source1]. */
    private var data1: T? = null

    /** Holds the latest value from [source2]. */
    private var data2: K? = null

    init {
        super.addSource(source1) { t ->
            data1 = t
            value = combine(data1, data2)
        }
        super.addSource(source2) { k ->
            data2 = k
            value = combine(data1, data2)
        }
    }

    /**
     * Disabled for safety: external sources cannot be added manually.
     *
     * This restriction ensures that the `CombinedLiveData` only observes
     * the two provided data sources and cannot be misused as a general
     * [MediatorLiveData].
     *
     * @throws UnsupportedOperationException always thrown to prevent modification
     */
    override fun <S : Any?> addSource(source: LiveData<S>, onChanged: Observer<in S>) {
        throw UnsupportedOperationException(
            "Cannot add more sources to CombinedLiveData — it is fixed to two sources."
        )
    }

    /**
     * Disabled for safety: sources cannot be removed manually.
     *
     * This restriction ensures consistency of combined emissions across
     * the lifetime of this object.
     *
     * @throws UnsupportedOperationException always thrown to prevent modification
     */
    override fun <T : Any?> removeSource(toRemove: LiveData<T>) {
        throw UnsupportedOperationException(
            "Cannot remove sources from CombinedLiveData — it always observes both inputs."
        )
    }
}

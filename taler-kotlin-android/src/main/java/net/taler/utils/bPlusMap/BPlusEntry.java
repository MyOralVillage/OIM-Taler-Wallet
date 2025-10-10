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

package net.taler.utils.bPlusMap;

import java.util.*;

/**
 * Represents a key-value pair stored in a B+ tree leaf node.
 * Implements  {@link java.util.Map.Entry}.
 * @param <K> the type of keys, which must be {@link Comparable}
 * @param <V> the type of values associated with the keys
 */
public class BPlusEntry<K extends Comparable<K>, V>
        implements Map.Entry<K, V>,
        Comparable<BPlusEntry<K, V>> {

    /** The key for this entry. Keys are immutable after construction. */
    private final K key;

    /** The value associated with the key. */
    private V val;

    /**
     * Constructs a new key-value entry.
     * @param k the key
     * @param v the value associated with the key
     */
    BPlusEntry(K k, V v) {key = k; val = v;}

    /** @return the key */
    @Override
    public K getKey() {return key;}

    /** @return the value */
    @Override
    public V getValue() {return val;}

    /**
     * Replaces the value associated with the key in this entry.
     * @param v the new value to associate with the key
     * @return the old value previously associated with the key
     */
    @Override
    public V setValue(V v) {V oldVal = val; val = v; return oldVal;}

    /**
     * Compares nodes based off of keys
     * @param e a BPlusEntry to compare to this
     * @return -1, 0, +1 if e less than, equal, or greater than this
     */
    @Override
    public int compareTo(BPlusEntry<K,V> e) {return key.compareTo(e.getKey());}
}

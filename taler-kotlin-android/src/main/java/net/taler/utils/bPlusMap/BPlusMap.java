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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.*;

public class BPlusMap<K extends Comparable<K>, V> implements Map<K, V> {

    /** maximum number of child nodes per parent */
    public final int degree;

    /** number of entries in tree */
    private int size = 0;

    /** root leaf (first in linked list) */
    private BPlusNodeLeaf<K,V> firstLeaf;

    /** root node */
    private BPlusNode<K,V> root;

    /** Initializes an empty B+ Tree
     * @param degree the maximum number of nodes per parent
     */
    public BPlusMap(int degree) {
        if (degree < 1) throw new IllegalArgumentException("Degree must be >= 1");
        this.degree = degree;
        root = null;
        firstLeaf = null;
    }

    /** @return the number of entries in the tree */
    @Override
    public int size() {return size;}

    /** @return true if no entries in tree */
    @Override
    public boolean isEmpty() {return size == 0;}

    /**
     * Searches for a key in the tree.
     * @param o a key to search for.
     * @return true if key exists in tree, false otherwise
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(@Nullable Object o) {

        // special case: tree is empty
        if (isEmpty()) return false;

        // if o isn't instance of K, cannot be in tree; throw error
        K k; try {k = (K) o;} catch (ClassCastException e) {
            throw new IllegalArgumentException("Illegal key type.");
        }

        // go to correct leaf
        BPlusNode<K,V> bpn = root;
        while (!(bpn.isLeaf())) {
            BPlusNodeInternal<K,V> bpni = (BPlusNodeInternal<K,V>) bpn;
            bpn = bpni.next(k);
        }
        BPlusNodeLeaf<K,V> bpnl = (BPlusNodeLeaf<K,V>) bpn;

        // do binary search for key
        return bpnl.getByKey(k) != null;
    }

    /**
     * Searches for a value in the tree.
     * @param o an object to search for.
     * @return true if value exists in tree, false otherwise
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsValue(@Nullable Object o) {

        // special case: tree is empty
        if (isEmpty()) return false;

        // if o isn't instance of V, cannot be in tree; throw error
        V v; try {v = (V) o;} catch (ClassCastException e) {
            throw new IllegalArgumentException("Illegal value type.");
        }

        // search for values
        BPlusNodeLeaf<K,V> bpnl = firstLeaf;
        while (bpnl != null) {
            if (bpnl.getByValue(v) != null) return true;
            bpnl = bpnl.getNext();
        }

        // not found
        return false;
    }

    /**
     * Searches for a value from a key.
     * @param o a key to search for
     * @return the value 
     */
    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public V get(@Nullable Object o) {
        if (isEmpty()) return null;

        // if o isn't instance of K, cannot be in tree; throw error
        K k; try {k = (K) o;} catch (ClassCastException e) {
            throw new IllegalArgumentException("Illegal key type.");
        }

        // go to correct leaf
        BPlusNode<K,V> bpn = root;
        while (!(bpn.isLeaf())) {
            BPlusNodeInternal<K,V> bpni = (BPlusNodeInternal<K,V>) bpn;
            bpn = bpni.next(k);
        }
        Entry<K,V> bpe = ((BPlusNodeLeaf<K,V>) bpn).getByKey(k);

        // do binary search for key, return val if found or null if not)
        return (bpe!=null) ? bpe.getValue() : null;
    }

    @Override
    public V put(K k, V v) {

        // if key or value are null, throw error
        if (k==null || v==null) throw new IllegalArgumentException("Key/value cannot be null");

        // special case: tree is empty
        if (isEmpty()) {
             firstLeaf = new BPlusNodeLeaf<>(degree);
             root = firstLeaf;
             firstLeaf.addEntry(k,v);
             firstLeaf.setPrev(null);
             firstLeaf.setNext(null);
             size++;
             return null;
         }

        // traverse down to insertion point
        BPlusNode<K,V> bpn = root;
        while (!(bpn.isLeaf())) {
            BPlusNodeInternal<K,V> bpni = (BPlusNodeInternal<K,V>) bpn;
            bpn = bpni.next(k);
        }
        BPlusNodeLeaf<K,V> bpnl = (BPlusNodeLeaf<K,V>) bpn;

        // check if key exists; if so replace value at key
        if (bpnl.getByKey(k) != null) {return (bpnl.getByKey(k)).setValue(v);}


        return null;
    }

    @Nullable
    @Override
    public V remove(@Nullable Object o) {
        return null;
    }

    /**
     * Copies all values in map into this map. Will overwrite values with duplicate keys.
     * @param map a map
     */
    @Override
    public void putAll(@NonNull  Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e: map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }

    /** deletes tree, and resets to empty tree */
    @Override
    public void clear() {
        root = null;
        firstLeaf = null;
        size = 0;
    }

    @NonNull
    @Override
    public  Set<K> keySet() {
        return Collections.emptySet();
    }

    @NonNull
    @Override
    public  Collection<V> values() {
        return Collections.emptyList();
    }

    @NonNull
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return Collections.emptySet();
    }
}

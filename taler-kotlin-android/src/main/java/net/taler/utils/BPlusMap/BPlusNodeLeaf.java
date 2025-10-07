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

package net.taler.utils.BPlusMap;

import java.util.*;

/**
 * Represents a terminal leaf node in a B+ tree.
 * Stores sorted key-value pairs to other BPlusNodes (either internal or leaf).
 * @param <K> the type of keys, which must be comparable
 * @param <V> the type of values stored in the tree
 */
public class BPlusNodeLeaf<K extends Comparable<K>, V> extends BPlusNode<K,V> {

    // leafs form linked list; initialized to nul.
    private BPlusNodeLeaf<K,V> next = null;
    private BPlusNodeLeaf<K,V> prev = null;

    // number of KV entries in node
    private final ArrayList<BPlusEntry<K,V>> entries = new ArrayList<>();

    /** static method to assert a node is not null */
    private static <K extends Comparable<K>, V> BPlusNode<K, V>
    checkNode(BPlusNodeInternal<K, V> node) {
        if (node == null) throw new IllegalArgumentException("Undefined parent");
        return node;
    }

    /**
     * Constructs a new root leaf node with an explicitly specified degree.
     * Use this constructor when creating the root node.
     * @param degree the maximum number of children this node can have
     */
    public BPlusNodeLeaf(int degree){super(null,degree);}

    /**
     * Adds a new entry
     * Keys must remain in sorted order (caller is responsible for maintaining order).
     * @param k the key to add
     * @throws IllegalStateException if the node already has the maximum number of entries
     * @throws IllegalArgumentException if k or v is null
     */
    public void addEntry(K k, V v) throws IllegalStateException {
        if (isFull()) throw new IllegalStateException("Cannot add entry to full node");
        if (k==null||v ==null) throw new IllegalArgumentException("Entry cannot be null!");
        BPlusEntry<K,V> bpe = new BPlusEntry<>(k,v);

        // do binary search to find insertion point
        int idx = Collections.binarySearch(entries, bpe);

        // idx is -ve if exact value not found (idx = -(insertion_point) - 1);
        // it therefore must be flipped and incr. to find correct idx
        if (idx < 0) idx = -idx - 1;

        // shift all elements to right from idx and inserts; worst case is O(n)
        entries.add(idx, bpe);
    }

    /**
     * This method performs a binary search over the sorted list of entries
     * to find the entry with the matching key. If no such entry exists, {@code null} is returned.
     * @param k the key to search for; must not be {@code null}
     * @return the {@link BPlusEntry} with the specified key, or {@code null} if no such entry exists
     * @throws IllegalArgumentException if {@code k} is {@code null}
     */
    public BPlusEntry<K,V> getEntry(K k) {
        if (k==null) throw new IllegalArgumentException("Key cannot be null!");

        // if no entry exists in leaf, return null
        if (entries.isEmpty()) return null;

        // create a dummy entry for comparison (entries compared on keys);
        // set v to entries.getFirst(), then do binary search on entries
        BPlusEntry<K,V> dummy = new BPlusEntry<>(k, entries.get(0).getValue());
        int idx = Collections.binarySearch(entries, dummy);

        // if idx is -ve, that means binary k is not in entries; return null
        // else, return the value at entries[idx]
        if (idx<0) return null;
        else return entries.get(idx);
    }

    /**
     * Constructs a new leaf node with the same degree as its parent.
     * Use this constructor when adding a child node to an existing tree.
     * @param parent the parent internal node; must not be null
     * @throws IllegalArgumentException if parent is null
     */
    public BPlusNodeLeaf(BPlusNodeInternal<K,V> parent) {
        super(checkNode(parent), checkNode(parent).degree);
    }

    /**
     * Checks if the leaf has reached the maximum number of entries.
     * @return true if the number of entries equals the B+ tree's degree, false otherwise
     */
    public boolean isFull() {return entries.size() == degree;}

    /** @return the next leaf in the linked list */
    public BPlusNodeLeaf<K,V> getNext() {return next;}

    /** @return the previous leaf in the linked list */
    public BPlusNodeLeaf<K,V> getPrev() {return prev;}

    /** @param n the node to set as the next node */
    public void setNext(BPlusNodeLeaf<K,V> n){next = n;}

    /** @param n the node to set as the previous node */
    public void setPrev(BPlusNodeLeaf<K,V> n){prev = n;}
}
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

import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents an internal (non-leaf) node in a B+ tree.
 * Stores sorted keys and child pointers to other BPlusNodes (either internal or leaf).
 * @param <K> the type of keys, which must be comparable
 * @param <V> the type of values stored in the tree
 */
public class BPlusNodeInternal<K extends Comparable<K>, V> extends BPlusNode<K,V>  {

    /** static method to assert a node is not null */
    private static <K extends Comparable<K>, V> BPlusNode<K, V>
    checkNode(BPlusNode<K, V> node) {
        if (node == null) throw new IllegalArgumentException("Undefined node");
        return node;
    }

    /**
     * The ordered list of keys stored in this internal node.
     * These keys guide traversal to the appropriate child node.
     */
    private final ArrayList<K> keys = new ArrayList<>();

    /** The list of child nodes. Always one more than the number of keys. */
    private final ArrayList<BPlusNode<K, V>> children = new ArrayList<>();

    /**
     * Use this constructor when adding the first internal node.
     * This does NOT update keys or children (must be manually updated).
     * Nor does it update the leaf's parents. This just ensures that
     * the new internal node has the same degree as the leaf,
     * and the parent of this node is set to null.
     * @param leaf the previous
     * @throws IllegalArgumentException if leaf is null
     */
    public BPlusNodeInternal(BPlusNodeLeaf<K,V> leaf) {
        super(null, checkNode(leaf).degree);
    }

    /**
     * Returns the next child based on a key value
     * @param k the key to search for
     * @return the next child node
     */
    public BPlusNode<K,V> next(K k) {

        // do binary search to find "position" of key
       int pos = Collections.binarySearch(keys, k);

       // return the next node
       return (pos>=0) ? children.get(pos + 1) : children.get(-pos-1);
    }

    /**
     * Constructs a new internal node with the same degree as its parent.
     * Use this constructor when adding a child node to an existing tree.
     * @param parent the parent internal node; must not be null
     * @throws IllegalArgumentException if parent is null
     */
    public BPlusNodeInternal(BPlusNodeInternal<K, V> parent) {
        super(checkNode(parent), checkNode(parent).degree);
    }

    /**
     * Adds a key and child to this internal node.
     * Keys must remain in sorted order (caller is responsible for maintaining order).
     * @param k the key to add
     * @throws IllegalStateException if the node has the maximum number of children or none
     * @throws IllegalArgumentException if k is null
     */
    public void addKeyChild(K k, BPlusNode<K,V> c) throws IllegalStateException {
        if (isFull()) throw new IllegalStateException("Cannot add to full node");
        if (k==null||c==null) throw new IllegalArgumentException(
                "Key and/or child cannot be null!"
        );

        // find insertion point in keys
        int pos = Collections.binarySearch(keys, k);

        // if pos does not exist, must convert to valid position
        if (pos < 0) pos = -pos - 1;

        // add value
        keys.add(pos, k);
        c.setParent(this);
        children.add(pos+1, c);
    }

    /**
     * Checks if a node contains a specific key
     * @param k a key to search for
     */
    public boolean containsKey(K k) {
        if (k == null) throw new IllegalArgumentException ("Key cannot be null!");
        return 0 <= Collections.binarySearch(keys, k);
    }

    /**
     * Checks if the internal node has reached the maximum number of child nodes.
     * @return true if the number of children equals the B+ tree's degree, false otherwise
     */
    boolean isFull() {return children.size() == degree;}
}
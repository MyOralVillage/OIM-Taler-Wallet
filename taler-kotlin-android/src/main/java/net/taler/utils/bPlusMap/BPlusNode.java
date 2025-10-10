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

/**
 * Abstract base class for all nodes in a B+ tree (internal and leaf nodes).
 *
 * Each node holds a reference to its parent and knows the tree's degree (maximum number of children).
 * Subclasses must implement their own behavior for storing entries or keys and children.
 *
 * @param <K> the type of keys, which must be comparable
 * @param <V> the type of values stored in the leaf nodes
 */
public abstract class
BPlusNode<K extends Comparable<K>, V> {

    /** The parent node of this node. */
    BPlusNode<K, V> parent;

    /** The maximum number of entries in the node */
    final int degree;

    /**
     * Constructs a B+ tree node
     * @param p the parent node (can be null if this is the root)
     * @param d the degree of the tree
     */
    BPlusNode(BPlusNode<K, V> p, int d) {parent = p; degree = d;}

    /**
     * Sets the parent of this node.
     * @param p the new parent node
     */
    public void setParent(BPlusNode<K, V> p) {
        parent = p;
    }

    /** @return true if this node is a leaf */
    public boolean isLeaf() {return this instanceof BPlusNodeLeaf<K,V>;}
}
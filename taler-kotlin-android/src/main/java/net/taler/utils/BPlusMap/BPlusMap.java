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


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.*;

public class BPlusMap<K extends Comparable<K>, V> implements Map<K, V> {

    // maximum number of child nodes per parent
    public final int degree;

    private LinkedList<V> leafs;

    public BPlusMap(int degree) {
        this.degree = degree;
    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(@Nullable Object o) {
        return false;
    }

    @Override
    public boolean containsValue(@Nullable Object o) {
        return false;
    }

    @Nullable
    @Override
    public V get(@Nullable Object o) {
        return null;
    }

    @Nullable
    @Override
    public V put(K k, V v) {
        return null;
    }

    @Nullable
    @Override
    public V remove(@Nullable Object o) {
        return null;
    }

    @Override
    public void putAll(@NonNull  Map<? extends K, ? extends V> map) {

    }

    @Override
    public void clear() {

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

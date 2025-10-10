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

package net.taler.utils.filterable;

import android.os.Build;
import androidx.annotation.RequiresApi;
import java.time.*;

/**
 * Wrapper class for {@link LocalDateTime} which
 * implements the {@link Filterable} interface.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class FilterableLocalDateTime
        implements Filterable<FilterableLocalDateTime> {

    /** The LocalDateTime instance being wrapped. */
    private final LocalDateTime dt;

    /**
     * Constructs a new {@code FilterableLocalDateTime} initialized to
     * the current datetime in the specified {@link ZoneId}.
     * @param tz the time zone to use for the current date-time
     */
    public FilterableLocalDateTime(ZoneId tz) {dt = LocalDateTime.now(tz);}

    /**
     * Constructs a new {@code FilterableLocalDateTime} initialized
     * to the current date-time in the system default time zone.
     */
    public FilterableLocalDateTime() {this(ZoneId.systemDefault());}

    /**
     * Constructs a new {@code FilterableLocalDateTime} wrapping
     * an existing {@link LocalDateTime}.
     * @param dateTime the {@link LocalDateTime} to wrap
     */
    public FilterableLocalDateTime(LocalDateTime dateTime) {dt = dateTime;}

    /** @return the wrapped {@link LocalDateTime} */
    public LocalDateTime unwrap() {return dt;}

    /**
     * Compares this {@code FilterableLocalDateTime} with another.
     * @param fdt the other {@code FilterableLocalDateTime} to compare against
     * @return a negative integer, zero, or a positive integer if this
     *         is less than, equal to, or greater than fdt.
     */
    @Override
    public int compareTo(FilterableLocalDateTime fdt)
    {return unwrap().compareTo(fdt.unwrap());}
}


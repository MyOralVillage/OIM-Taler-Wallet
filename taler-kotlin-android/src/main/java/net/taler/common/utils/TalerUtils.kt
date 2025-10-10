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

package net.taler.common.utils

import androidx.annotation.RequiresApi
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * General-purpose utility functions used across the Taler app.
 */
object TalerUtils {

    /**
     * Returns a localized string from a map of language codes to strings.
     *
     * Uses the system's preferred locales to find the best match in [map]. If no match is found,
     * returns [default]. The map keys should be in BCP-47 language tag format (e.g., "en", "en_US").
     *
     * @param map Map of language codes to localized strings.
     * @param default Default string to return if no match is found.
     * @return The best-matching localized string, or [default] if none matches.
     */
    @RequiresApi(26)
    fun getLocalizedString(map: Map<String, String>?, default: String): String {
        if (map == null) return default

        // create a priority list of language ranges from system locales
        val locales = LocaleListCompat.getDefault()
        val priorityList = ArrayList<Locale.LanguageRange>(locales.size())
        for (i in 0 until locales.size()) {
            locales[i]?.let { locale ->
                priorityList.add(Locale.LanguageRange(locale.toLanguageTag()))
            }
        }

        // create a list of locales available in the given map
        val availableLocales = map.keys.mapNotNull {
            if (it == "_") return@mapNotNull null
            val list = it.split("_")
            when (list.size) {
                1 -> Locale(list[0])
                2 -> Locale(list[0], list[1])
                3 -> Locale(list[0], list[1], list[2])
                else -> null
            }
        }

        val match = Locale.lookup(priorityList, availableLocales)
        return match?.toString()?.let { map[it] } ?: default
    }
}

/**
 * Returns the current system time in milliseconds, rounded to the nearest second.
 *
 * @return Current epoch time in milliseconds, rounded to seconds.
 */
fun now(): Long {
    return ((System.currentTimeMillis() + 500) / 1000) * 1000
}
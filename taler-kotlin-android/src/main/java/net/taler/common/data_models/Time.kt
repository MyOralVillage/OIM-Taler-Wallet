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

package net.taler.common.data_models

import android.annotation.SuppressLint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonTransformingSerializer
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.math.max

/**
 * Represents a point in time, internally stored in seconds.
 *
 * Special sentinel value -1 (represented in JSON as `"never"`)
 * indicates a non-occurring or infinite timestamp.
 *
 * @property ms Milliseconds representation of the timestamp. Derived from internal seconds.
 */
@Serializable
@SuppressLint("UnsafeOptInUsageError")
data class Timestamp constructor(
    /**
     * Internal representation of the timestamp in seconds.
     * Serialized as `"t_s"` and supports `"never"` as a sentinel value via [NeverSerializer].
     */
    @SerialName("t_s")
    @Serializable(NeverSerializer::class)
    private val s: Long,
) : Filterable<Timestamp> {

    companion object {
        private const val NEVER: Long = -1

        /**
         * Returns the current system time as a [Timestamp] in epoch time.
         */
        fun now(): Timestamp = fromMillis(System.currentTimeMillis())

        /**
         * Returns a sentinel [Timestamp] representing
         * a non-occurring time ("never").
         */
        fun never(): Timestamp = Timestamp(NEVER)

        /**
         * Creates a [Timestamp] from a time value in milliseconds.
         *
         * @param ms The time in milliseconds.
         * @return A [Timestamp] corresponding to the given time.
         */
        fun fromMillis(ms: Long): Timestamp = Timestamp(ms / 1000L)
    }

    /**
     * Time in milliseconds, derived from the internal seconds representation.
     */
    val ms: Long = s * 1000L

    /**
     * Calculates the difference between this timestamp and another.
     *
     * - If this is `"never"`, returns [RelativeTime.forever].
     * - If [other] is `"never"`, throws an [Error].
     * - If [other] is after this timestamp, returns a zero duration.
     *
     * @param other The [Timestamp] to subtract.
     * @return A [RelativeTime] representing the difference.
     */
    operator fun minus(other: Timestamp): RelativeTime = when {
        ms == NEVER -> RelativeTime.fromMillis(RelativeTime.FOREVER)
        other.ms == NEVER -> throw Error("Invalid argument for timestamp comparison")
        ms < other.ms -> RelativeTime.fromMillis(0)
        else -> RelativeTime.fromMillis(ms - other.ms)
    }

    /**
     * Adds a duration to this timestamp.
     *
     * - If this is `"never"`, returns `"never"`.
     * - If the duration is `"forever"`, returns `"never"`.
     *
     * @param other The [RelativeTime] to add.
     * @return A new [Timestamp] with the duration added.
     */
    operator fun plus(other: RelativeTime): Timestamp = when {
        ms == NEVER -> this
        other.ms == RelativeTime.FOREVER -> never()
        else -> fromMillis(ms + other.ms)
    }

    /**
     * Subtracts a duration from this timestamp.
     *
     * - If this is `"never"`, returns `"never"`.
     * - If the duration is `"forever"`, returns the zero timestamp.
     * - Result is clamped at 0.
     *
     * @param other The [RelativeTime] to subtract.
     * @return A new [Timestamp] with the duration subtracted.
     */
    operator fun minus(other: RelativeTime): Timestamp = when {
        ms == NEVER -> this
        other.ms == RelativeTime.FOREVER -> fromMillis(0)
        else -> fromMillis(max(0, ms - other.ms))
    }

    /**
     * Compares this timestamp to another.
     *
     * - `"never"` is considered greater than any real timestamp.
     *
     * @param other The [Timestamp] to compare to.
     * @return Comparison result: negative if less, 0 if equal, positive if greater.
     */
    override fun compareTo(other: Timestamp): Int {
        return if (ms == NEVER) {
            if (other.ms == NEVER) 0
            else 1
        } else {
            if (other.ms == NEVER) -1
            else ms.compareTo(other.ms)
        }
    }
}

/**
 * Represents a time duration, internally stored in microseconds.
 *
 * A duration can be a concrete value or a special `"forever"` value, internally represented by -1.
 *
 * @property ms Duration in milliseconds, derived from microseconds.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class RelativeTime(
    /**
     * Internal representation of the duration in microseconds.
     * Serialized as `"d_us"` and supports `"forever"` as a
     * sentinel value via [ForeverSerializer].
     */
    @SerialName("d_us")
    @Serializable(ForeverSerializer::class)
    private val us: Long,
) {
    /** Duration in milliseconds. */
    val ms: Long = us / 1000L

    companion object {
        internal const val FOREVER: Long = -1

        /**
         * Returns a sentinel [RelativeTime] representing an infinite duration ("forever").
         */
        fun forever(): RelativeTime = fromMillis(FOREVER)

        /**
         * Creates a [RelativeTime] from a duration in milliseconds.
         *
         * @param ms Duration in milliseconds.
         * @return A [RelativeTime] equivalent to the given duration.
         */
        fun fromMillis(ms: Long): RelativeTime = RelativeTime(ms / 100L)
    }
}

/**
 * Base serializer that transforms a sentinel
 * keyword (like `"never"` or `"forever"`) to -1,
 * and vice versa for JSON serialization/deserialization.
 *
 * @param keyword The keyword string to map to -1.
 */
internal abstract class MinusOneSerializer(private val keyword: String) :
    JsonTransformingSerializer<Long>(Long.serializer()) {

    /**
     * Transforms JSON input:
     * - If the JSON value matches [keyword], returns -1.
     * - Otherwise, performs normal deserialization.
     */
    override fun transformDeserialize(element: JsonElement): JsonElement {
        return if (element.jsonPrimitive.contentOrNull == keyword) {
            JsonPrimitive(-1)
        } else {
            super.transformDeserialize(element)
        }
    }

    /**
     * Transforms JSON output:
     * - If the value is -1, serializes as [keyword].
     * - Otherwise, performs normal serialization.
     */
    override fun transformSerialize(element: JsonElement): JsonElement {
        return if (element.jsonPrimitive.longOrNull == -1L) {
            JsonPrimitive(keyword)
        } else {
            element
        }
    }
}

/**
 * Custom serializer for [Timestamp] sentinel value `"never"`.
 * Internally maps to -1L.
 */
internal object NeverSerializer : MinusOneSerializer("never")

/**
 * Custom serializer for [RelativeTime] sentinel value `"forever"`.
 * Internally maps to -1L.
 */
internal object ForeverSerializer : MinusOneSerializer("forever")

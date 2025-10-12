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

@file:Suppress("unused")

package net.taler.common.utils.time

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.time.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable as KxSerializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.taler.common.utils.Filterable

/**
 * Wrapper class for [LocalDateTime] which
 * implements the [Filterable] interface.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
@KxSerializable(with = FilterableLocalDateTimeSerializer::class)
class FilterableLocalDateTime : Filterable<FilterableLocalDateTime>, Serializable {

    /** The LocalDateTime instance being wrapped. */
    private val _dt: LocalDateTime
    private val _tz: ZoneId
    private val _ms: Long // in epoch

    /**
     * Constructs a new `FilterableLocalDateTime` initialized to
     * the current datetime in the specified [ZoneId].
     * @param tz the time zone to use for the current date-time
     */
    constructor(tz: ZoneId) {
        _dt = LocalDateTime.now()
        _tz = tz
        _ms = _dt.toInstant(_tz.rules.getOffset(_dt)).toEpochMilli()
    }

    /**
     * Constructs a new `FilterableLocalDateTime` initialized
     * to the current date-time in the system default time zone.
     */
    constructor() {
        _dt = LocalDateTime.now()
        _tz = ZoneId.systemDefault()
        _ms = _dt.toInstant(_tz.rules.getOffset(_dt)).toEpochMilli()
    }

    /**
     * Constructs a new `FilterableLocalDateTime` wrapping
     * an existing [LocalDateTime].
     * @param dateTime the [LocalDateTime] to wrap
     * @param timeZone the [ZoneId] to wrap
     */
    constructor(dateTime: LocalDateTime, timeZone: ZoneId) {
        _dt = dateTime
        _tz = timeZone
        _ms = _dt.toInstant(_tz.rules.getOffset(_dt)).toEpochMilli()
    }

    /**
     * Constructs a new `FilterableLocalDateTime` from a [Timestamp].
     * @param timeStamp the [Timestamp] to wrap
     */
    constructor(timeStamp: Timestamp) {
        _tz = ZoneId.of("UTC")
        _dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp.ms), _tz)
        _ms = timeStamp.ms
    }

    /** @return the wrapped [LocalDateTime] */
    fun unwrap(): LocalDateTime = _dt

    /** @return the [ZoneId] of the wrapped [LocalDateTime] */
    fun timeZone(): ZoneId = _tz

    /** @return the epoch milliseconds of the wrapped [LocalDateTime] */
    fun epochMillis(): Long = _ms

            /**
     * Compares this `FilterableLocalDateTime` with another.
     * @param other the other `FilterableLocalDateTime` to compare against
     * @return a negative integer, zero, or a positive integer if this
     *         is less than, equal to, or greater than fdt.
     */
    override fun compareTo(other: FilterableLocalDateTime): Int =
        unwrap().compareTo(other.unwrap())

    companion object {
        /**
         * A unique identifier for this class used during the Java serialization and
         * deserialization process to verify compatibility between the sender and
         * receiver of a serialized object.
         *
         * This value should be updated only if the class definition changes in a
         * way that affects its serialized form (e.g., adding/removing non-transient
         * fields). Keeping it constant ensures consistent deserialization across
         * different versions of the class.
         */
        private const val serialVersionUID: Long = 1L
    }
}

/**
 * Kotlinx serializer for [FilterableLocalDateTime] that
 * includes the time zone in the serialized form.
 * Serializes to an ISO-8601 string with offset (e.g., "2025-10-11T14:35:00+02:00")
 * and deserializes back into a [FilterableLocalDateTime] preserving the time zone.
 */
object FilterableLocalDateTimeSerializer : KSerializer<FilterableLocalDateTime> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FilterableLocalDateTime", PrimitiveKind.STRING)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun serialize(encoder: Encoder, value: FilterableLocalDateTime) {
        val zoned = value.unwrap().atZone(value.timeZone())
        encoder.encodeString(zoned.toString()) // ISO-8601 string with offset
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun deserialize(decoder: Decoder): FilterableLocalDateTime {
        val isoString = decoder.decodeString()
        val zoned = ZonedDateTime.parse(isoString) // Parse ISO-8601 with offset
        return FilterableLocalDateTime(zoned.toLocalDateTime(), zoned.zone)
    }
}
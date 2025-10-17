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

package net.taler.database.data_models

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant

/**
 * Wrapper class for [LocalDateTime] which
 * implements the [Filterable] interface.
 */
@Serializable(with = FilterableLocalDateTimeSerializer::class)
class FilterableLocalDateTime : Filterable<FilterableLocalDateTime> {

    /** The LocalDateTime instance being wrapped. */
    private val _dt: LocalDateTime

    /** The ZoneID being wrapped.
     * If none is passed, _tz set to system default. */
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
     * @param dateTime the [LocalDateTime] to wrap.
     * @param timeZone the [ZoneId] to wrap. if none passed, defaults to sys default.
     */
    constructor(dateTime: LocalDateTime, timeZone: ZoneId?) {
        _dt = dateTime
        _tz = timeZone ?: ZoneId.systemDefault()
        _ms = _dt.toInstant(_tz.rules.getOffset(_dt)).toEpochMilli()
    }

    /**
     * Constructs a new `FilterableLocalDateTime` from a [Timestamp],
     * with the timezone set to the system default.
     * @param timeStamp the [Timestamp] to wrap
     * @param timeZone the [ZoneId] to wrap. if none passed, defaults to sys default.
     */
    constructor(timeStamp: Timestamp, timeZone: ZoneId?) {
        _tz = timeZone ?: ZoneId.systemDefault()
        _dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp.ms), _tz)
        _ms = timeStamp.ms
    }

    /**
     * Constructs a new `FilterableLocalDateTime` from a [Timestamp],
     * with the timezone set to the system default.
     * @param epochMillis the time in epoch milliseconds
     * @param timeZone the [ZoneId] to wrap. if none passed, defaults to sys default.
     */
    constructor(epochMillis: Long, timeZone: ZoneId?) {
        _tz = timeZone ?: ZoneId.systemDefault()
        _dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), _tz)
        _ms = epochMillis
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
         */
        private const val serialVersionUID: Long = 1L
    }
}

/**
 * Kotlinx serializer for [FilterableLocalDateTime] that serializes
 * the internal structure (epoch milliseconds and time zone ID) for
 * efficient and precise storage/transmission.
 *
 * Serialized format:
 * ```json
 * {
 *   "epoch_ms": 1729088100000,
 *   "zone_id": "UTC"
 * }
 * ```
 */
object FilterableLocalDateTimeSerializer : KSerializer<FilterableLocalDateTime> {

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("FilterableLocalDateTime") {
        element<Long>("epoch_ms")
        element<String>("zone_id")
    }

    override fun serialize(encoder: Encoder, value: FilterableLocalDateTime) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.epochMillis())
            encodeStringElement(descriptor, 1, value.timeZone().id)
        }
    }

    override fun deserialize(decoder: Decoder): FilterableLocalDateTime {
        return decoder.decodeStructure(descriptor) {
            var epochMs: Long? = null
            var zoneId: String? = null

            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> epochMs = decodeLongElement(descriptor, 0)
                    1 -> zoneId = decodeStringElement(descriptor, 1)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> error("Unexpected index: $index")
                }
            }

            requireNotNull(epochMs) { "Missing epoch_ms" }
            requireNotNull(zoneId) { "Missing zone_id" }

            val zone = ZoneId.of(zoneId)
            val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), zone)
            FilterableLocalDateTime(dateTime, zone)
        }
    }
}
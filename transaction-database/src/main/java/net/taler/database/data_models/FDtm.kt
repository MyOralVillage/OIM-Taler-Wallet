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
import java.time.format.DateTimeFormatter

/**

 * Represents a filterable date–time value with a fixed time zone and
 * epoch millisecond precision.
 *
 * This class acts as a lightweight, serializable wrapper around [LocalDateTime]
 * that implements the [Filterable] interface, enabling database-compatible
 * filtering, ordering, and serialization of time-based values.
 *
 * Unlike plain [LocalDateTime], instances of [FDtm] include:
 * * A stable [ZoneId] to preserve the original time context.
 * * A cached epoch millisecond value for efficient comparisons and storage.
 *
 * The class supports construction from multiple sources (epoch milliseconds,
 * [LocalDateTime], [Timestamp]) and ensures all representations remain
 * internally consistent. It is primarily used for representing transaction
 * timestamps in the GNU Taler wallet database layer.
 */
@Serializable(with = FDtmSerializer::class)
class FDtm : Filterable<FDtm> {

    /** The [LocalDateTime] instance being wrapped. */
    private val _dt: LocalDateTime

    /** The [ZoneId] of the wrapped date–time.

     * Defaults to the system time zone if not specified. */
    private val _tz: ZoneId

    /** Epoch time in milliseconds corresponding to [_dt] and [_tz]. */
    private val _ms: Long

    /**

     * Constructs a new [FDtm] initialized to the current date–time
     * in the specified [ZoneId].
     *
     * @param tz the time zone to use for the current date–time.
     */
    constructor(tz: ZoneId) {
        _dt = LocalDateTime.now()
        _tz = tz
        _ms = _dt.toInstant(_tz.rules.getOffset(_dt)).toEpochMilli()
    }

    /**

     * Constructs a new [FDtm] initialized to the current date–time
     * in the system default time zone.
     */
    constructor() {
        _dt = LocalDateTime.now()
        _tz = ZoneId.systemDefault()
        _ms = _dt.toInstant(_tz.rules.getOffset(_dt)).toEpochMilli()
    }

    /**

     * Constructs a new [FDtm] wrapping an existing [LocalDateTime].
     *
     * @param dateTime the [LocalDateTime] to wrap.
     * @param timeZone the [ZoneId] to associate with it; if `null`, uses the system default.
     */
    constructor(dateTime: LocalDateTime, timeZone: ZoneId?) {
        _dt = dateTime
        _tz = timeZone ?: ZoneId.systemDefault()
        _ms = _dt.toInstant(_tz.rules.getOffset(_dt)).toEpochMilli()
    }

    /**

     * Constructs a new [FDtm] from a [Timestamp], converting the provided epoch
     * milliseconds into a [LocalDateTime] in the given or system default [ZoneId].
     *
     * @param timeStamp the [Timestamp] to wrap.
     * @param timeZone the [ZoneId] to associate with it; if `null`, uses the system default.
     */
    constructor(timeStamp: Timestamp, timeZone: ZoneId?) {
        _tz = timeZone ?: ZoneId.systemDefault()
        _dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp.ms), _tz)
        _ms = timeStamp.ms
    }

    /**

     * Constructs a new [FDtm] from raw epoch milliseconds, using the provided or
     * system default [ZoneId].
     *
     * @param epochMillis the time in epoch milliseconds.
     * @param timeZone the [ZoneId] to associate with it; if `null`, uses the system default.
     */
    constructor(epochMillis: Long, timeZone: ZoneId?) {
        _tz = timeZone ?: ZoneId.systemDefault()
        _dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), _tz)
        _ms = epochMillis
    }

    /** Returns the wrapped [LocalDateTime] instance. */
    fun unwrap(): LocalDateTime = _dt

    /** Returns the [ZoneId] associated with this date–time. */
    fun timeZone(): ZoneId = _tz

    /** Returns the epoch millisecond representation of this date–time. */
    fun epochMillis(): Long = _ms

    /**
     * Formats a [FDtm] object into the desired format
     */
    fun fmtString(fmt: DateTimeFormatter) : String =  _dt.format(fmt)

    /**

     * Compares this [FDtm] instance with another for chronological ordering.
     *
     * @param other the other [FDtm] to compare against.
     * @return a negative integer, zero, or a positive integer if this instance
     * ```
    is less than, equal to, or greater than [other].
    ```

     */
    override fun compareTo(other: FDtm): Int =
        unwrap().compareTo(other.unwrap())

    companion object {
        /**
         * Unique version identifier used during Java serialization.
         * Ensures compatibility between serialized instances of [FDtm].
         */
        private const val serialVersionUID: Long = 1L
    }
}

/**

 * Custom [KSerializer] implementation for [FDtm].
 *
 * Serializes and deserializes [FDtm] objects as a compact JSON object containing
 * the epoch millisecond timestamp and zone identifier, ensuring high precision
 * and full round-trip fidelity.
 *
 * Example serialized form:
 * ```json
```
 * {
 * "epoch_ms": 1729088100000,
 * "zone_id": "UTC"
 * }
 * ```
```

 */
object FDtmSerializer : KSerializer<FDtm> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("FilterableLocalDateTime") {
            element<Long>("epoch_ms")
            element<String>("zone_id")
        }

    override fun serialize(encoder: Encoder, value: FDtm) {
        encoder.encodeStructure(descriptor) {
            encodeLongElement(descriptor, 0, value.epochMillis())
            encodeStringElement(descriptor, 1, value.timeZone().id)
        }
    }

    override fun deserialize(decoder: Decoder): FDtm {
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
            FDtm(dateTime, zone)
        }
    }
}

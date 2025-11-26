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

import kotlinx.serialization.Serializable
import androidx.core.graphics.toColorInt

/**
 * Interface for representing transaction purposes
 * and their associated assets
 */
@Serializable
sealed interface TranxPurp : Filterable<TranxPurp> {

    /** Comparison key (alphabetical) */
    val cmp: String

    /** group the purpose belongs to.
     * used to group together similar purposes in UI. */
    val tranxGroup: String

    /** hex colour of string */
    val colourHex: String

    /** Compares two TranxPurp alphabetically by their cmp field
     * @return
     *  - -1 if this is smaller
     *  - 0  if they are equal
     *  - +1 if this is larger */
    override fun compareTo(other: TranxPurp):
            Int = cmp.compareTo(other.cmp)

    /** @return the colour int */
    fun colourInt(): Int = colourHex.toColorInt()
}


/** School uniforms */
@Serializable
object EDUC_CLTH : TranxPurp {
    override val cmp = "EDUC_CLTH"
    override val tranxGroup = "Education"
    override val colourHex = "#ccddff"
}

/** Tuition or general schooling fees */
@Serializable
object EDUC_SCHL : TranxPurp {
    override val cmp = "EDUC_SCHL"
    override val tranxGroup = "Education"
    override val colourHex = "#ccddff"

}

/** School supplies */
@Serializable
object EDUC_SUPL : TranxPurp {
    override val cmp = "EDUC_SUPL"
    override val tranxGroup = "Education"
    override val colourHex = "#ccddff"
}

/** Phone expenses */
@Serializable
object EXPN_CELL : TranxPurp {
    override val cmp = "EXPN_CELL"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Loan repayment / debt expenses */
@Serializable
object EXPN_DEBT : TranxPurp {
    override val cmp = "EXPN_DEBT"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Farming expenses (tools, seeds, maintenance) */
@Serializable
object EXPN_FARM : TranxPurp {
    override val cmp = "EXPN_FARM"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Grocery expenses */
@Serializable
object EXPN_GRCR : TranxPurp {
    override val cmp = "EXPN_GRCR"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Market fee expenses */
@Serializable
object EXP_MRKT : TranxPurp {
    override val cmp = "EXP_MRKT"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Gas/petrol expenses */
@Serializable
object EXPN_PTRL : TranxPurp {
    override val cmp = "EXPN_PTRL"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Housing expenses */
@Serializable
object EXPN_RENT : TranxPurp {
    override val cmp = "EXPN_RENT"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Tool/equipment expenses */
@Serializable
object EXPN_TOOL : TranxPurp {
    override val cmp = "EXPN_TOOL"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Transportation-related expenses */
@Serializable
object EXPN_TRPT : TranxPurp {
    override val cmp = "EXPN_TRPT"
    override val tranxGroup = "Expenses"
    override val colourHex ="#a6749a"
}

/** Doctor/clinic visits */
@Serializable
object HLTH_DOCT : TranxPurp {
    override val cmp = "HLTH_DOCT"
    override val tranxGroup = "Healthcare"
    override val colourHex = "#74fac8"
}

/** Medicine */
@Serializable
object HLTH_MEDS : TranxPurp {
    override val cmp = "HLTH_MEDS"
    override val tranxGroup = "Healthcare"
    override val colourHex = "#74fac8"
}

/** Receive money via transaction */
@Serializable
object TRNS_RECV : TranxPurp {
    override val cmp = "TRNS_RECV"
    override val tranxGroup = "Transactions"
    override val colourHex = "#ae9ffa"
}

/** Send money via transaction */
@Serializable
object TRNS_SEND : TranxPurp {
    override val cmp = "TRNS_SEND"
    override val tranxGroup = "Transactions"
    override val colourHex = "#ae9ffa"
}

/** Electricity/power utilities */
@Serializable
object UTIL_ELEC : TranxPurp {
    override val cmp = "UTIL_ELEC"
    override val tranxGroup = "Utilities"
    override val colourHex = "#ffcff0"
}

/** Water utilities */
@Serializable
object UTIL_WATR : TranxPurp {
    override val cmp = "UTIL_WATR"
    override val tranxGroup = "Utilities"
    override val colourHex = "#ffcff0"
}

/**
 * Lookup table for all [TranxPurp] objects.
 *
 * This table allows fast access to a transaction purpose object based on its
 * [TranxPurp.cmp] string.
 *
 * Example usage:
 * ```
 * val purpose = tranxPurpLookup["EXPN_GRCR"]
 * println(purpose?.assetLabel) // prints "groceries"
 * ```
 */
val tranxPurpLookup: Map<String, TranxPurp> = mapOf(
    EDUC_CLTH.cmp to EDUC_CLTH,EDUC_SCHL.cmp to EDUC_SCHL,EDUC_SUPL.cmp to EDUC_SUPL,
    EXPN_CELL.cmp to EXPN_CELL,EXPN_DEBT.cmp to EXPN_DEBT,EXPN_FARM.cmp to EXPN_FARM,
    EXPN_GRCR.cmp to EXPN_GRCR,EXP_MRKT.cmp to EXP_MRKT,EXPN_PTRL.cmp to EXPN_PTRL,
    EXPN_RENT.cmp to EXPN_RENT,EXPN_TOOL.cmp to EXPN_TOOL,EXPN_TRPT.cmp to EXPN_TRPT,
    HLTH_DOCT.cmp to HLTH_DOCT,HLTH_MEDS.cmp to HLTH_MEDS,TRNS_RECV.cmp to TRNS_RECV,
    TRNS_SEND.cmp to TRNS_SEND,UTIL_ELEC.cmp to UTIL_ELEC,UTIL_WATR.cmp to UTIL_WATR
)
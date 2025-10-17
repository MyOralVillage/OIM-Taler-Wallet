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

/**
 * Marker interface for representing transaction purposes
 * and their associated assets
 *
 * Example usage:
 * ```
 * val iconPath = EXPN_GRCR.assetPath
 * val label = EXPN_GRCR.assetLabel
 * val inputStream = context.assets.open(iconPath)
 * ```
 */
@Serializable
sealed interface TranxPurp : Filterable<TranxPurp> {

    /** Comparison key (alphabetical) */
    val cmp: String

    /** Path to the asset for this purpose
     *
     *
     *  **NOTE** Currently unused (assets pulled directly; look into removing)
     *  */
    val assetPath: String

    /** Label of the asset
     *
     * **NOTE** Currently unused (assets pulled directly; look into removing)
     * */
    val assetLabel: String

    override fun compareTo(other: TranxPurp):
            Int = cmp.compareTo(other.cmp)

    companion object {val BASE : String = "OIM/transaction-purposes/"}
}

/** School uniforms */
@Serializable
object EDUC_CLTH : TranxPurp {
    override val cmp = "EDUC_CLTH"
    override val assetPath = TranxPurp.BASE + TranxPurp.BASE + "OIM/transaction-purposes/school_uniforms.png"
    override val assetLabel = "school_uniforms"
}

/** Tuition or general schooling fees */
@Serializable
object EDUC_SCHL : TranxPurp {
    override val cmp = "EDUC_SCHL"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/schooling.png"
    override val assetLabel = "tuition_fees"
}

/** School supplies */
@Serializable
object EDUC_SUPL : TranxPurp {
    override val cmp = "EDUC_SUPL"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/school_supplies.png"
    override val assetLabel = "school_supplies"
}

/** Phone expenses */
@Serializable
object EXPN_CELL : TranxPurp {
    override val cmp = "EXPN_CELL"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/phone.png"
    override val assetLabel = "phone_bill"
}

/** Loan repayment / debt expenses */
@Serializable
object EXPN_DEBT : TranxPurp {
    override val cmp = "EXPN_DEBT"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/loan.png"
    override val assetLabel = "debt"
}

/** Farming expenses (tools, seeds, maintenance) */
@Serializable
object EXPN_FARM : TranxPurp {
    override val cmp = "EXPN_FARM"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/farming.png"
    override val assetLabel = "farming"
}

/** Grocery expenses */
@Serializable
object EXPN_GRCR : TranxPurp {
    override val cmp = "EXPN_GRCR"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/groceries.png"
    override val assetLabel = "groceries"
}

/** Market fee expenses */
@Serializable
object EXP_MRKT : TranxPurp {
    override val cmp = "EXP_MRKT"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/market_stall.png"
    override val assetLabel = "market_fees"
}

/** Gas/petrol expenses */
@Serializable
object EXPN_PTRL : TranxPurp {
    override val cmp = "EXPN_PTRL"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/gas.png"
    override val assetLabel = "gas"
}

/** Housing expenses */
@Serializable
object EXPN_RENT : TranxPurp {
    override val cmp = "EXPN_RENT"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/housing.png"
    override val assetLabel = "housing_expenses"
}

/** Tool/equipment expenses */
@Serializable
object EXPN_TOOL : TranxPurp {
    override val cmp = "EXPN_TOOL"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/tools.png"
    override val assetLabel = "tools_and_equipment"
}

/** Transportation-related expenses */
@Serializable
object EXPN_TRPT : TranxPurp {
    override val cmp = "EXPN_TRPT"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/transportation.png"
    override val assetLabel = "transportation"
}

/** Doctor/clinic visits */
@Serializable
object HLTH_DOCT : TranxPurp {
    override val cmp = "HLTH_DOCT"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/doctor_appointment.png"
    override val assetLabel = "doctors_appointment"
}

/** Medicine */
@Serializable
object HLTH_MEDS : TranxPurp {
    override val cmp = "HLTH_MEDS"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/medicine.png"
    override val assetLabel = "medicine"
}

/** Receive money via transaction */
@Serializable
object TRNS_RECV : TranxPurp {
    override val cmp = "TRNS_RECV"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/receive.png"
    override val assetLabel = "receive_money"
}

/** Send money via transaction */
@Serializable
object TRNS_SEND : TranxPurp {
    override val cmp = "TRNS_SEND"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/send.png"
    override val assetLabel = "send_money"
}

/** Electricity/power utilities */
@Serializable
object UTIL_ELEC : TranxPurp {
    override val cmp = "UTIL_ELEC"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/electricity.png"
    override val assetLabel = "electricity_and_power"
}

/** Water utilities */
@Serializable
object UTIL_WATR : TranxPurp {
    override val cmp = "UTIL_WATR"
    override val assetPath = TranxPurp.BASE + "OIM/transaction-purposes/water.png"
    override val assetLabel = "water"
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
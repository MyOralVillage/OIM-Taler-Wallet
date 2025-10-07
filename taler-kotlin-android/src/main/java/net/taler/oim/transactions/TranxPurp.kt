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
package net.taler.oim.transactions

import kotlinx.serialization.Serializable

/**
 * Marker interface for representing transaction purposes
 * and their associated assets
 *
 * Example usage:
 * ```
 * val iconPath = EXPN_GRCR.asset_path
 * val label = EXPN_GRCR.asset_label
 * val inputStream = context.assets.open(iconPath)
 * ```
 */
@Serializable
sealed interface TranxPurp : Comparable<TranxPurp> {

    /** Comparison key (alphabetical) */
    val cmp: String

    /** Path to the asset for this purpose */
    val asset_path: String

    /** Label of the asset */
    val asset_label: String

    companion object {
        /** Base path of transaction purpose assets */
        const val BASE: String = "OIM/transaction-purposes/"
    }

    override fun compareTo(other: TranxPurp): Int = cmp.compareTo(other.cmp)
}

/** School uniforms */
@Serializable
object EDUC_CLTH : TranxPurp {
    override val cmp = "EDUC_CLTH"
    override val asset_path = TranxPurp.BASE + "school_uniforms.png"
    override val asset_label = "school_uniforms"
}

/** Tuition or general schooling fees */
@Serializable
object EDUC_SCHL : TranxPurp {
    override val cmp = "EDUC_SCHL"
    override val asset_path = TranxPurp.BASE + "schooling.png"
    override val asset_label = "tuition_fees"
}

/** School supplies */
@Serializable
object EDUC_SUPL : TranxPurp {
    override val cmp = "EDUC_SUPL"
    override val asset_path = TranxPurp.BASE + "school_supplies.png"
    override val asset_label = "school_supplies"
}

/** Phone expenses */
@Serializable
object EXPN_CELL : TranxPurp {
    override val cmp = "EXPN_CELL"
    override val asset_path = TranxPurp.BASE + "phone.png"
    override val asset_label = "phone_bill"
}

/** Loan repayment / debt expenses */
@Serializable
object EXPN_DEBT : TranxPurp {
    override val cmp = "EXPN_DEBT"
    override val asset_path = TranxPurp.BASE + "loan.png"
    override val asset_label = "debt"
}

/** Farming expenses (tools, seeds, maintenance) */
@Serializable
object EXPN_FARM : TranxPurp {
    override val cmp = "EXPN_FARM"
    override val asset_path = TranxPurp.BASE + "farming.png"
    override val asset_label = "farming"
}

/** Grocery expenses */
@Serializable
object EXPN_GRCR : TranxPurp {
    override val cmp = "EXPN_GRCR"
    override val asset_path = TranxPurp.BASE + "groceries.png"
    override val asset_label = "groceries"
}

/** Market fee expenses */
@Serializable
object EXP_MRKT : TranxPurp {
    override val cmp = "EXP_MRKT"
    override val asset_path = TranxPurp.BASE + "market_stall.png"
    override val asset_label = "market_fees"
}

/** Gas/petrol expenses */
@Serializable
object EXPN_PTRL : TranxPurp {
    override val cmp = "EXPN_PTRL"
    override val asset_path = TranxPurp.BASE + "gas.png"
    override val asset_label = "gas"
}

/** Housing expenses */
@Serializable
object EXPN_RENT : TranxPurp {
    override val cmp = "EXPN_RENT"
    override val asset_path = TranxPurp.BASE + "housing.png"
    override val asset_label = "housing_expenses"
}

/** Tool/equipment expenses */
@Serializable
object EXPN_TOOL : TranxPurp {
    override val cmp = "EXPN_TOOL"
    override val asset_path = TranxPurp.BASE + "tools.png"
    override val asset_label = "tools_and_equipment"
}

/** Transportation-related expenses */
@Serializable
object EXPN_TRPT : TranxPurp {
    override val cmp = "EXPN_TRPT"
    override val asset_path = TranxPurp.BASE + "transportation.png"
    override val asset_label = "transportation"
}

/** Doctor/clinic visits */
@Serializable
object HLTH_DOCT : TranxPurp {
    override val cmp = "HLTH_DOCT"
    override val asset_path = TranxPurp.BASE + "doctor_appointment.png"
    override val asset_label = "doctors_appointment"
}

/** Medicine */
@Serializable
object HLTH_MEDS : TranxPurp {
    override val cmp = "HLTH_MEDS"
    override val asset_path = TranxPurp.BASE + "medicine.png"
    override val asset_label = "medicine"
}

/** Receive money via transaction */
@Serializable
object TRNS_RECV : TranxPurp {
    override val cmp = "TRNS_RECV"
    override val asset_path = TranxPurp.BASE + "receive.png"
    override val asset_label = "receive_money"
}

/** Send money via transaction */
@Serializable
object TRNS_SEND : TranxPurp {
    override val cmp = "TRNS_SEND"
    override val asset_path = TranxPurp.BASE + "send.png"
    override val asset_label = "send_money"
}

/** Electricity/power utilities */
@Serializable
object UTIL_ELEC : TranxPurp {
    override val cmp = "UTIL_ELEC"
    override val asset_path = TranxPurp.BASE + "electricity.png"
    override val asset_label = "electricity_and_power"
}

/** Water utilities */
@Serializable
object UTIL_WATR : TranxPurp {
    override val cmp = "UTIL_WATR"
    override val asset_path = TranxPurp.BASE + "water.png"
    override val asset_label = "water"
}

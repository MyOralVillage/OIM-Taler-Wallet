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

package net.taler.common.utils.model

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeByteArray
import android.os.Build
import android.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.taler.common.utils.TalerUtils.getLocalizedString
import net.taler.common.transaction.Amount
import net.taler.common.utils.time.Timestamp

/**
 * Regex to match base64-encoded product images.
 * Matches JPEG or PNG images embedded as data URIs.
 */
val REGEX_PRODUCT_IMAGE = Regex("^data:image/(jpeg|png);base64,([A-Za-z0-9+/=]+)$")

/**
 * Represents the terms of a contract, including product list, deadlines, and fulfillment information.
 *
 * @property summary A short description of the contract.
 * @property summaryI18n Optional localized summaries keyed by language code.
 * @property amount The total amount of the contract.
 * @property fulfillmentUrl Optional URL for fulfilling the contract.
 * @property fulfillmentMessage Optional message related to fulfillment.
 * @property products The list of products included in this contract.
 * @property wireTransferDeadline Optional deadline for wire transfer payment.
 * @property refundDeadline Optional deadline for requesting a refund.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ContractTerms(
    val summary: String,
    @SerialName("summary_i18n")
    val summaryI18n: Map<String, String>? = null,
    val amount: Amount,
    @SerialName("fulfillment_url")
    val fulfillmentUrl: String? = null,
    @SerialName("fulfillment_message")
    val fulfillmentMessage: String? = null,
    val products: List<ContractProduct>,
    @SerialName("wire_transfer_deadline")
    val wireTransferDeadline: Timestamp? = null,
    @SerialName("refund_deadline")
    val refundDeadline: Timestamp? = null
)

/**
 * Abstract representation of a product in a contract.
 * Supports localized descriptions, pricing, images, and taxes.
 */
abstract class Product {

    /** Unique identifier for the product, if available. */
    abstract val productId: String?

    /** Default description of the product. */
    abstract val description: String

    /** Optional localized descriptions keyed by language code. */
    abstract val descriptionI18n: Map<String, String>?

    /** Optional unit price of the product. */
    abstract val price: Amount?

    /** Optional location associated with the product. */
    abstract val location: String?

    /** Optional image data in base64 format (data URI). */
    abstract val image: String?

    /** Optional set of taxes applied to this product. */
    abstract val taxes: Set<Tax>?

    /**
     * Returns the localized description if available and running on API 26+,
     * otherwise returns the default description.
     */
    val localizedDescription: String
        get() = if (Build.VERSION.SDK_INT >= 26) {
            getLocalizedString(descriptionI18n, description)
        } else {
            description
        }

    /**
     * Converts the base64-encoded image (data URI) to a [Bitmap] if possible.
     * Returns null if no image is available or decoding fails.
     */
    val imageBitmap: Bitmap?
        get() = image?.let {
            REGEX_PRODUCT_IMAGE.matchEntire(it)?.let { match ->
                match.groups[2]?.value?.let { group ->
                    val decodedString = Base64.decode(group, Base64.DEFAULT)
                    decodeByteArray(decodedString, 0, decodedString.size)
                }
            }
        }
}

/**
 * A concrete implementation of [Product] for contract-related products.
 *
 * @property quantity The number of units purchased.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ContractProduct(
    @SerialName("product_id")
    override val productId: String? = null,
    override val description: String,
    @SerialName("description_i18n")
    override val descriptionI18n: Map<String, String>? = null,
    override val price: Amount? = null,
    @SerialName("delivery_location")
    override val location: String? = null,
    override val image: String? = null,
    override val taxes: Set<Tax>? = null,
    val quantity: Int = 1,
) : Product() {

    /**
     * Calculates the total price for this product based on quantity.
     * Returns null if the unit price is not available.
     */
    val totalPrice: Amount? by lazy {
        price?.let { it * quantity }
    }
}

/**
 * Represents a tax applied to a product.
 *
 * @property name Human-readable name of the tax (e.g., "VAT").
 * @property tax Amount representing the tax value.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class Tax(
    val name: String,
    val tax: Amount
)

/**
 * Represents a merchant in a contract.
 *
 * @property name Merchant's display name.
 */
@SuppressLint("UnsafeOptInUsageError")
@Serializable
data class ContractMerchant(
    val name: String
)


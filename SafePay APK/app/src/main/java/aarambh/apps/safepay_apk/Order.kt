package aarambh.apps.safepay_apk

import aarambh.apps.safepay_apk.models.OrderCard

data class Order(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val customer: Customer,
    val payoutReleased: Boolean,
    val product: Product,
    val refundIssued: Boolean,
    val status: String,
    val updatedAt: String,
    val verificationStatus: String,
    val videoUrl: String
) {
    fun toOrderCard(): OrderCard {
        val allImages = product.images.toMutableList()
        if (!product.imageUrl.isNullOrEmpty() && !allImages.contains(product.imageUrl)) {
            allImages.add(product.imageUrl)
        }
        
        return OrderCard(
            orderId = _id,
            productName = product.name ?: "",
            amount = product.price?.toString() ?: "0",
            imageUrl = product.imageUrl ?: "",
            images = allImages,
            escrowstatus = status?.replaceFirstChar { it.uppercase() } ?: "Pending",
            verificationStatus = verificationStatus?.replaceFirstChar { it.uppercase() } ?: "Not Verified"
        )
    }
}
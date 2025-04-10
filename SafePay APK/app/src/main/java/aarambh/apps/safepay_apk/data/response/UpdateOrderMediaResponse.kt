package aarambh.apps.safepay_apk.data.response

data class UpdateOrderMediaResponse(
    val success: Boolean,
    val message: String,
    val data: OrderMediaData? = null
)

data class OrderMediaData(
    val orderId: String,
    val videoUrl: String?,
    val imageUrls: List<String>
) 
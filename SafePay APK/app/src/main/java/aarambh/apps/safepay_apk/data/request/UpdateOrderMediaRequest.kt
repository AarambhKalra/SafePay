package aarambh.apps.safepay_apk.data.request

data class UpdateOrderMediaRequest(
    val orderId: String,
    val videoUrl: String?,
    val imageUrls: List<String>
) 
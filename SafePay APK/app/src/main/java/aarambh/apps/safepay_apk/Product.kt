package aarambh.apps.safepay_apk

data class Product(
    val description: String,
    val imageUrl: String,
    val images: List<String>,
    val name: String,
    val price: Int
)
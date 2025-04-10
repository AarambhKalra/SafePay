package aarambh.apps.safepay_apk.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderCard(
    val orderId: String,
    val productName: String,
    val amount: String,
    val imageUrl: String,
    val images: List<String>,
    val escrowstatus: String = "Not Started",
    var verificationStatus: String = "Not Verified"
) : Parcelable

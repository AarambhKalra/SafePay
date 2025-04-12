package aarambh.apps.safepay_apk.api

import aarambh.apps.safepay_apk.orders
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.POST

interface OrderApiService {
    @GET("api/orders/{phoneNumber}")
    suspend fun getOrders(@Path("phoneNumber") phoneNumber: String): orders

    @PUT("api/orders/update-status/{orderId}")
    suspend fun updateOrderStatus(
        @Path("orderId") orderId: String,
        @Body status: UpdateOrderStatusRequest
    ): UpdateOrderStatusResponse

    @PUT("api/users/update-token")
    suspend fun updateFcmToken(
        @Body request: UpdateFcmTokenRequest
    ): UpdateFcmTokenResponse

    @PUT("api/orders/update-media/{orderId}")
    suspend fun updateOrderMedia(
        @Path("orderId") orderId: String,
        @Body request: UpdateOrderMediaRequest
    ): UpdateOrderMediaResponse

    @POST("api/orders/verify")
    suspend fun verifyOrder(
        @Body request: VideoUrlRequest
    ): VideoUrlResponse
}

data class UpdateOrderStatusRequest(
    val status: String
)

data class UpdateOrderStatusResponse(
    val success: Boolean,
    val message: String
)

data class UpdateFcmTokenRequest(
    val firebaseUid: String,
    val fcmToken: String
)

data class UpdateFcmTokenResponse(
    val success: Boolean,
    val message: String
)

data class UpdateOrderMediaRequest(
    val orderId: String,
    val videoUrl: String?,
    val imageUrls: List<String>
)

data class UpdateOrderMediaResponse(
    val success: Boolean,
    val message: String
)

data class VideoUrlRequest(
    val orderId: String,
    val videoUrl: String
)

data class VideoUrlResponse(
    val success: Boolean,
    val message: String
) 
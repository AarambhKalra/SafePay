package aarambh.apps.safepay_apk

import aarambh.apps.safepay_apk.api.OrderApiService
import aarambh.apps.safepay_apk.api.UpdateOrderStatusRequest
import aarambh.apps.safepay_apk.api.UpdateFcmTokenRequest
import aarambh.apps.safepay_apk.api.VideoUrlRequest
import aarambh.apps.safepay_apk.models.OrderCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class OrderRepository(private val api: OrderApiService) {
    suspend fun getOrders(userId: String): Result<List<OrderCard>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getOrders(userId)
            Log.d("OrderRepository", "API Response - Raw orders: ${response.orders}")
            val mappedOrders = response.orders.map { it.toOrderCard() }
            Log.d("OrderRepository", "Mapped OrderCards: $mappedOrders")
            Result.success(mappedOrders)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error fetching orders", e)
            Result.failure(e)
        }
    }

    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Updating order status - OrderId: $orderId, New Status: $newStatus")
            val request = UpdateOrderStatusRequest(status = newStatus.lowercase())
            val response = api.updateOrderStatus(orderId, request)
            Log.d("OrderRepository", "Update status response: $response")
            Result.success(response.success)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error updating order status", e)
            Log.e("OrderRepository", "Error details: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun verifyOrder(orderId: String, videoUrl: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Preparing to verify order - OrderId: $orderId, URL: $videoUrl")
            val request = VideoUrlRequest(orderId = orderId, videoUrl = videoUrl)
            Log.d("OrderRepository", "Request body: $request")
            val response = api.verifyOrder(request)
            Log.d("OrderRepository", "API Response: $response")
            if (response.success) {
                Log.d("OrderRepository", "Successfully verified order with video URL")
            } else {
                Log.e("OrderRepository", "Failed to verify order: ${response.message}")
            }
            Result.success(response.success)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error verifying order", e)
            Result.failure(e)
        }
    }

    suspend fun updateFcmToken(firebaseUid: String, fcmToken: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            Log.d("OrderRepository", "Updating FCM token for user: $firebaseUid")
            val request = UpdateFcmTokenRequest(firebaseUid = firebaseUid, fcmToken = fcmToken)
            val response = api.updateFcmToken(request)
            Log.d("OrderRepository", "Update FCM token response: $response")
            Result.success(response.success)
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error updating FCM token", e)
            Result.failure(e)
        }
    }
}
package aarambh.apps.safepay_apk

import retrofit2.http.GET

interface OrderApiService {
    @GET("api/orders/09999999999") // replace with actual endpoint
    suspend fun getOrders(): List<Order>
}
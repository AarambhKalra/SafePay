package aarambh.apps.safepay_apk

import retrofit2.http.GET

interface OrderApiService {
    @GET("api/orders/08595758735") // replace with actual endpoint
    suspend fun getOrders(): List<Order>
}
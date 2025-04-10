package aarambh.apps.safepay_apk

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://safepay-backend-mde8.onrender.com/" // replace accordingly

    val instance: OrderApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(OrderApiService::class.java)
    }
}
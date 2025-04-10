package aarambh.apps.safepay_apk.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://safepay-backend-mde8.onrender.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method(original.method, original.body)
            
            val request = requestBuilder.build()
            chain.proceed(request)
        }
        .connectTimeout(120, TimeUnit.SECONDS)  // Increased timeout for render.com
        .readTimeout(120, TimeUnit.SECONDS)     // Increased timeout for render.com
        .writeTimeout(120, TimeUnit.SECONDS)    // Increased timeout for render.com
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val orderApiService: OrderApiService = retrofit.create(OrderApiService::class.java)

    // Alternative base URLs to try if the main one fails
    fun updateBaseUrl(newUrl: String): OrderApiService {
        val newRetrofit = Retrofit.Builder()
            .baseUrl(newUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return newRetrofit.create(OrderApiService::class.java)
    }
} 
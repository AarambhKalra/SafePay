package aarambh.apps.safepay_apk.viewmodels

import aarambh.apps.safepay_apk.OrderRepository
import aarambh.apps.safepay_apk.api.RetrofitClient
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class OrderViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
            val repository = OrderRepository(RetrofitClient.orderApiService)
            @Suppress("UNCHECKED_CAST")
            return OrderViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 
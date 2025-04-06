package aarambh.apps.safepay_apk.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrderViewModel : ViewModel() {

    private val _orderStatusMap = MutableLiveData<Map<String, String>>() // orderId -> status
    val orderStatusMap: LiveData<Map<String, String>> = _orderStatusMap

    init {
        _orderStatusMap.value = mutableMapOf()
    }

    fun updateOrderStatus(orderId: String, status: String) {
        val currentMap = _orderStatusMap.value?.toMutableMap() ?: mutableMapOf()
        currentMap[orderId] = status
        _orderStatusMap.value = currentMap
    }

    fun getOrderStatus(orderId: String): String? {
        return _orderStatusMap.value?.get(orderId)
    }
}

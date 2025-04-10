package aarambh.apps.safepay_apk.viewmodels

import aarambh.apps.safepay_apk.OrderRepository
import aarambh.apps.safepay_apk.models.OrderCard
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import android.util.Log

class OrderViewModel(private val repository: OrderRepository) : ViewModel() {

    private val TAG = "SafePay_Order"

    private val _orders = MutableStateFlow<List<OrderCard>>(emptyList())
    val orders: StateFlow<List<OrderCard>> = _orders

    private val _selectedOrder = MutableStateFlow<OrderCard?>(null)
    val selectedOrder: StateFlow<OrderCard?> = _selectedOrder

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // For scan upload status handling
    private val _scanUploadStatus = MutableStateFlow<Boolean?>(null)
    val scanUploadStatus: StateFlow<Boolean?> = _scanUploadStatus

    // For storing video URL
    private val _currentVideoUrl = MutableStateFlow<String?>(null)
    val currentVideoUrl: StateFlow<String?> = _currentVideoUrl

    fun fetchOrders(phoneNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.getOrders(phoneNumber).fold(
                    onSuccess = { ordersList ->
                        _orders.value = ordersList
                        _error.value = null
                    },
                    onFailure = { exception ->
                        _error.value = when (exception) {
                            is UnknownHostException, is ConnectException -> "No internet connection"
                            is SocketTimeoutException -> "Connection timed out"
                            else -> "Error: ${exception.message}"
                        }
                    }
                )
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectOrder(orderId: String) {
        Log.d(TAG, "Trying to select order with ID: $orderId")
        Log.d(TAG, "Available orders: ${_orders.value.map { "id: ${it.orderId}" }}")
        val order = _orders.value.find { it.orderId == orderId }
        if (order == null) {
            Log.d(TAG, "Order not found with ID: $orderId")
        } else {
            Log.d(TAG, "Found order: $order")
        }
        _selectedOrder.value = order
        // Reset video URL when selecting a new order
        _currentVideoUrl.value = null
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        _orders.update { currentOrders ->
            currentOrders.map {
                if (it.orderId == orderId) it.copy(escrowstatus = newStatus) else it
            }
        }

        _selectedOrder.update { selected ->
            selected?.takeIf { it.orderId == orderId }?.copy(escrowstatus = newStatus) ?: selected
        }
    }

    fun getEscrowOrders(): List<OrderCard> {
        return _orders.value.filter { it.escrowstatus == "In Escrow" }
    }

    fun calculateEscrowTotal(): Int {
        return getEscrowOrders()
            .mapNotNull { it.amount.toIntOrNull() }
            .sum()
    }

    fun isOrderPaid(orderId: String): Boolean {
        return _orders.value.any { it.orderId == orderId && it.escrowstatus == "In Escrow" }
    }

    fun markOrderAsPaid(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "Attempting to mark order as paid: $orderId")
                repository.updateOrderStatus(orderId, "in escrow").fold(
                    onSuccess = { success ->
                        if (success) {
                            updateOrderStatus(orderId, "In Escrow")
                            _error.value = null
                            Log.d(TAG, "Successfully marked order as paid: $orderId")
                        } else {
                            _error.value = "Failed to update order status"
                            Log.e(TAG, "Failed to mark order as paid: $orderId")
                        }
                    },
                    onFailure = { exception ->
                        _error.value = "Error: ${exception.message}"
                        Log.e(TAG, "Error marking order as paid: $orderId", exception)
                    }
                )
            } catch (e: Exception) {
                _error.value = "Unexpected error: ${e.message}"
                Log.e(TAG, "Unexpected error marking order as paid: $orderId", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // For scan upload UI logic
    fun setScanUploadStatus(success: Boolean) {
        _scanUploadStatus.value = success
    }

    fun resetScanUploadStatus() {
        _scanUploadStatus.value = null
    }

    fun updateVerificationStatus(orderId: String, newStatus: String) {
        _orders.update { currentOrders ->
            currentOrders.map {
                if (it.orderId == orderId) it.copy(verificationStatus = newStatus) else it
            }
        }

        _selectedOrder.update { selected ->
            selected?.takeIf { it.orderId == orderId }?.copy(verificationStatus = newStatus) ?: selected
        }
    }

    // Video URL handling functions
    fun setVideoUrl(url: String) {
        _currentVideoUrl.value = url
    }

    fun clearMediaUrls() {
        _currentVideoUrl.value = null
    }
}

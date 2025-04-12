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

    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess

    // For scan upload status handling
    private val _scanUploadStatus = MutableStateFlow<Boolean?>(null)
    val scanUploadStatus: StateFlow<Boolean?> = _scanUploadStatus

    // For storing video URL
    private val _currentVideoUrl = MutableStateFlow<String?>(null)
    val currentVideoUrl: StateFlow<String?> = _currentVideoUrl

    // Track current user's phone number
    private var currentUserPhone: String? = null

    fun clearAllData() {
        _orders.value = emptyList()
        _selectedOrder.value = null
        _currentVideoUrl.value = null
        currentUserPhone = null
        _error.value = null
        Log.d(TAG, "Cleared all order data")
    }

    fun fetchOrders(phoneNumber: String) {
        // If phone number changed, clear all data
        if (currentUserPhone != phoneNumber) {
            clearAllData()
        }
        
        currentUserPhone = phoneNumber
        Log.d(TAG, "Fetching orders for phone number: $phoneNumber")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                repository.getOrders(phoneNumber).fold(
                    onSuccess = { ordersList ->
                        if (phoneNumber == currentUserPhone) {
                            Log.d(TAG, "Received ${ordersList.size} orders for $phoneNumber")
                            ordersList.forEach { order ->
                                Log.d(TAG, "Order: ${order.orderId}, Product: ${order.productName}")
                            }
                            _orders.value = ordersList
                            _error.value = null
                        } else {
                            Log.d(TAG, "Ignoring orders response for old phone number: $phoneNumber, current: $currentUserPhone")
                        }
                    },
                    onFailure = { exception ->
                        if (phoneNumber == currentUserPhone) {
                            val errorMsg = when (exception) {
                                is UnknownHostException, is ConnectException -> "No internet connection"
                                is SocketTimeoutException -> "Connection timed out"
                                else -> "Error: ${exception.message}"
                            }
                            Log.e(TAG, "Error fetching orders: $errorMsg", exception)
                            _error.value = errorMsg
                        }
                    }
                )
            } catch (e: Exception) {
                if (phoneNumber == currentUserPhone) {
                    Log.e(TAG, "Unexpected error fetching orders", e)
                    _error.value = "Unexpected error: ${e.message}"
                }
            } finally {
                if (phoneNumber == currentUserPhone) {
                    _isLoading.value = false
                }
            }
        }
    }

    fun selectOrder(orderId: String) {
        Log.d(TAG, "Trying to select order with ID: $orderId")
        val order = _orders.value.find { it.orderId == orderId }
        _selectedOrder.value = order
        // Reset video URL when selecting a new order
        _currentVideoUrl.value = null
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateOrderStatus(orderId, newStatus).fold(
                    onSuccess = { success ->
                        if (success) {
                            _orders.update { currentOrders ->
                                currentOrders.map {
                                    if (it.orderId == orderId) it.copy(escrowstatus = newStatus) else it
                                }
                            }
                            _selectedOrder.update { selected ->
                                selected?.takeIf { it.orderId == orderId }?.copy(escrowstatus = newStatus) ?: selected
                            }
                            _error.value = null
                        } else {
                            _error.value = "Failed to update order status"
                        }
                    },
                    onFailure = { exception ->
                        _error.value = "Error: ${exception.message}"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Error updating status: ${e.message}"
            }
        }
    }

    fun getEscrowOrders(): List<OrderCard> {
        val escrowOrders = _orders.value.filter { order ->
            // Case insensitive comparison for escrow status
            order.escrowstatus.equals("escrow", ignoreCase = true) ||
            order.escrowstatus.equals("in escrow", ignoreCase = true)
        }
        Log.d(TAG, "Total orders: ${_orders.value.size}")
        Log.d(TAG, "All order statuses: ${_orders.value.map { "${it.orderId}: ${it.escrowstatus}" }}")
        Log.d(TAG, "Found ${escrowOrders.size} orders in escrow")
        return escrowOrders
    }

    fun calculateEscrowTotal(): Int {
        val escrowOrders = getEscrowOrders()
        val total = escrowOrders.mapNotNull { order ->
            try {
                order.amount.toInt()
            } catch (e: NumberFormatException) {
                Log.e(TAG, "Error parsing amount for order ${order.orderId}: ${order.amount}", e)
                null
            }
        }.sum()
        Log.d(TAG, "Calculated escrow total: ₹$total from ${escrowOrders.size} orders")
        return total
    }

    fun isOrderPaid(orderId: String): Boolean {
        return _orders.value.any { order -> 
            order.orderId == orderId && 
            (order.escrowstatus.equals("escrow", ignoreCase = true) ||
             order.escrowstatus.equals("in escrow", ignoreCase = true))
        }
    }

    fun markOrderAsPaid(orderId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d(TAG, "Attempting to mark order as paid: $orderId")
                repository.updateOrderStatus(orderId, "escrow").fold(
                    onSuccess = { success ->
                        if (success) {
                            updateOrderStatus(orderId, "escrow")
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
        // When we have both order and video URL, send to API
        _selectedOrder.value?.let { order ->
            verifyOrder(order.orderId, url)
        }
    }

    fun verifyOrder(orderId: String, videoUrl: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Sending video URL to API - OrderId: $orderId, URL: $videoUrl")
                _isLoading.value = true
                _error.value = null
                
                repository.verifyOrder(orderId, videoUrl).fold(
                    onSuccess = { success ->
                        if (success) {
                            Log.d(TAG, "Successfully sent video URL to API")
                            _error.value = null
                            setScanUploadStatus(true)
                            updateVerificationStatus(orderId, "Verifying")
                            _currentVideoUrl.value = videoUrl
                        } else {
                            val errorMsg = "Failed to send video URL to API"
                            Log.e(TAG, errorMsg)
                            _error.value = errorMsg
                            setScanUploadStatus(false)
                        }
                    },
                    onFailure = { exception ->
                        val errorMsg = "Error sending video URL: ${exception.message}"
                        Log.e(TAG, errorMsg, exception)
                        _error.value = errorMsg
                        setScanUploadStatus(false)
                    }
                )
            } catch (e: Exception) {
                val errorMsg = "Unexpected error sending video URL: ${e.message}"
                Log.e(TAG, errorMsg, e)
                _error.value = errorMsg
                setScanUploadStatus(false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setScanUploadStatus(success: Boolean) {
        _scanUploadStatus.value = success
    }

    fun resetScanUploadStatus() {
        _scanUploadStatus.value = null
    }

    fun clearMediaUrls() {
        _currentVideoUrl.value = null
    }

    override fun onCleared() {
        super.onCleared()
        currentUserPhone = null
        _orders.value = emptyList()
        _selectedOrder.value = null
        _currentVideoUrl.value = null
    }
}


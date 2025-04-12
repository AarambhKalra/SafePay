package aarambh.apps.safepay_apk.users

import aarambh.apps.safepay_apk.R
import aarambh.apps.safepay_apk.adapters.OrderAdapter
import aarambh.apps.safepay_apk.viewmodels.OrderViewModel
import aarambh.apps.safepay_apk.viewmodels.OrderViewModelFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import android.util.Log

class HomeFragment : Fragment() {
    private val TAG = "SafePay_Home"
    private val viewModel: OrderViewModel by activityViewModels { OrderViewModelFactory() }
    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStatusBarColor()
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        orderAdapter = OrderAdapter { orderId ->
            viewModel.selectOrder(orderId)
            findNavController().navigate(R.id.action_homeFragment_to_orderInfoFragment)
        }
        recyclerView.adapter = orderAdapter

        setupObservers(view)
        
        // Get user's phone number and fetch orders
        getUserPhoneAndFetchOrders()
        
        return view
    }

    private fun getUserPhoneAndFetchOrders() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val phoneNumber = currentUser.phoneNumber
            if (!phoneNumber.isNullOrEmpty()) {
                // Format phone number: remove +91 prefix and any leading zeros
                val formattedNumber = phoneNumber
                    .replace("+91", "") // Remove country code if present
                    .replaceFirst("^0+", "") // Remove leading zeros
                
                Log.d(TAG, "Original phone: $phoneNumber")
                Log.d(TAG, "Formatted phone: $formattedNumber")
                
                if (formattedNumber.length == 10) {
                    viewModel.fetchOrders(formattedNumber)
                } else {
                    Log.e(TAG, "Invalid phone number format: $formattedNumber")
                    Toast.makeText(requireContext(), "Invalid phone number format", Toast.LENGTH_LONG).show()
                }
            } else {
                Log.e(TAG, "No phone number found for user")
                Toast.makeText(requireContext(), "No phone number found", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.e(TAG, "No user logged in")
            Toast.makeText(requireContext(), "Please log in first", Toast.LENGTH_LONG).show()
            // Navigate to login screen or handle accordingly
        }
    }

    private fun setupObservers(view: View) {
        val noOrdersText = view.findViewById<TextView>(R.id.noOrdersText)
        val orderCountText = view.findViewById<TextView>(R.id.orderCountText)
        val escrowAmountText = view.findViewById<TextView>(R.id.escrowAmountText)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe loading state
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    }
                }

                // Observe error state
                launch {
                    viewModel.error.collect { errorMessage ->
                        errorMessage?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                // Observe orders
                launch {
                    viewModel.orders.collect { orders ->
                        orderAdapter.submitList(orders)

                        if (orders.isEmpty()) {
                            recyclerView.visibility = View.GONE
                            noOrdersText.visibility = View.VISIBLE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            noOrdersText.visibility = View.GONE
                        }

                        val escrowOrders = viewModel.getEscrowOrders()
                        orderCountText.text = "${escrowOrders.size} Orders in Escrow"
                        escrowAmountText.text = "₹${viewModel.calculateEscrowTotal()} Total"
                    }
                }
            }
        }
    }

    private fun setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requireActivity().window.statusBarColor = Color.WHITE
            requireActivity().window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

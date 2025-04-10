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

class HomeFragment : Fragment() {

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
        
        // Fetch orders with the specific phone number
        viewModel.fetchOrders("08595758735")
        
        return view
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

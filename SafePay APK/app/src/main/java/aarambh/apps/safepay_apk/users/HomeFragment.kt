package aarambh.apps.safepay_apk.users

import aarambh.apps.safepay_apk.R
import aarambh.apps.safepay_apk.adapters.OrderAdapter
import aarambh.apps.safepay_apk.models.OrderCard
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {



    private lateinit var recyclerView: RecyclerView
    private lateinit var orderAdapter: OrderAdapter
    private val orderList = mutableListOf<OrderCard>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setStatusBarColor()
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Sample Data
        orderList.clear()
        orderList.addAll(
            listOf(
                OrderCard("ORD001", "Nike Slides", "In Escrow", "₹1200", "https://example.com/image1.jpg", "In Escrow"),
                OrderCard("ORD002", "Adidas Sneakers", "Released", "₹3200", "https://example.com/image2.jpg", "Released"),
                OrderCard("ORD003", "Puma Running Shoes", "Refunded", "₹2500", "https://example.com/image3.jpg", "Refunded")
            )
        )

        // Adapter with orderId click listener
        orderAdapter = OrderAdapter(orderList) { orderId ->
            val bundle = Bundle().apply {
                putString("orderId", orderId)
            }
            findNavController().navigate(R.id.action_homeFragment_to_orderInfoFragment, bundle)
        }
        recyclerView.adapter = orderAdapter

        // No orders message
        val noOrdersText = view.findViewById<TextView>(R.id.noOrdersText)
        if (orderList.isEmpty()) {
            recyclerView.visibility = View.GONE
            noOrdersText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            noOrdersText.visibility = View.GONE
        }

        // Escrow summary
        val escrowOrders = orderList.filter { it.status == "In Escrow" }
        val orderCountText = view.findViewById<TextView>(R.id.orderCountText)
        val escrowAmountText = view.findViewById<TextView>(R.id.escrowAmountText)

        orderCountText.text = "${escrowOrders.size} Orders in Escrow"
        escrowAmountText.text = "₹${calculateTotal(escrowOrders)} Total"

        return view
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            statusBarColor = Color.TRANSPARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    private fun calculateTotal(orders: List<OrderCard>): Int {
        return orders.sumOf {
            it.amount.replace("₹", "").toIntOrNull() ?: 0
        }
    }
}

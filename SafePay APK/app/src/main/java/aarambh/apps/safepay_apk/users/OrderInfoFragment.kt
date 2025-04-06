package aarambh.apps.safepay_apk.users

import aarambh.apps.safepay_apk.R
import aarambh.apps.safepay_apk.databinding.FragmentOrderInfoBinding
import aarambh.apps.safepay_apk.models.OrderCard
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide

class OrderInfoFragment : Fragment() {

    private var _binding: FragmentOrderInfoBinding? = null
    private val binding get() = _binding!!

    private lateinit var order: OrderCard

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val orderId = arguments?.getString("orderId")
        if (orderId == null) {
            // Handle missing ID gracefully
            binding.productNameText.text = "Order not found"
            return
        }

        // Simulated fetch — replace with real data source (ViewModel, Repository, etc.)
        val mockOrderList = listOf(
            OrderCard("ORD001", "Nike Slides", "Shipped", "₹1200", "https://example.com/image1.jpg", "In Escrow"),
            OrderCard("ORD002", "Adidas Sneakers", "Delivered", "₹3200", "https://example.com/image2.jpg", "Released"),
            OrderCard("ORD003", "Puma Running Shoes", "Cancelled", "₹2500", "https://example.com/image3.jpg", "Refunded")
        )

        order = mockOrderList.find { it.orderId == orderId } ?: run {
            binding.productNameText.text = "Order not found"
            return
        }

        // Populate UI
        binding.productNameText.text = order.productName
        binding.orderIdText.text = "Order ID: ${order.orderId}"
        binding.orderStatusText.text = "Order Status: ${order.orderStatus}"
        binding.amountText.text = "Amount: ${order.amount}"
        binding.statusText.text = "Escrow Status: ${order.status}"

        Glide.with(requireContext())
            .load(order.imageUrl)
            .placeholder(R.drawable.icon)
            .into(binding.productImage)

        // Show scan button only if In Escrow
        binding.scanVerifyBtn.visibility = if (order.status == "In Escrow") View.VISIBLE else View.GONE

        // Back button
        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Scan button action — placeholder
        binding.scanVerifyBtn.setOnClickListener {
            // TODO: Add actual scan verification logic
        }

        binding.scanVerifyBtn.setOnClickListener {
            findNavController().navigate(R.id.action_orderInfoFragment_to_scanFragment)
        }

        parentFragmentManager.setFragmentResultListener("upload_result", viewLifecycleOwner) { _, bundle ->
            val success = bundle.getBoolean("upload_success", false)
            if (success) {
                binding.scanVerifyBtn.isEnabled = false
                binding.scanVerifyBtn.text = "In Progress..."
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

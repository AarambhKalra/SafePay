package aarambh.apps.safepay_apk.users

import aarambh.apps.safepay_apk.R
import aarambh.apps.safepay_apk.adapters.ImageSliderAdapter
import aarambh.apps.safepay_apk.databinding.FragmentOrderInfoBinding
import aarambh.apps.safepay_apk.viewmodels.OrderViewModel
import aarambh.apps.safepay_apk.viewmodels.OrderViewModelFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class OrderInfoFragment : Fragment() {

    private var _binding: FragmentOrderInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderViewModel by activityViewModels { OrderViewModelFactory() }
    private lateinit var imageSliderAdapter: ImageSliderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageSliderAdapter = ImageSliderAdapter()
        binding.imageSlider.adapter = imageSliderAdapter

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedOrder.collect { order ->
                    Log.d("SafePay_Order", "OrderInfoFragment received order: $order")
                    order?.let {
                        binding.productNameText.text = it.productName
                        binding.orderIdText.text = "Order ID: ${it.orderId}"
                        binding.amountText.text = "Amount: ₹${it.amount}"
                        binding.statusText.text = "Status: ${it.escrowstatus}"

                        // Set images to the slider
                        val images = if (it.images.isNotEmpty()) {
                            it.images
                        } else if (it.imageUrl.isNotEmpty()) {
                            listOf(it.imageUrl)
                        } else {
                            emptyList()
                        }
                        imageSliderAdapter.setImages(images)

                        // Hide payment button as we're skipping payment
                        binding.testPaymentButton.visibility = View.GONE

                        // Always show verify button
                        binding.scanVerifyBtn.visibility = View.VISIBLE
                        binding.scanVerifyBtn.apply {
                            text = when (it.verificationStatus) {
                                "Verifying" -> "Verifying"
                                else -> "Scan & Verify"
                            }
                            isEnabled = it.verificationStatus != "Verifying"
                        }

                    } ?: run {
                        binding.productNameText.text = "Order not found"
                        Toast.makeText(context, "Order not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.backToHomeBtn.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.scanVerifyBtn.setOnClickListener {
            findNavController().navigate(R.id.action_orderInfoFragment_to_scanFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

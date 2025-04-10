package aarambh.apps.safepay_apk.adapters

import aarambh.apps.safepay_apk.R
import aarambh.apps.safepay_apk.models.OrderCard
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OrderAdapter(
    private val onItemClick: (String) -> Unit
) : ListAdapter<OrderCard, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    private val imageSliderAdapters = mutableMapOf<String, ImageSliderAdapter>()
    private val tabMediators = mutableMapOf<String, TabLayoutMediator>()

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val escrowStatus: TextView = view.findViewById(R.id.escrowStatus)
        val amount: TextView = view.findViewById(R.id.amount)
        val imageSlider: ViewPager2 = view.findViewById(R.id.imageSlider)
        val tabLayout: TabLayout = view.findViewById(R.id.imageSliderIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.productName.text = order.productName
        holder.escrowStatus.text = order.escrowstatus
        holder.amount.text = "₹${order.amount}"

        // Set background color based on escrow status
        when (order.escrowstatus.lowercase()) {
            "in escrow" -> holder.escrowStatus.setBackgroundResource(R.drawable.status_escrow)
            "released" -> holder.escrowStatus.setBackgroundResource(R.drawable.status_released)
            "refunded" -> holder.escrowStatus.setBackgroundResource(R.drawable.status_refunded)
            else -> holder.escrowStatus.setBackgroundResource(R.drawable.status_default)
        }

        // Handle image slider
        val imageAdapter = imageSliderAdapters.getOrPut(order.orderId) { ImageSliderAdapter() }
        holder.imageSlider.adapter = imageAdapter
        
        // Combine all available images
        val allImages = order.images.toMutableList()
        if (order.imageUrl.isNotEmpty() && !allImages.contains(order.imageUrl)) {
            allImages.add(order.imageUrl)
        }
        imageAdapter.setImages(allImages)

        // Set up dot indicators
        tabMediators[order.orderId]?.detach()
        if (allImages.isNotEmpty()) {
            tabMediators[order.orderId] = TabLayoutMediator(holder.tabLayout, holder.imageSlider) { _, _ -> }
                .also { it.attach() }
            holder.tabLayout.visibility = View.VISIBLE
        } else {
            holder.tabLayout.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(order.orderId)
        }
    }

    override fun onViewRecycled(holder: OrderViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val order = getItem(position)
            tabMediators[order.orderId]?.detach()
        }
    }

    private class OrderDiffCallback : DiffUtil.ItemCallback<OrderCard>() {
        override fun areItemsTheSame(oldItem: OrderCard, newItem: OrderCard): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: OrderCard, newItem: OrderCard): Boolean {
            return oldItem == newItem
        }
    }
}

package aarambh.apps.safepay_apk.adapters

import aarambh.apps.safepay_apk.R
import aarambh.apps.safepay_apk.models.OrderCard
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class OrderAdapter(
    private val orders: List<OrderCard>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val orderStatus: TextView = view.findViewById(R.id.orderStatus)
        val amount: TextView = view.findViewById(R.id.amount)
        val productImage: ImageView = view.findViewById(R.id.productImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_card, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.productName.text = order.productName
        holder.orderStatus.text = order.orderStatus
        holder.amount.text = order.amount

        // Dynamic status background color
        when (order.orderStatus.lowercase()) {
            "in escrow" -> holder.orderStatus.setBackgroundResource(R.drawable.status_escrow)
            "released" -> holder.orderStatus.setBackgroundResource(R.drawable.status_released)
            "refunded" -> holder.orderStatus.setBackgroundResource(R.drawable.status_refunded)
            else -> holder.orderStatus.setBackgroundResource(R.drawable.status_default)
        }

        // Load image with Glide
        Glide.with(holder.itemView.context)
            .load(order.imageUrl)
            .placeholder(R.drawable.icon)
            .into(holder.productImage)

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(order.orderId)
        }

    }

    override fun getItemCount(): Int = orders.size
}

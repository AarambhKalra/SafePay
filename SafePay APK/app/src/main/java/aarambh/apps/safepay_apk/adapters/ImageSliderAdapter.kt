package aarambh.apps.safepay_apk.adapters

import aarambh.apps.safepay_apk.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ImageSliderAdapter : RecyclerView.Adapter<ImageSliderAdapter.ImageViewHolder>() {
    
    private var images: List<String> = emptyList()

    fun setImages(newImages: List<String>) {
        images = newImages
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image_slider, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageUrl = images[position]
        Glide.with(holder.itemView.context)
            .load(imageUrl)
            .placeholder(R.drawable.icon)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }
} 
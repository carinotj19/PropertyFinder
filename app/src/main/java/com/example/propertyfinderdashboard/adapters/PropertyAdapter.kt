package com.example.propertyfinderdashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.models.PropertyModel
import com.squareup.picasso.Picasso

class PropertyAdapter(private var items: List<PropertyModel>,  private val listener: OnItemClickListener) :
    RecyclerView.Adapter<PropertyAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val propertyImage: ImageView = itemView.findViewById(R.id.property_image)
        val propertyName: TextView = itemView.findViewById(R.id.property_name)
        val propertyLocation: TextView = itemView.findViewById(R.id.property_location)
        val propertyPrice: TextView = itemView.findViewById(R.id.property_price)
        val propertyRating: TextView = itemView.findViewById(R.id.property_rating)
        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION){
                listener.onItemClick(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.property_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = items[position]

        val formattedPrice = "₱${String.format("%.2f", currentItem.price)}"

        Picasso.get().load(currentItem.propertyImage).into(holder.propertyImage)
        holder.propertyName.text = currentItem.propertyName
        holder.propertyLocation.text = currentItem.location
        holder.propertyPrice.text = formattedPrice
        holder.propertyRating.text = currentItem.rating.toString()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newItems: List<PropertyModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClick(position: Int)
    }
}
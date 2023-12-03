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
class PendingPropertyAdapter(private var items: List<PropertyModel>, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<PendingPropertyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val propertyImage: ImageView = itemView.findViewById(R.id.property_image)
        val propertyName: TextView = itemView.findViewById(R.id.property_name)
        val propertyLocation: TextView = itemView.findViewById(R.id.property_location)
        val propertyPrice: TextView = itemView.findViewById(R.id.property_price)
        val propertyRating: TextView = itemView.findViewById(R.id.property_rating)

        val trashButton: ImageView = itemView.findViewById(R.id.trash)
        val checkButton: ImageView = itemView.findViewById(R.id.check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.pending_property_layout, parent, false)
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

        holder.trashButton.setOnClickListener {
            itemClickListener.onDeleteButtonClick(currentItem)
        }

        holder.checkButton.setOnClickListener {
            itemClickListener.onCheckButtonClick(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateData(newItems: List<PropertyModel>) {
        items = newItems
        notifyDataSetChanged()
    }

    interface OnItemClickListener {
        fun onDeleteButtonClick(item: PropertyModel)
        fun onCheckButtonClick(item: PropertyModel)
    }
}
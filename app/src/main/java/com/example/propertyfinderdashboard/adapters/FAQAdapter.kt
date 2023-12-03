package com.example.propertyfinderdashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.propertyfinderdashboard.R
import com.example.propertyfinderdashboard.models.FAQModel

class FAQAdapter(private val faqList: List<FAQModel>) : RecyclerView.Adapter<FAQAdapter.FAQViewHolder>() {

    inner class FAQViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        val answerTextView: TextView = itemView.findViewById(R.id.answerTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_faq, parent, false)
        return FAQViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: FAQViewHolder, position: Int) {
        val currentFAQ = faqList[position]

        holder.questionTextView.text = currentFAQ.question
        holder.answerTextView.text = currentFAQ.answer
    }

    override fun getItemCount(): Int {
        return faqList.size
    }
}

package com.ssl.smarttaskreminder.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssl.smarttaskreminder.databinding.ItemAnalyticsUserBinding

/**
 * Adapter for analytics ranking lists (best performers, worst performers, overdue teams).
 * Accepts a list of Pair<String, Int> — (name, count).
 */
class AnalyticsItemAdapter(private val isPercentage: Boolean = false) :
    ListAdapter<Pair<String, Any>, AnalyticsItemAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemAnalyticsUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Pair<String, Any>, position: Int) {
            binding.tvRank.text  = "#${position + 1}"
            binding.tvName.text  = item.first
            if (isPercentage) {
                val value = item.second as? Float ?: 0f
                binding.tvCount.text = "%.1f%%".format(value)
            } else {
                binding.tvCount.text = item.second.toString()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemAnalyticsUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position), position)

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Pair<String, Any>>() {
            override fun areItemsTheSame(old: Pair<String, Any>, new: Pair<String, Any>) =
                old.first == new.first
            override fun areContentsTheSame(old: Pair<String, Any>, new: Pair<String, Any>) =
                old == new
        }
    }
}

package com.ssl.smarttaskreminder.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssl.smarttaskreminder.data.model.Manager
import com.ssl.smarttaskreminder.databinding.ItemManagerBinding

class ManagerAdapter(
    private val onEditClick: (Manager) -> Unit,
    private val onDeleteClick: (Manager) -> Unit
) : ListAdapter<Manager, ManagerAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemManagerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(manager: Manager) {
            binding.tvManagerName.text  = manager.name
            binding.tvManagerEmail.text = manager.email
            binding.tvManagerId.text    = "MID: ${manager.mid}"

            binding.btnEdit.setOnClickListener { onEditClick(manager) }
            binding.btnDelete.setOnClickListener { onDeleteClick(manager) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemManagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Manager>() {
            override fun areItemsTheSame(old: Manager, new: Manager) = old.documentId == new.documentId
            override fun areContentsTheSame(old: Manager, new: Manager) = old == new
        }
    }
}

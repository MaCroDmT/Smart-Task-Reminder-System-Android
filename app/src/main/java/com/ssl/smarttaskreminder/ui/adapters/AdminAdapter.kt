package com.ssl.smarttaskreminder.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssl.smarttaskreminder.data.model.Admin
import com.ssl.smarttaskreminder.databinding.ItemUserBinding

class AdminAdapter(
    private val onEditClick: (Admin) -> Unit,
    private val onDeleteClick: (Admin) -> Unit
) : ListAdapter<Admin, AdminAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(admin: Admin) {
            binding.tvUserName.text  = admin.name
            binding.tvUserEmail.text = admin.email
            binding.tvUserId.text    = "AID: ${admin.aid}"
            binding.tvManagerId.visibility = View.GONE  // Admins don't have a manager ID

            binding.btnEdit.setOnClickListener { onEditClick(admin) }
            binding.btnDelete.setOnClickListener { onDeleteClick(admin) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Admin>() {
            override fun areItemsTheSame(old: Admin, new: Admin) = old.documentId == new.documentId
            override fun areContentsTheSame(old: Admin, new: Admin) = old == new
        }
    }
}

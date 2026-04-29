package com.ssl.smarttaskreminder.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssl.smarttaskreminder.data.model.User
import com.ssl.smarttaskreminder.databinding.ItemUserBinding

class UserAdapter(
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, UserAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            binding.tvUserName.text  = user.name
            binding.tvUserEmail.text = user.email
            binding.tvUserId.text    = "UID: ${user.uid}"
            binding.tvManagerId.text = "Manager ID: ${user.managerId}"

            binding.btnEdit.setOnClickListener { onEditClick(user) }
            binding.btnDelete.setOnClickListener { onDeleteClick(user) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(old: User, new: User) = old.documentId == new.documentId
            override fun areContentsTheSame(old: User, new: User) = old == new
        }
    }
}

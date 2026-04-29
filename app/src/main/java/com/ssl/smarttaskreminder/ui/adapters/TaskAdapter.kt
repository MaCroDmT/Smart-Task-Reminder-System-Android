package com.ssl.smarttaskreminder.ui.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.R
import com.ssl.smarttaskreminder.data.model.Task
import com.ssl.smarttaskreminder.databinding.ItemTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val onItemClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.ViewHolder>(DiffCallback) {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    inner class ViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(task: Task) {
            binding.tvTaskId.text   = "TASK #${task.tid}"
            binding.tvTaskName.text = task.taskName
            binding.tvStyleNo.text  = "Style: ${task.styleNo}"
            binding.tvDeadline.text = task.deadline?.toDate()?.let { dateFormat.format(it) } ?: "N/A"

            // Completion info
            if (task.status == AppConstants.STATUS_COMPLETED) {
                binding.tvCompletedAt.visibility = View.VISIBLE
                val completedStr = task.completedAt?.toDate()?.let { dateFormat.format(it) } ?: ""
                binding.tvCompletedAt.text = "Completed: $completedStr"
            } else {
                binding.tvCompletedAt.visibility = View.GONE
            }

            // Status badge (Premium Tonal Design)
            val context = binding.root.context
            when (task.status) {
                AppConstants.STATUS_OVERDUE -> {
                    binding.tvStatus.text = "OVERDUE"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_overdue)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.color_status_overdue))
                }
                AppConstants.STATUS_COMPLETED -> {
                    binding.tvStatus.text = "COMPLETED"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_completed)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.color_status_completed))
                }
                else -> {
                    binding.tvStatus.text = "PENDING"
                    binding.tvStatus.setBackgroundResource(R.drawable.bg_badge_pending)
                    binding.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.color_status_pending))
                }
            }

            // Importance badge (Premium Tonal Design)
            when (task.importance) {
                AppConstants.IMPORTANCE_HIGH -> {
                    binding.tvImportance.setBackgroundResource(R.drawable.bg_badge_overdue)
                    binding.tvImportance.setTextColor(ContextCompat.getColor(context, R.color.color_status_overdue))
                }
                AppConstants.IMPORTANCE_LOW -> {
                    binding.tvImportance.setBackgroundResource(R.drawable.bg_badge_completed)
                    binding.tvImportance.setTextColor(ContextCompat.getColor(context, R.color.color_status_completed))
                }
                else -> {
                    binding.tvImportance.setBackgroundResource(R.drawable.bg_badge_pending)
                    binding.tvImportance.setTextColor(ContextCompat.getColor(context, R.color.color_status_pending))
                }
            }
            binding.tvImportance.text = task.importance.uppercase()

            binding.root.setOnClickListener { onItemClick(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(old: Task, new: Task) = old.documentId == new.documentId
            override fun areContentsTheSame(old: Task, new: Task) = old == new
        }
    }
}

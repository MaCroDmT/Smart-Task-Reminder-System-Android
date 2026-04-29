package com.ssl.smarttaskreminder.ui.task

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.data.model.Task
import com.ssl.smarttaskreminder.databinding.ActivityTaskDetailBinding
import com.ssl.smarttaskreminder.notifications.NotificationScheduler
import com.ssl.smarttaskreminder.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private val viewModel: TaskViewModel by viewModels()
    private var currentTask: Task? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Task Detail"

        val taskDocId = intent.getStringExtra(AppConstants.EXTRA_TASK_ID) ?: run {
            finish(); return
        }

        viewModel.loadTask(taskDocId)

        viewModel.selectedTask.observe(this) { task ->
            task?.let { displayTask(it) }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.success.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
                // Cancel notification since task is done
                currentTask?.let { task ->
                    NotificationScheduler.cancelTaskReminder(this, task.documentId)
                }
                
                if (it == "Task deleted!") {
                    finish()
                }
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show(); viewModel.clearMessages() }
        }
    }

    private fun displayTask(task: Task) {
        currentTask = task

        binding.tvTaskId.text   = "Task #${task.tid}"
        binding.tvTaskName.text = task.taskName
        binding.tvStyleNo.text  = task.styleNo.ifEmpty { "N/A" }
        binding.tvImportance.text = task.importance.uppercase()
        binding.tvDetails.text  = task.details.ifEmpty { "No details provided" }

        val deadline = task.deadline?.toDate()
        binding.tvDeadline.text = deadline?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
        } ?: "N/A"

        // Status badge
        val (statusText, statusBg) = when (task.status) {
            AppConstants.STATUS_OVERDUE   -> "OVERDUE" to com.ssl.smarttaskreminder.R.drawable.bg_button_danger
            AppConstants.STATUS_COMPLETED -> "COMPLETED" to com.ssl.smarttaskreminder.R.drawable.bg_button_success
            else                          -> "PENDING" to com.ssl.smarttaskreminder.R.drawable.bg_button_primary
        }
        binding.tvStatusBadge.text = statusText
        binding.tvStatusBadge.setBackgroundResource(statusBg)

        // Completed info
        if (task.status == AppConstants.STATUS_COMPLETED) {
            binding.layoutCompletedInfo.visibility = View.VISIBLE
            binding.tvCompletedAt.text = task.completedAt?.toDate()
                ?.let { dateFormat.format(it) } ?: ""
            binding.tvCompletionType.text = "Type: ${task.completionType?.uppercase() ?: ""}"
        } else {
            binding.layoutCompletedInfo.visibility = View.GONE
        }

        // Action buttons per status
        binding.btnMarkComplete.visibility  = View.GONE
        binding.btnLateCompletion.visibility = View.GONE

        when (task.status) {
            AppConstants.STATUS_PENDING -> {
                binding.btnMarkComplete.visibility = View.VISIBLE
                binding.btnMarkComplete.setOnClickListener { confirmCompletion(task) }
            }
            AppConstants.STATUS_OVERDUE -> {
                binding.btnLateCompletion.visibility = View.VISIBLE
                binding.btnLateCompletion.setOnClickListener { confirmCompletion(task) }
            }
        }

        // Super Admin delete button
        if (SessionManager.isSuperAdmin()) {
            binding.btnDelete.visibility = View.VISIBLE
            binding.btnDelete.setOnClickListener {
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Task")
                    .setMessage("Permanently delete this task?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteTask(task.documentId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    private fun confirmCompletion(task: Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Complete Task")
            .setMessage("Mark \"${task.taskName}\" as completed?")
            .setPositiveButton("Yes") { _, _ -> viewModel.completeTask(task.documentId, task.deadline) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

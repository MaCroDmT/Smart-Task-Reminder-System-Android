package com.ssl.smarttaskreminder.ui.task

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.databinding.ActivityAddTaskBinding
import com.ssl.smarttaskreminder.notifications.NotificationScheduler
import com.ssl.smarttaskreminder.viewmodel.AdminViewModel
import com.ssl.smarttaskreminder.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTaskBinding
    private val viewModel: TaskViewModel by viewModels()
    private val adminViewModel: AdminViewModel by viewModels()
    
    private var selectedDeadline: Date? = null
    private var selectedManagerId: Int = 0
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Create Task"
        
        setupManagerSelection()

        // Importance spinner
        val importanceLevels = listOf("High", "Medium", "Low")
        binding.spinnerImportance.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item, importanceLevels
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerImportance.setSelection(1) // Default: Medium

        // Date picker
        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    val cal2 = Calendar.getInstance()
                    cal2.set(year, month, day)
                    selectedDeadline = cal2.time
                    binding.btnPickDate.text = "📅 ${dateFormat.format(selectedDeadline!!)}"
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSave.setOnClickListener {
            val taskName   = binding.etTaskName.text.toString().trim()
            val styleNo    = binding.etStyleNo.text.toString().trim()
            val details    = binding.etDetails.text.toString().trim()
            val importance = when (binding.spinnerImportance.selectedItemPosition) {
                0    -> AppConstants.IMPORTANCE_HIGH
                2    -> AppConstants.IMPORTANCE_LOW
                else -> AppConstants.IMPORTANCE_MEDIUM
            }
            val deadline = selectedDeadline

            if (taskName.isEmpty()) {
                showError("Task name is required")
                return@setOnClickListener
            }
            if (deadline == null) {
                showError("Please select a deadline")
                return@setOnClickListener
            }

            val finalManagerId = if (SessionManager.role == AppConstants.ROLE_ADMIN) {
                selectedManagerId
            } else {
                SessionManager.managerId
            }

            viewModel.createTask(
                taskName   = taskName,
                styleNo    = styleNo,
                details    = details,
                importance = importance,
                deadline   = deadline,
                managerId  = finalManagerId
            )
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled      = !isLoading
        }

        viewModel.success.observe(this) { msg ->
            msg?.let {
                // Schedule notification for newly created task
                viewModel.selectedTask.value?.let { task ->
                    NotificationScheduler.scheduleTaskReminder(
                        context   = applicationContext,
                        taskDocId = task.documentId,
                        companyId = SessionManager.companyId
                    )
                    
                    // Also show an immediate notification for testing/feedback
                    com.ssl.smarttaskreminder.notifications.NotificationHelper.showTaskCreatedNotification(
                        applicationContext,
                        task.taskName,
                        task.styleNo
                    )
                }
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearMessages()
                binding.root.postDelayed({ finish() }, 1000)
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let { showError(it); viewModel.clearMessages() }
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text       = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun setupManagerSelection() {
        if (SessionManager.role == AppConstants.ROLE_ADMIN) {
            binding.layoutManagerSelection.visibility = View.VISIBLE
            
            adminViewModel.managers.observe(this) { managers ->
                val names = managers.map { "${it.name} (${it.department})" }
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
                binding.actvManager.setAdapter(adapter)
                
                binding.actvManager.setOnItemClickListener { _, _, position, _ ->
                    selectedManagerId = managers[position].mid
                }
            }
            adminViewModel.loadManagers()
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

package com.ssl.smarttaskreminder.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.databinding.ActivityAdminDashboardBinding
import com.ssl.smarttaskreminder.notifications.NotificationScheduler
import com.ssl.smarttaskreminder.ui.auth.LoginActivity
import com.ssl.smarttaskreminder.ui.task.AddTaskActivity
import com.ssl.smarttaskreminder.ui.task.TaskListActivity
import com.ssl.smarttaskreminder.viewmodel.AdminViewModel
import com.ssl.smarttaskreminder.viewmodel.AuthViewModel

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val adminViewModel: AdminViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Schedule admin overdue notification
        NotificationScheduler.scheduleAdminOverdueAlert(
            context     = this,
            adminUid    = SessionManager.firebaseUid,
            companyId   = SessionManager.companyId
        )

        setupClickListeners()
        observeViewModel()
        
        binding.tvCompanyNameHero.text = SessionManager.userName
        binding.tvUserRoleHero.text    = "Administrator — ${SessionManager.companyName}"
        
        adminViewModel.loadDashboardStats()
        adminViewModel.loadAnalytics()
    }

    private fun generateReport() {
        val pending   = adminViewModel.pendingCount.value ?: 0
        val overdue   = adminViewModel.overdueCount.value ?: 0
        val completed = adminViewModel.completedCount.value ?: 0
        val pct       = adminViewModel.completionPct.value ?: 0f
        val best      = adminViewModel.bestPerformers.value ?: emptyList()
        val worst     = adminViewModel.worstPerformers.value ?: emptyList()
        val health    = adminViewModel.departmentHealthScores.value ?: emptyMap()

        val file = com.ssl.smarttaskreminder.utils.PdfReportGenerator.generateWeeklyReport(
            context          = this,
            pending          = pending,
            overdue          = overdue,
            completed        = completed,
            completionPct    = pct,
            bestPerformers   = best.map { it.first to (it.second as Int) },
            worstPerformers  = worst.map { it.first to (it.second as Int) },
            deptHealthScores = health
        )

        if (file != null) {
            com.ssl.smarttaskreminder.utils.PdfReportGenerator.sharePdf(this, file)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Logout")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            logout()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        authViewModel.logout()
        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }

    private fun setupClickListeners() {
        binding.btnLogoutHeader.setOnClickListener { logout() }
        binding.btnGenerateReportHeader.setOnClickListener { generateReport() }
        binding.btnManageManagers.setOnClickListener {
            startActivity(Intent(this, ManagerListActivity::class.java))
        }
        binding.btnManageUsers.setOnClickListener {
            startActivity(Intent(this, UserListActivity::class.java))
        }
        binding.btnAnalytics.setOnClickListener {
            startActivity(Intent(this, AdminAnalyticsActivity::class.java))
        }
        binding.btnAllTasks.setOnClickListener {
            val intent = Intent(this, TaskListActivity::class.java)
            intent.putExtra("VIEW_MY_TASKS", false)
            startActivity(intent)
        }
        binding.btnMyTasks.setOnClickListener {
            val intent = Intent(this, TaskListActivity::class.java)
            intent.putExtra("VIEW_MY_TASKS", true)
            startActivity(intent)
        }
        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    private fun observeViewModel() {
        adminViewModel.pendingCount.observe(this) {
            binding.tvPendingCount.text = it.toString()
            updateActiveTaskCount()
        }
        adminViewModel.overdueCount.observe(this) {
            binding.tvOverdueCount.text = it.toString()
            updateActiveTaskCount()
        }
        adminViewModel.completedCount.observe(this) {
            binding.tvCompletedCount.text = it.toString()
        }
        adminViewModel.completionPct.observe(this) {
            binding.tvCompletionRate.text = "%.0f%%".format(it)
        }
        adminViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        adminViewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show() }
        }
    }

    private fun updateActiveTaskCount() {
        val pending = adminViewModel.pendingCount.value ?: 0
        val overdue = adminViewModel.overdueCount.value ?: 0
        binding.tvActiveTaskCount.text = "${pending + overdue} Active Tasks"
    }

    override fun onResume() {
        super.onResume()
        adminViewModel.loadDashboardStats()
    }
}

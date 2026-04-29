package com.ssl.smarttaskreminder.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.databinding.ActivityAdminDashboardBinding
import com.ssl.smarttaskreminder.ui.auth.LoginActivity
import com.ssl.smarttaskreminder.ui.task.AddTaskActivity
import com.ssl.smarttaskreminder.ui.task.TaskListActivity
import com.ssl.smarttaskreminder.viewmodel.AuthViewModel
import com.ssl.smarttaskreminder.viewmodel.TaskViewModel

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val taskViewModel: TaskViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hero Info
        binding.tvCompanyNameHero.text = SessionManager.userName
        binding.tvUserRoleHero.text    = "User — ${SessionManager.companyName}"
        binding.tvCompletionRate.visibility = View.GONE

        setupClickListeners()
        observeViewModel()
        taskViewModel.loadMyTasks()
    }

    private fun setupClickListeners() {
        binding.btnLogoutHeader.setOnClickListener { logout() }
        binding.btnManageManagers.text = "View My Tasks"
        binding.btnManageManagers.setOnClickListener {
            startActivity(Intent(this, TaskListActivity::class.java))
        }
        
        binding.btnManageUsers.visibility = View.GONE
        binding.btnAnalytics.visibility   = View.GONE
        binding.btnAllTasks.visibility    = View.GONE
        binding.btnMyTasks.visibility     = View.GONE

        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Logout"); return true
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

    private fun observeViewModel() {
        taskViewModel.pendingCount.observe(this) { 
            binding.tvPendingCount.text = it.toString()
            updateActiveCount()
        }
        taskViewModel.overdueCount.observe(this) { 
            binding.tvOverdueCount.text = it.toString() 
            updateActiveCount()
        }
        taskViewModel.completedCount.observe(this) { binding.tvCompletedCount.text = it.toString() }
        taskViewModel.loading.observe(this) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
    }

    private fun updateActiveCount() {
        val p = taskViewModel.pendingCount.value ?: 0
        val o = taskViewModel.overdueCount.value ?: 0
        binding.tvActiveTaskCount.text = "${p + o} Personal Tasks"
    }

    override fun onResume() { super.onResume(); taskViewModel.loadMyTasks() }
}

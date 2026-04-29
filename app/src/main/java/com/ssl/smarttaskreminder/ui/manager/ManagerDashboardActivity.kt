package com.ssl.smarttaskreminder.ui.manager

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
import com.ssl.smarttaskreminder.viewmodel.ManagerViewModel

class ManagerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private val viewModel: ManagerViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Hero Info
        binding.tvCompanyNameHero.text = SessionManager.userName
        binding.tvUserRoleHero.text    = "Manager — ${SessionManager.companyName}"

        setupClickListeners()
        observeViewModel()
        viewModel.loadTeamData()
    }

    private fun setupClickListeners() {
        binding.btnLogoutHeader.setOnClickListener { logout() }
        binding.btnManageManagers.text = "My Team"
        binding.btnManageManagers.setOnClickListener {
            startActivity(Intent(this, TeamUserListActivity::class.java))
        }

        binding.btnManageUsers.text = "All Tasks"
        binding.btnManageUsers.setOnClickListener {
            val intent = Intent(this, TaskListActivity::class.java)
            intent.putExtra("VIEW_MY_TASKS", false)
            startActivity(intent)
        }

        binding.btnAnalytics.visibility = View.GONE
        binding.btnAllTasks.visibility = View.GONE

        binding.btnMyTasks.text = "My Personal Tasks"
        binding.btnMyTasks.setOnClickListener {
            val intent = Intent(this, TaskListActivity::class.java)
            intent.putExtra("VIEW_MY_TASKS", true)
            startActivity(intent)
        }

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
        viewModel.pendingCount.observe(this) { 
            binding.tvPendingCount.text = it.toString()
            updateActiveCount()
        }
        viewModel.overdueCount.observe(this) { 
            binding.tvOverdueCount.text = it.toString() 
            updateActiveCount()
        }
        viewModel.completedCount.observe(this) { binding.tvCompletedCount.text = it.toString() }
        viewModel.loading.observe(this) { binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE }
    }

    private fun updateActiveCount() {
        val p = viewModel.pendingCount.value ?: 0
        val o = viewModel.overdueCount.value ?: 0
        binding.tvActiveTaskCount.text = "${p + o} Active Tasks"
    }

    override fun onResume() { super.onResume(); viewModel.loadTeamData() }
}

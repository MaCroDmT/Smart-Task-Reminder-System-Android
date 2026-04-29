package com.ssl.smarttaskreminder.ui.task

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.data.model.Task
import com.ssl.smarttaskreminder.databinding.ActivityTaskListBinding
import com.ssl.smarttaskreminder.ui.adapters.TaskAdapter
import com.ssl.smarttaskreminder.viewmodel.TaskViewModel

/**
 * Shared TaskListActivity — used by User, Manager and Admin roles.
 * Shows all tasks with tab filtering (All | Pending | Overdue | Completed).
 */
class TaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private var allTasks = listOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val viewMyTasks = intent.getBooleanExtra("VIEW_MY_TASKS", false)
        val role = SessionManager.role
        title = when (role) {
            AppConstants.ROLE_SUPER_ADMIN -> "All Company Tasks"
            AppConstants.ROLE_ADMIN   -> if (viewMyTasks) "My Tasks" else "All Company Tasks"
            AppConstants.ROLE_MANAGER -> "Team Tasks"
            else                      -> "My Tasks"
        }

        setupTabs()

        if (role == AppConstants.ROLE_SUPER_ADMIN) {
            binding.fabAddTask.visibility = View.GONE
        }

        adapter = TaskAdapter { task ->
            val intent = Intent(this, TaskDetailActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_TASK_ID, task.documentId)
            startActivity(intent)
        }
        binding.rvTasks.layoutManager = LinearLayoutManager(this)
        binding.rvTasks.adapter = adapter

        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(this, AddTaskActivity::class.java))
        }

        viewModel.tasks.observe(this) { tasks ->
            allTasks = tasks
            filterAndShow("all")
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        loadTasksForRole()
    }

    private fun loadTasksForRole() {
        val viewMyTasks = intent.getBooleanExtra("VIEW_MY_TASKS", false)
        val role = SessionManager.role
        when (role) {
            AppConstants.ROLE_SUPER_ADMIN -> viewModel.loadAllTasks()
            AppConstants.ROLE_ADMIN   -> if (viewMyTasks) viewModel.loadMyTasks() else viewModel.loadAllTasks()
            AppConstants.ROLE_MANAGER -> if (viewMyTasks) viewModel.loadMyTasks() else viewModel.loadManagerTasks(SessionManager.managerId)
            else                      -> viewModel.loadMyTasks()
        }
    }

    private fun setupTabs() {
        listOf("All", "Pending", "Overdue", "Completed").forEach {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it))
        }
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> filterAndShow("all")
                    1 -> filterAndShow(AppConstants.STATUS_PENDING)
                    2 -> filterAndShow(AppConstants.STATUS_OVERDUE)
                    3 -> filterAndShow(AppConstants.STATUS_COMPLETED)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun filterAndShow(filter: String) {
        val filtered = if (filter == "all") allTasks
                       else allTasks.filter { it.status == filter }
        adapter.submitList(filtered)
        binding.tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        binding.rvTasks.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onResume() { super.onResume(); loadTasksForRole() }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

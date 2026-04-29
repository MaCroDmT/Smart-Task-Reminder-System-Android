package com.ssl.smarttaskreminder.ui.manager

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.databinding.ActivityTaskListBinding
import com.ssl.smarttaskreminder.ui.adapters.TaskAdapter
import com.ssl.smarttaskreminder.ui.task.AddTaskActivity
import com.ssl.smarttaskreminder.ui.task.TaskDetailActivity
import com.ssl.smarttaskreminder.viewmodel.TaskViewModel
import com.ssl.smarttaskreminder.SessionManager
import com.google.android.material.tabs.TabLayout

class ManagerTaskListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskListBinding
    private val viewModel: TaskViewModel by viewModels()
    private lateinit var adapter: TaskAdapter
    private var allTasks = listOf<com.ssl.smarttaskreminder.data.model.Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Team Tasks"

        setupTabs()

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

        viewModel.loadManagerTasks(SessionManager.managerId)
    }

    private fun setupTabs() {
        val tabs = listOf("All", "Pending", "Overdue", "Completed")
        tabs.forEach { binding.tabLayout.addTab(binding.tabLayout.newTab().setText(it)) }
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

    override fun onResume() { super.onResume(); viewModel.loadManagerTasks(SessionManager.managerId) }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

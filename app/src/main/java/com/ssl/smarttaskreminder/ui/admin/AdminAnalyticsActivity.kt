package com.ssl.smarttaskreminder.ui.admin

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.databinding.ActivityAdminAnalyticsBinding
import com.ssl.smarttaskreminder.ui.adapters.AnalyticsItemAdapter
import com.ssl.smarttaskreminder.utils.PdfReportGenerator
import com.ssl.smarttaskreminder.viewmodel.AdminViewModel

class AdminAnalyticsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminAnalyticsBinding
    private val viewModel: AdminViewModel by viewModels()

    private val overdueAdapter   = AnalyticsItemAdapter()
    private val bestAdapter      = AnalyticsItemAdapter()
    private val worstAdapter     = AnalyticsItemAdapter()
    private val healthAdapter    = AnalyticsItemAdapter(isPercentage = true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminAnalyticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Executive Analytics"

        binding.rvOverdueTeams.layoutManager     = LinearLayoutManager(this)
        binding.rvBestPerformers.layoutManager   = LinearLayoutManager(this)
        binding.rvWorstPerformers.layoutManager  = LinearLayoutManager(this)
        binding.rvDeptHealth.layoutManager       = LinearLayoutManager(this)

        binding.rvOverdueTeams.adapter    = overdueAdapter
        binding.rvBestPerformers.adapter  = bestAdapter
        binding.rvWorstPerformers.adapter = worstAdapter
        binding.rvDeptHealth.adapter      = healthAdapter

        binding.fabGenerateReport.setOnClickListener { generateReport() }

        observeViewModel()
        viewModel.loadDashboardStats() // To get core numbers
        viewModel.loadAnalytics()      // To get performer lists
    }

    private fun generateReport() {
        val pending   = viewModel.pendingCount.value ?: 0
        val overdue   = viewModel.overdueCount.value ?: 0
        val completed = viewModel.completedCount.value ?: 0
        val pct       = viewModel.completionPct.value ?: 0f
        val best      = viewModel.bestPerformers.value ?: emptyList()
        val worst     = viewModel.worstPerformers.value ?: emptyList()
        val health    = viewModel.departmentHealthScores.value ?: emptyMap()

        val file = com.ssl.smarttaskreminder.utils.PdfReportGenerator.generateWeeklyReport(
            context          = this,
            pending          = pending,
            overdue          = overdue,
            completed        = completed,
            completionPct    = pct,
            bestPerformers   = best.map { it.first to (it.second as? Int ?: 0) },
            worstPerformers  = worst.map { it.first to (it.second as? Int ?: 0) },
            deptHealthScores = health
        )

        if (file != null) {
            com.ssl.smarttaskreminder.utils.PdfReportGenerator.sharePdf(this, file)
        }
    }

    private fun observeViewModel() {
        viewModel.topOverdueManagers.observe(this) { overdueAdapter.submitList(it) }
        viewModel.bestPerformers.observe(this) { bestAdapter.submitList(it) }
        viewModel.worstPerformers.observe(this) { worstAdapter.submitList(it) }
        viewModel.departmentHealthScores.observe(this) { scores ->
            healthAdapter.submitList(scores.toList())
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

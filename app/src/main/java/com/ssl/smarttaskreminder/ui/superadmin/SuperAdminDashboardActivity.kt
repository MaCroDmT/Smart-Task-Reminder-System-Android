package com.ssl.smarttaskreminder.ui.superadmin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.R
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.data.model.Company
import com.ssl.smarttaskreminder.databinding.ActivitySuperAdminDashboardBinding
import com.ssl.smarttaskreminder.ui.adapters.CompanyAdapter
import com.ssl.smarttaskreminder.ui.auth.LoginActivity
import com.ssl.smarttaskreminder.viewmodel.AuthViewModel
import com.ssl.smarttaskreminder.viewmodel.CompanyViewModel

class SuperAdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuperAdminDashboardBinding
    private val companyViewModel: CompanyViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var adapter: CompanyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySuperAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeViewModel()

        companyViewModel.loadAllCompanies()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Platform Dashboard"
        binding.tvToolbarSubtitle.text = "Logged in: ${SessionManager.userName}"
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "Logout")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == 1) {
            authViewModel.logout()
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        adapter = CompanyAdapter(
            onItemClick    = { company -> openCompanyDetail(company) },
            onToggleStatus = { company ->
                companyViewModel.toggleCompanyStatus(company.cid, company.status)
            }
        )
        binding.rvCompanies.layoutManager = LinearLayoutManager(this)
        binding.rvCompanies.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddCompany.setOnClickListener {
            startActivity(Intent(this, AddCompanyActivity::class.java))
        }
    }

    private fun observeViewModel() {
        companyViewModel.companies.observe(this) { companies ->
            adapter.submitList(companies)
            binding.tvTotalCompanies.text = companies.size.toString()

            val totalUsers = 0 // Would need platform-wide user query — simplified here
            binding.tvTotalUsers.text = totalUsers.toString()

            if (companies.isEmpty()) {
                binding.tvEmpty.visibility     = View.VISIBLE
                binding.rvCompanies.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility     = View.GONE
                binding.rvCompanies.visibility = View.VISIBLE
            }
        }

        companyViewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        companyViewModel.success.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                companyViewModel.clearMessages()
            }
        }

        companyViewModel.error.observe(this) { err ->
            err?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                companyViewModel.clearMessages()
            }
        }
    }

    private fun openCompanyDetail(company: Company) {
        val intent = Intent(this, CompanyDetailActivity::class.java)
        intent.putExtra(AppConstants.EXTRA_COMPANY_ID,   company.cid)
        intent.putExtra(AppConstants.EXTRA_COMPANY_NAME, company.name)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        companyViewModel.loadAllCompanies()
    }
}

package com.ssl.smarttaskreminder.ui.superadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.databinding.ActivityCompanyDetailBinding
import com.ssl.smarttaskreminder.viewmodel.CompanyViewModel

class CompanyDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompanyDetailBinding
    private val viewModel: CompanyViewModel by viewModels()
    private var companyId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompanyDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId = intent.getStringExtra(AppConstants.EXTRA_COMPANY_ID) ?: ""
        val companyName = intent.getStringExtra(AppConstants.EXTRA_COMPANY_NAME) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = companyName

        // Impersonate the selected company purely for the downstream List activities
        com.ssl.smarttaskreminder.SessionManager.companyId = companyId

        binding.btnManageAdmins.setOnClickListener {
            val intent = Intent(this, AdminListActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_COMPANY_ID,   companyId)
            intent.putExtra(AppConstants.EXTRA_COMPANY_NAME, companyName)
            startActivity(intent)
        }

        binding.btnManageManagers.setOnClickListener {
            val intent = Intent(this, com.ssl.smarttaskreminder.ui.admin.ManagerListActivity::class.java)
            startActivity(intent)
        }

        binding.btnManageUsers.setOnClickListener {
            val intent = Intent(this, com.ssl.smarttaskreminder.ui.admin.UserListActivity::class.java)
            startActivity(intent)
        }

        binding.btnManageTasks.setOnClickListener {
            val intent = Intent(this, com.ssl.smarttaskreminder.ui.task.TaskListActivity::class.java)
            startActivity(intent)
        }

        viewModel.selectedCompany.observe(this) { company ->
            company?.let {
                binding.tvCompanyName.text = it.name
                binding.tvCompanyCid.text  = "CID: ${it.cid}"
                binding.tvStatus.text      = "Status: ${it.status.uppercase()}"
            }
        }

        viewModel.loadCompany(companyId)
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    override fun onResume() {
        super.onResume()
        // Restore impersonated company id if we come back
        com.ssl.smarttaskreminder.SessionManager.companyId = companyId
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear impersonation when we leave the company context completely
        if (com.ssl.smarttaskreminder.SessionManager.role == AppConstants.ROLE_SUPER_ADMIN) {
            com.ssl.smarttaskreminder.SessionManager.companyId = ""
        }
    }
}

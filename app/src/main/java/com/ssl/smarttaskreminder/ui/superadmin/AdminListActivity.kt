package com.ssl.smarttaskreminder.ui.superadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Admin
import com.ssl.smarttaskreminder.databinding.ActivityUserListBinding
import com.ssl.smarttaskreminder.ui.adapters.AdminAdapter
import com.ssl.smarttaskreminder.viewmodel.CompanyViewModel

class AdminListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserListBinding
    private val viewModel: CompanyViewModel by viewModels()
    private lateinit var adapter: AdminAdapter
    private var companyId: String = ""
    private var companyName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId   = intent.getStringExtra(AppConstants.EXTRA_COMPANY_ID) ?: ""
        companyName = intent.getStringExtra(AppConstants.EXTRA_COMPANY_NAME) ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Manage Admins"

        adapter = AdminAdapter(
            onEditClick = { admin ->
                val intent = Intent(this, AddAdminActivity::class.java)
                intent.putExtra(AppConstants.EXTRA_COMPANY_ID,   admin.companyId)
                intent.putExtra(AppConstants.EXTRA_COMPANY_NAME, companyName)
                intent.putExtra(AppConstants.EXTRA_ADMIN_DOC_ID, admin.documentId)
                intent.putExtra(AppConstants.EXTRA_IS_EDIT_MODE, true)
                startActivity(intent)
            },
            onDeleteClick = { admin ->
                MaterialAlertDialogBuilder(this)
                    .setTitle("Delete Admin")
                    .setMessage("Remove ${admin.name} permanently? Their ID will be recycled.")
                    .setPositiveButton("Delete") { _, _ -> viewModel.deleteAdmin(admin.documentId) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        binding.rvUsers.adapter = adapter

        binding.fabAddUser.setOnClickListener {
            val intent = Intent(this, AddAdminActivity::class.java)
            intent.putExtra(AppConstants.EXTRA_COMPANY_ID,   companyId)
            intent.putExtra(AppConstants.EXTRA_COMPANY_NAME, companyName)
            startActivity(intent)
        }

        observeViewModel()
        viewModel.loadAdminsForCompany(companyId)
    }

    private fun observeViewModel() {
        viewModel.adminsForCompany.observe(this) { admins ->
            adapter.submitList(admins)
            binding.tvEmpty.visibility = if (admins.isEmpty()) View.VISIBLE else View.GONE
            binding.rvUsers.visibility = if (admins.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.success.observe(this) { msg ->
            msg?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show(); viewModel.clearMessages() }
        }

        viewModel.error.observe(this) { err ->
            err?.let { Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show(); viewModel.clearMessages() }
        }
    }

    override fun onResume() { super.onResume(); viewModel.loadAdminsForCompany(companyId) }
    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

package com.ssl.smarttaskreminder.ui.superadmin

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.databinding.ActivityAddAdminBinding
import com.ssl.smarttaskreminder.ui.auth.LoginActivity
import com.ssl.smarttaskreminder.viewmodel.CompanyViewModel

class AddAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAdminBinding
    private val viewModel: CompanyViewModel by viewModels()
    private var companyId: String = ""
    private var adminUid: String  = ""
    private var isEditMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        companyId  = intent.getStringExtra(AppConstants.EXTRA_COMPANY_ID) ?: ""
        adminUid   = intent.getStringExtra(AppConstants.EXTRA_ADMIN_DOC_ID) ?: ""
        isEditMode = intent.getBooleanExtra(AppConstants.EXTRA_IS_EDIT_MODE, false)
        val companyName = intent.getStringExtra(AppConstants.EXTRA_COMPANY_NAME) ?: ""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        if (isEditMode) {
            title = "Edit Admin"
            binding.btnSave.text = "Update Admin"
            binding.layoutPassword.visibility = View.GONE
            binding.tvCompanyLabel.text = "Editing Admin for: $companyName"
            binding.btnResetPassword.visibility = View.VISIBLE
            
            // Observe the list to prefill when it arrives
            viewModel.adminsForCompany.observe(this) { list ->
                list.find { it.documentId == adminUid }?.let { a ->
                    binding.etName.setText(a.name)
                    binding.etEmail.setText(a.email)
                }
            }
            viewModel.loadAdminsForCompany(companyId)
        } else {
            title = "Add Admin"
            binding.tvCompanyLabel.text = if (companyName.isNotEmpty()) {
                "Creating Admin for: $companyName"
            } else {
                "Creating Admin for Company"
            }
        }

        binding.btnSave.setOnClickListener {
            val name  = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val pass  = binding.etTempPassword.text.toString().trim()
            
            if (isEditMode) {
                viewModel.updateAdmin(adminUid, companyId, name, email)
            } else {
                if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                    showError("All fields are required for new admin")
                    return@setOnClickListener
                }
                viewModel.createAdminForCompany(companyId, name, email, pass)
            }
        }

        binding.btnResetPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            if (email.isEmpty()) {
                showError("Email is required to send reset link")
                return@setOnClickListener
            }
            viewModel.resetPassword(email)
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled      = !isLoading
        }

        viewModel.success.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessages()
                finish()
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let {
                showError(it)
                viewModel.clearMessages()
            }
        }
    }

    private fun prefillData() {
        viewModel.adminsForCompany.value?.find { it.documentId == adminUid }?.let { a ->
            binding.etName.setText(a.name)
            binding.etEmail.setText(a.email)
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text       = msg
        binding.tvError.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

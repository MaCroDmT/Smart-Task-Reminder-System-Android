package com.ssl.smarttaskreminder.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.databinding.ActivityAddEditManagerBinding
import com.ssl.smarttaskreminder.ui.auth.LoginActivity
import com.ssl.smarttaskreminder.viewmodel.AdminViewModel

class AddEditManagerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditManagerBinding
    private val viewModel: AdminViewModel by viewModels()

    private var isEditMode = false
    private var managerUid = ""

    private val departments = listOf(
        "Production",
        "Sampling",
        "Merchandising & Marketing",
        "Design",
        "Programming",
        "IE",
        "Store",
        "Yarn",
        "Maintenance",
        "IT",
        "Knitting",
        "Washing",
        "Commercial",
        "Accounts",
        "Human Resources (HR) & Administration",
        "Laboratory",
        "Social & Welfare",
        "General"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra(AppConstants.EXTRA_IS_EDIT_MODE, false)
        managerUid = intent.getStringExtra(AppConstants.EXTRA_MANAGER_ID) ?: ""

        setupDropdown()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (isEditMode) {
            title = "Edit Manager"
            binding.tvTitle.text = "Edit Manager"
            binding.layoutPassword.visibility = View.GONE
            binding.etEmail.isEnabled = true
            binding.btnResetPassword.visibility = View.VISIBLE
            
            viewModel.managers.observe(this) { list ->
                list.find { it.documentId == managerUid }?.let { m ->
                    binding.etName.setText(m.name)
                    binding.etEmail.setText(m.email)
                    binding.actvDepartment.setText(m.department, false)
                }
            }
            viewModel.loadManagers()
        } else {
            title = "Add Manager"
        }

        binding.btnSave.setOnClickListener {
            val name  = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val dept  = binding.actvDepartment.text.toString().trim()
            val pass  = binding.etTempPassword.text.toString().trim()

            if (isEditMode) {
                viewModel.updateManager(managerUid, name, email, dept)
            } else {
                if (pass.isEmpty()) {
                    showError("Password is required for new manager")
                    return@setOnClickListener
                }
                viewModel.createManager(name, email, dept, pass)
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

        observeViewModel()
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, departments)
        binding.actvDepartment.setAdapter(adapter)
        if (!isEditMode) {
            binding.actvDepartment.setText(departments[0], false)
        }
    }

    private fun prefillData() {
        // Finding manager in viewModel's existing list
        viewModel.managers.value?.find { it.documentId == managerUid }?.let { m ->
            binding.etName.setText(m.name)
            binding.etEmail.setText(m.email)
            binding.actvDepartment.setText(m.department, false)
        }
    }
    private fun observeViewModel() {
        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSave.isEnabled      = !isLoading
        }

        viewModel.success.observe(this) { msg ->
            msg?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearMessages()
                
                if (!isEditMode) {
                    // REST API silently creates account. Session remains intact.
                    finish()
                } else {
                    finish()
                }
            }
        }

        viewModel.error.observe(this) { err ->
            err?.let {
                binding.tvError.text       = it
                binding.tvError.visibility = View.VISIBLE
                viewModel.clearMessages()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

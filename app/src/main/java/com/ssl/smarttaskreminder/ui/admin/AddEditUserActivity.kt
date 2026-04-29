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
import com.ssl.smarttaskreminder.data.model.Manager
import com.ssl.smarttaskreminder.databinding.ActivityAddEditUserBinding
import com.ssl.smarttaskreminder.ui.auth.LoginActivity
import com.ssl.smarttaskreminder.viewmodel.AdminViewModel

class AddEditUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditUserBinding
    private val viewModel: AdminViewModel by viewModels()

    private var isEditMode = false
    private var userUid    = ""
    private var managers   = listOf<Manager>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isEditMode = intent.getBooleanExtra(AppConstants.EXTRA_IS_EDIT_MODE, false)
        userUid    = intent.getStringExtra(AppConstants.EXTRA_USER_ID) ?: ""

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (isEditMode) {
            title = "Edit User"
            binding.tvTitle.text = "Edit User"
            binding.layoutPassword.visibility = View.GONE
            binding.etEmail.isEnabled = true
            binding.btnResetPassword.visibility = View.VISIBLE
            
            viewModel.users.observe(this) { list ->
                list.find { it.documentId == userUid }?.let { u ->
                    binding.etName.setText(u.name)
                    binding.etEmail.setText(u.email)
                    
                    // Also update manager selection
                    val mgrList = viewModel.managers.value ?: emptyList()
                    val pos = mgrList.indexOfFirst { it.mid == u.managerId }
                    if (pos != -1) binding.spinnerManager.setSelection(pos + 1)
                }
            }
            viewModel.loadUsers()
        } else {
            title = "Add User"
        }

        viewModel.loadManagers()

        viewModel.managers.observe(this) { mgrList ->
            managers = mgrList
            val names = mgrList.map { "${it.name} (MID: ${it.mid})" }.toMutableList()
            names.add(0, "-- No Manager --")
            binding.spinnerManager.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_item, names
            ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
            
            // Re-select manager if in edit mode
            if (isEditMode) {
                viewModel.users.value?.find { it.documentId == userUid }?.let { u ->
                    val pos = mgrList.indexOfFirst { it.mid == u.managerId }
                    if (pos != -1) binding.spinnerManager.setSelection(pos + 1)
                }
            }
        }

        binding.btnSave.setOnClickListener {
            val name       = binding.etName.text.toString().trim()
            val email      = binding.etEmail.text.toString().trim()
            val pass       = binding.etTempPassword.text.toString().trim()
            val spinnerPos = binding.spinnerManager.selectedItemPosition
            val managerId  = if (spinnerPos > 0 && managers.isNotEmpty()) managers[spinnerPos - 1].mid else 0

            if (isEditMode) {
                viewModel.updateUser(userUid, name, email, managerId)
            } else {
                if (pass.isEmpty()) {
                    showError("Password is required for new user")
                    return@setOnClickListener
                }
                viewModel.createUser(name, email, pass, managerId)
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

    private fun prefillData() {
        viewModel.users.value?.find { it.documentId == userUid }?.let { u ->
            binding.etName.setText(u.name)
            binding.etEmail.setText(u.email)
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
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

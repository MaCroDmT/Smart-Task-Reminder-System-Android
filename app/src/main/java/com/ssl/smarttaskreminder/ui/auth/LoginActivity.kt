package com.ssl.smarttaskreminder.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.databinding.ActivityLoginBinding
import com.ssl.smarttaskreminder.ui.admin.AdminDashboardActivity
import com.ssl.smarttaskreminder.ui.manager.ManagerDashboardActivity
import com.ssl.smarttaskreminder.ui.superadmin.SuperAdminDashboardActivity
import com.ssl.smarttaskreminder.ui.user.UserDashboardActivity
import com.ssl.smarttaskreminder.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email    = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.tvError.text       = getString(com.ssl.smarttaskreminder.R.string.error_email_empty)
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.tvError.text       = getString(com.ssl.smarttaskreminder.R.string.error_password_empty)
                binding.tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }

            binding.tvError.visibility = View.GONE
            viewModel.login(email, password)
        }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is AuthViewModel.LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled     = false
                }
                is AuthViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled     = true
                    routeToDashboard(state.role)
                }
                is AuthViewModel.LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled     = true
                    binding.tvError.text           = state.message
                    binding.tvError.visibility     = View.VISIBLE
                }
            }
        }
    }

    private fun routeToDashboard(role: String) {
        val intent = when (role) {
            AppConstants.ROLE_SUPER_ADMIN -> Intent(this, SuperAdminDashboardActivity::class.java)
            AppConstants.ROLE_ADMIN       -> Intent(this, AdminDashboardActivity::class.java)
            AppConstants.ROLE_MANAGER     -> Intent(this, ManagerDashboardActivity::class.java)
            AppConstants.ROLE_USER        -> Intent(this, UserDashboardActivity::class.java)
            else -> return
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}

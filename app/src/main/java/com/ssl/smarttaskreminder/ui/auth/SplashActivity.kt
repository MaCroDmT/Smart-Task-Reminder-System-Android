package com.ssl.smarttaskreminder.ui.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.databinding.ActivitySplashBinding
import com.ssl.smarttaskreminder.ui.admin.AdminDashboardActivity
import com.ssl.smarttaskreminder.ui.manager.ManagerDashboardActivity
import com.ssl.smarttaskreminder.ui.superadmin.SuperAdminDashboardActivity
import com.ssl.smarttaskreminder.ui.user.UserDashboardActivity
import com.ssl.smarttaskreminder.viewmodel.AuthViewModel

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: AuthViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        checkAuth()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is AuthViewModel.LoginState.Success -> routeToDashboard(state.role)
                is AuthViewModel.LoginState.Error   -> goToLogin()
                else                                -> {}
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkAuth()
            }
        } else {
            checkAuth()
        }
    }

    private fun checkAuth() {
        if (viewModel.isSignedIn()) {
            viewModel.resolveCurrentUser()
        } else {
            goToLogin()
        }
    }

    private fun routeToDashboard(role: String) {
        val intent = when (role) {
            AppConstants.ROLE_SUPER_ADMIN -> Intent(this, SuperAdminDashboardActivity::class.java)
            AppConstants.ROLE_ADMIN       -> Intent(this, AdminDashboardActivity::class.java)
            AppConstants.ROLE_MANAGER     -> Intent(this, ManagerDashboardActivity::class.java)
            AppConstants.ROLE_USER        -> Intent(this, UserDashboardActivity::class.java)
            else                          -> Intent(this, LoginActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
}

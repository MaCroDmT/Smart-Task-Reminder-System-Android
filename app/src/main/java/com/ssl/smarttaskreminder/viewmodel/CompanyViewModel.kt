package com.ssl.smarttaskreminder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssl.smarttaskreminder.data.model.Admin
import com.ssl.smarttaskreminder.data.model.Company
import com.ssl.smarttaskreminder.data.repository.AdminRepository
import com.ssl.smarttaskreminder.data.repository.CompanyRepository
import com.ssl.smarttaskreminder.data.repository.ManagerRepository
import com.ssl.smarttaskreminder.data.repository.UserRepository
import kotlinx.coroutines.launch

class CompanyViewModel : ViewModel() {

    private val companyRepo  = CompanyRepository()
    private val adminRepo    = AdminRepository()
    private val managerRepo  = ManagerRepository()
    private val userRepo     = UserRepository()

    private val _companies    = MutableLiveData<List<Company>>()
    val companies: LiveData<List<Company>> = _companies

    private val _selectedCompany = MutableLiveData<Company?>()
    val selectedCompany: LiveData<Company?> = _selectedCompany

    private val _adminsForCompany = MutableLiveData<List<Admin>>()
    val adminsForCompany: LiveData<List<Admin>> = _adminsForCompany

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    // Platform stats
    private val _totalUsers = MutableLiveData<Int>()
    val totalUsers: LiveData<Int> = _totalUsers

    private val _totalManagers = MutableLiveData<Int>()
    val totalManagers: LiveData<Int> = _totalManagers

    fun loadAllCompanies() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _companies.value = companyRepo.getAllCompanies()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadCompany(cid: String) {
        viewModelScope.launch {
            try {
                _selectedCompany.value = companyRepo.getCompany(cid)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadAdminsForCompany(cid: String) {
        viewModelScope.launch {
            try {
                _adminsForCompany.value = adminRepo.getAdminsByCompany(cid)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createCompany(name: String, industry: String, status: String) {
        if (name.isBlank()) { _error.value = "Company name is required"; return }
        _loading.value = true
        viewModelScope.launch {
            try {
                companyRepo.createCompany(name, industry, status)
                _success.value = "Company created successfully!"
                loadAllCompanies()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateCompany(cid: String, name: String, industry: String, status: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                companyRepo.updateCompany(cid, name, industry, status)
                _success.value = "Company updated!"
                loadAllCompanies()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun toggleCompanyStatus(cid: String, currentStatus: String) {
        val newStatus = if (currentStatus == "active") "inactive" else "active"
        viewModelScope.launch {
            try {
                companyRepo.toggleStatus(cid, newStatus)
                _success.value = "Status updated to $newStatus"
                loadAllCompanies()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createAdminForCompany(cid: String, name: String, email: String, tempPassword: String) {
        if (name.isBlank() || email.isBlank() || tempPassword.isBlank()) {
            _error.value = "All fields are required"; return
        }
        _loading.value = true
        viewModelScope.launch {
            try {
                adminRepo.createAdmin(cid, name, email, tempPassword)
                _success.value = "Admin account created for $name!"
                loadAdminsForCompany(cid)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateAdmin(adminUid: String, cid: String, name: String, email: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                adminRepo.updateAdmin(adminUid, name, email)
                _success.value = "Admin updated!"
                loadAdminsForCompany(cid)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun resetPassword(email: String) {
        _loading.value = true
        com.google.firebase.auth.FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                _loading.value = false
                if (task.isSuccessful) {
                    _success.value = "Password reset email sent to $email"
                } else {
                    _error.value = task.exception?.message
                }
            }
    }

    fun deleteAdmin(adminUid: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                adminRepo.deleteAdmin(adminUid)
                _success.value = "Admin deleted successfully!"
                _selectedCompany.value?.let { loadAdminsForCompany(it.cid) }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _success.value = null
    }
}

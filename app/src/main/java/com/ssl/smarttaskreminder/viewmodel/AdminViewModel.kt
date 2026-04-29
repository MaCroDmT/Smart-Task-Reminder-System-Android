package com.ssl.smarttaskreminder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.data.model.Manager
import com.ssl.smarttaskreminder.data.model.User
import com.ssl.smarttaskreminder.data.repository.ManagerRepository
import com.ssl.smarttaskreminder.data.repository.TaskRepository
import com.ssl.smarttaskreminder.data.repository.UserRepository
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val managerRepo = ManagerRepository()
    private val userRepo    = UserRepository()
    private val taskRepo    = TaskRepository()

    private val _managers = MutableLiveData<List<Manager>>()
    val managers: LiveData<List<Manager>> = _managers

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> = _users

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    // Analytics data
    private val _pendingCount   = MutableLiveData<Int>()
    val pendingCount: LiveData<Int> = _pendingCount

    private val _overdueCount   = MutableLiveData<Int>()
    val overdueCount: LiveData<Int> = _overdueCount

    private val _completedCount = MutableLiveData<Int>()
    val completedCount: LiveData<Int> = _completedCount

    private val _completionPct  = MutableLiveData<Float>()
    val completionPct: LiveData<Float> = _completionPct

    private val _topOverdueManagers = MutableLiveData<List<Pair<String, Int>>>() // name, count
    val topOverdueManagers: LiveData<List<Pair<String, Int>>> = _topOverdueManagers

    private val _bestPerformers = MutableLiveData<List<Pair<String, Int>>>()
    val bestPerformers: LiveData<List<Pair<String, Int>>> = _bestPerformers

    private val _worstPerformers = MutableLiveData<List<Pair<String, Int>>>()
    val worstPerformers: LiveData<List<Pair<String, Int>>> = _worstPerformers

    private val _departmentHealthScores = MutableLiveData<Map<String, Float>>()
    val departmentHealthScores: LiveData<Map<String, Float>> = _departmentHealthScores

    fun loadManagers() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _managers.value = managerRepo.getManagersByCompany(SessionManager.companyId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadUsers() {
        _loading.value = true
        viewModelScope.launch {
            try {
                _users.value = userRepo.getUsersByCompany(SessionManager.companyId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadDashboardStats() {
        viewModelScope.launch {
            try {
                val tasks = taskRepo.getAllTasksByCompany(SessionManager.companyId)
                _pendingCount.value   = tasks.count { it.status == AppConstants.STATUS_PENDING }
                _overdueCount.value   = tasks.count { it.status == AppConstants.STATUS_OVERDUE }
                _completedCount.value = tasks.count { it.status == AppConstants.STATUS_COMPLETED }
                _completionPct.value  = taskRepo.getCompletionPercentage(SessionManager.companyId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun loadAnalytics() {
        viewModelScope.launch {
            try {
                val companyId = SessionManager.companyId
                val managers  = managerRepo.getManagersByCompany(companyId)
                val users     = userRepo.getUsersByCompany(companyId)
                val allTasks  = taskRepo.getAllTasksByCompany(companyId)

                val managerMap = managers.associateBy({ it.mid }, { it.name })
                val userMap    = users.associateBy({ it.documentId }, { it.name })
                
                // Dept Health Scores
                val deptMap = mutableMapOf<String, MutableList<com.ssl.smarttaskreminder.data.model.Task>>()
                allTasks.forEach { task ->
                    val manager = managers.find { it.mid == task.managerId }
                    val dept = manager?.department ?: "General"
                    deptMap.getOrPut(dept) { mutableListOf() }.add(task)
                }
                
                val healthScores = deptMap.mapValues { (_, tasks) ->
                    if (tasks.isEmpty()) 0f
                    else {
                        val completed = tasks.count { it.status == AppConstants.STATUS_COMPLETED }
                        (completed.toFloat() / tasks.size.toFloat()) * 100f
                    }
                }
                _departmentHealthScores.value = healthScores

                // Top overdue by manager
                val overdueByMgr = taskRepo.getOverdueCountByManager(companyId)
                _topOverdueManagers.value = overdueByMgr.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { (mid, count) -> Pair(managerMap[mid] ?: "Manager $mid", count) }

                // Best performers
                val onTimeByUser = taskRepo.getOnTimeCountByUser(companyId)
                _bestPerformers.value = onTimeByUser.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { (uid, count) -> Pair(userMap[uid] ?: "User", count) }

                // Worst performers
                val lateByUser = taskRepo.getLateCountByUser(companyId)
                _worstPerformers.value = lateByUser.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { (uid, count) -> Pair(userMap[uid] ?: "User", count) }

            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createManager(name: String, email: String, department: String, tempPassword: String) {
        if (name.isBlank() || email.isBlank() || tempPassword.isBlank()) {
            _error.value = "All fields are required"; return
        }
        _loading.value = true
        viewModelScope.launch {
            try {
                managerRepo.createManager(
                    companyId    = SessionManager.companyId,
                    name         = name,
                    email        = email,
                    department   = department,
                    tempPassword = tempPassword,
                    createdByUid = SessionManager.firebaseUid
                )
                _success.value = "Manager $name created!"
                loadManagers()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateManager(managerUid: String, name: String, email: String, department: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                managerRepo.updateManager(managerUid, name, email, department)
                _success.value = "Manager updated!"
                loadManagers()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteManager(managerUid: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                managerRepo.deleteManager(managerUid)
                _success.value = "Manager removed!"
                loadManagers()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun createUser(name: String, email: String, tempPassword: String, managerId: Int) {
        if (name.isBlank() || email.isBlank() || tempPassword.isBlank()) {
            _error.value = "All fields are required"; return
        }
        _loading.value = true
        viewModelScope.launch {
            try {
                userRepo.createUser(
                    companyId    = SessionManager.companyId,
                    name         = name,
                    email        = email,
                    tempPassword = tempPassword,
                    managerId    = managerId
                )
                _success.value = "User $name created!"
                loadUsers()
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun updateUser(userUid: String, name: String, email: String, managerId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                userRepo.updateUser(userUid, name, email, managerId)
                _success.value = "User updated!"
                loadUsers()
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

    fun deleteUser(userUid: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                userRepo.deleteUser(userUid)
                _success.value = "User removed!"
                loadUsers()
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

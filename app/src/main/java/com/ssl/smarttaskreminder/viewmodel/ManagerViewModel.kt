package com.ssl.smarttaskreminder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.data.model.Task
import com.ssl.smarttaskreminder.data.model.User
import com.ssl.smarttaskreminder.data.repository.TaskRepository
import com.ssl.smarttaskreminder.data.repository.UserRepository
import kotlinx.coroutines.launch

class ManagerViewModel : ViewModel() {

    private val taskRepo = TaskRepository()
    private val userRepo = UserRepository()

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _teamUsers = MutableLiveData<List<User>>()
    val teamUsers: LiveData<List<User>> = _teamUsers

    private val _pendingCount   = MutableLiveData<Int>()
    val pendingCount: LiveData<Int> = _pendingCount

    private val _overdueCount   = MutableLiveData<Int>()
    val overdueCount: LiveData<Int> = _overdueCount

    private val _completedCount = MutableLiveData<Int>()
    val completedCount: LiveData<Int> = _completedCount

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _success = MutableLiveData<String?>()
    val success: LiveData<String?> = _success

    fun loadTeamData() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val companyId = SessionManager.companyId
                val managerId = SessionManager.managerId

                val tasks = taskRepo.getTasksByManager(companyId, managerId)
                _tasks.value = tasks
                _pendingCount.value   = tasks.count { it.status == AppConstants.STATUS_PENDING }
                _overdueCount.value   = tasks.count { it.status == AppConstants.STATUS_OVERDUE }
                _completedCount.value = tasks.count { it.status == AppConstants.STATUS_COMPLETED }

                _teamUsers.value = userRepo.getUsersByManager(companyId, managerId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadTeamUsers() {
        viewModelScope.launch {
            try {
                _teamUsers.value = userRepo.getUsersByManager(
                    SessionManager.companyId,
                    SessionManager.managerId
                )
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _success.value = null
    }
}

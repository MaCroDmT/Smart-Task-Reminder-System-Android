package com.ssl.smarttaskreminder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.SessionManager
import com.ssl.smarttaskreminder.data.model.Task
import com.ssl.smarttaskreminder.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel : ViewModel() {

    private val taskRepo = TaskRepository()

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> = _tasks

    private val _selectedTask = MutableLiveData<Task?>()
    val selectedTask: LiveData<Task?> = _selectedTask

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

    /** Loads tasks for the currently logged-in user. */
    fun loadMyTasks() {
        _loading.value = true
        viewModelScope.launch {
            try {
                // Load tasks created by the current user (using their Firebase UID)
                val tasks = taskRepo.getTasksByUser(SessionManager.companyId, SessionManager.firebaseUid)
                _tasks.value = tasks
                updateCounts(tasks)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /** Loads all tasks for the company (Admin view). */
    fun loadAllTasks() {
        _loading.value = true
        viewModelScope.launch {
            try {
                val tasks = taskRepo.getAllTasksByCompany(SessionManager.companyId)
                _tasks.value = tasks
                updateCounts(tasks)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /** Loads team tasks for a manager. */
    fun loadManagerTasks(managerId: Int) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val tasks = taskRepo.getTasksByManager(SessionManager.companyId, managerId)
                _tasks.value = tasks
                updateCounts(tasks)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    /** Loads tasks filtered by a specific status. */
    fun loadTasksByStatus(status: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val tasks = taskRepo.getTasksByStatus(SessionManager.companyId, status)
                _tasks.value = tasks
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadTask(documentId: String) {
        viewModelScope.launch {
            try {
                _selectedTask.value = taskRepo.getTask(documentId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun createTask(
        taskName: String,
        styleNo: String,
        details: String,
        importance: String,
        deadline: java.util.Date,
        managerId: Int
    ) {
        if (taskName.isBlank()) { _error.value = "Task name is required"; return }
        _loading.value = true
        viewModelScope.launch {
            try {
                val newTask = taskRepo.createTask(
                    companyId  = SessionManager.companyId,
                    taskName   = taskName,
                    styleNo    = styleNo,
                    details    = details,
                    importance = importance,
                    deadline   = deadline,
                    createdBy  = SessionManager.firebaseUid,
                    managerId  = managerId
                )
                _selectedTask.value = newTask
                _success.value = "Task created successfully!"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun completeTask(documentId: String, deadline: com.google.firebase.Timestamp?) {
        _loading.value = true
        viewModelScope.launch {
            try {
                taskRepo.completeTask(documentId, deadline)
                _success.value = "Task marked as completed!"
                loadTask(documentId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun deleteTask(documentId: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                taskRepo.deleteTask(documentId)
                _success.value = "Task deleted!"
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    private fun updateCounts(tasks: List<Task>) {
        _pendingCount.value   = tasks.count { it.status == AppConstants.STATUS_PENDING }
        _overdueCount.value   = tasks.count { it.status == AppConstants.STATUS_OVERDUE }
        _completedCount.value = tasks.count { it.status == AppConstants.STATUS_COMPLETED }
    }

    fun clearMessages() {
        _error.value = null
        _success.value = null
    }
}

package com.ssl.smarttaskreminder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssl.smarttaskreminder.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState

    sealed class LoginState {
        object Loading : LoginState()
        data class Success(val role: String) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val role = repo.login(email, password)
                _loginState.value = LoginState.Success(role)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Login failed")
            }
        }
    }

    fun resolveCurrentUser() {
        viewModelScope.launch {
            try {
                val role = repo.resolveCurrentUser()
                if (role != null) {
                    _loginState.value = LoginState.Success(role)
                } else {
                    _loginState.value = LoginState.Error("Not signed in")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Session expired")
            }
        }
    }

    fun logout() = repo.logout()

    fun isSignedIn(): Boolean = repo.isSignedIn()
}

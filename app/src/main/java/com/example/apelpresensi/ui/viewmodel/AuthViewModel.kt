package com.example.apelpresensi.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.dto.AuthResponse
import com.example.apelpresensi.data.remote.dto.LoginRequest
import com.example.apelpresensi.data.remote.dto.RegisterRequest
import com.example.apelpresensi.data.repository.AuthRepository
import kotlinx.coroutines.launch

// State untuk memantau status autentikasi di UI
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val data: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel(
    private val repository: AuthRepository,
    private val prefManager: PreferenceManager
) : ViewModel() {
    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    val authData = response.body()!!

                    // Simpan token JWT dan role ke PreferenceManager
                    prefManager.saveAuthToken(authData.token)
                    prefManager.saveUserData(username, authData.role)

                    _authState.value = AuthState.Success(authData)
                } else {
                    _authState.value = AuthState.Error("Login Gagal: Username atau password salah")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Terjadi kesalahan jaringan: ${e.message}")
            }
        }
    }

    // Di dalam class AuthViewModel
    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = repository.register(request)
                if (response.isSuccessful && response.body() != null) {
                    _authState.value = AuthState.Success(response.body()!!)
                } else {
                    _authState.value = AuthState.Error("Registrasi Gagal: Data tidak valid atau NIM sudah terdaftar")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Kesalahan jaringan: ${e.message}")
            }
        }
    }

    fun logout() {
        prefManager.clearSession()
        _authState.value = AuthState.Idle
    }
}
package com.example.apelpresensi.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.RetrofitClient
import com.example.apelpresensi.data.remote.dto.MahasiswaResponse
import kotlinx.coroutines.launch

sealed class MahasiswaState {
    object Loading : MahasiswaState()
    data class Success(val data: MahasiswaResponse) : MahasiswaState()
    data class Error(val message: String) : MahasiswaState()
}

class MahasiswaViewModel(private val prefManager: PreferenceManager) : ViewModel() {
    private val _profileState = mutableStateOf<MahasiswaState>(MahasiswaState.Loading)
    val profileState: State<MahasiswaState> = _profileState

    fun fetchProfile() {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getMyProfile("Bearer $token")
                if (response.isSuccessful) {
                    _profileState.value = MahasiswaState.Success(response.body()!!)
                } else {
                    _profileState.value = MahasiswaState.Error("Gagal mengambil profil")
                }
            } catch (e: Exception) {
                _profileState.value = MahasiswaState.Error("Error: ${e.message}")
            }
        }
    }
}
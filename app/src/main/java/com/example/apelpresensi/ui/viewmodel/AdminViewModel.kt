package com.example.apelpresensi.ui.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.dto.IzinResponse
import com.example.apelpresensi.data.remote.dto.JadwalRequest
import com.example.apelpresensi.data.remote.dto.JadwalResponse
import com.example.apelpresensi.data.remote.dto.PresensiResponse
import com.example.apelpresensi.data.repository.AdminRepository
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: AdminRepository,
    private val prefManager: PreferenceManager
) : ViewModel() {
    var jadwalList by mutableStateOf<List<JadwalResponse>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var izinList by mutableStateOf<List<IzinResponse>>(emptyList())

    fun fetchJadwal() {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.getAllJadwal(token)
                if (response.isSuccessful) jadwalList = response.body() ?: emptyList()
            } catch (e: Exception) { errorMessage = e.message }
            isLoading = false
        }
    }
    fun addJadwal(tanggal: String, waktu: String, tingkat: String, ket: String) {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val request = JadwalRequest(tanggal, waktu, tingkat, ket)
                val response = repository.createJadwal(token, request)
                if (response.isSuccessful) fetchJadwal()
            } catch (e: Exception) { errorMessage = e.message }
        }
    }
    // Tambahkan di dalam class AdminViewModel
    fun deleteJadwal(id: Long) {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = repository.deleteJadwal(token, id)
                if (response.isSuccessful) {
                    fetchJadwal() // Refresh daftar setelah menghapus
                } else {
                    errorMessage = if (response.code() == 500 || response.code() == 409) {
                        "Jadwal tidak bisa dihapus karena sudah memiliki data presensi mahasiswa."
                    } else {
                        "Gagal menghapus jadwal: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            }
        }
    }

    private val _rekapList = mutableStateOf<List<PresensiResponse>>(emptyList())
    val rekapList: State<List<PresensiResponse>> = _rekapList

    fun fetchRekap(scheduleId: Long) {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                isLoading = true
                val response = repository.getRekap(token, scheduleId)
                if (response.isSuccessful) {
                    _rekapList.value = response.body() ?: emptyList()
                } else {
                    errorMessage = "Gagal memuat rekap: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Koneksi gagal: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    fun fetchIzin() {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            isLoading = true
            try {
                val response = repository.getAllIzin(token)
                if (response.isSuccessful) izinList = response.body() ?: emptyList()
            } catch (e: Exception) { errorMessage = e.message }
            isLoading = false
        }
    }

    fun validateIzin(id: Long, status: String) {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = repository.validateIzin(token, id, status)
                if (response.isSuccessful) fetchIzin() // Refresh data
            } catch (e: Exception) { errorMessage = e.message }
        }
    }
}
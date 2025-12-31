package com.example.apelpresensi.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.dto.ApelSchedule
import com.example.apelpresensi.data.remote.dto.PresensiRecordResponse
import com.example.apelpresensi.data.remote.dto.ScanResponse
import com.example.apelpresensi.data.repository.PresensiRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class ScannerState {
    object Idle : ScannerState()
    object Loading : ScannerState()
    data class Success(val data: ScanResponse) : ScannerState()
    data class NeedConfirmation(val data: ScanResponse) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

class SpdViewModel(
    private val repository: PresensiRepository,
    private val prefManager: PreferenceManager
) : ViewModel() {

    private val _scannerState = mutableStateOf<ScannerState>(ScannerState.Idle)
    val scannerState: State<ScannerState> = _scannerState

    private val _availableSchedules = mutableStateOf<List<ApelSchedule>>(emptyList())
    val availableSchedules: State<List<ApelSchedule>> = _availableSchedules
    var searchQuery by mutableStateOf("")
        private set
    private val scannedNims = mutableSetOf<String>()
    private val _scheduleHistory = mutableStateOf<List<PresensiRecordResponse>>(emptyList())
    val scheduleHistory: State<List<PresensiRecordResponse>> = _scheduleHistory

    var isRefreshing by mutableStateOf(false)
        private set

    fun fetchTodaySchedules() {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = repository.getSchedulesForSpd(token)
                if (response.isSuccessful) {
                    _availableSchedules.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error("Gagal memuat jadwal")
            }
        }
    }

    fun refreshScreen() {
        viewModelScope.launch {
            isRefreshing = true
            fetchTodaySchedules()
            delay(500)
            isRefreshing = false
        }
    }

    fun fetchHistoryBySchedule(scheduleId: Long) {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = repository.getHistoryBySchedule(token, scheduleId)
                if (response.isSuccessful) {
                    _scheduleHistory.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) { /* Handle error */ }
        }
    }

    fun scanQR(nim: String, scheduleId: Long) {
        if (scannedNims.contains(nim)) {
            _scannerState.value = ScannerState.Error("Mahasiswa NIM $nim sudah di-scan")
            return
        }

        val token = prefManager.getAuthToken() ?: return
        _scannerState.value = ScannerState.Loading

        viewModelScope.launch {
            try {
                val response = repository.scanQR(token, nim, scheduleId)
                if (response.isSuccessful) {
                    val body = response.body()
                    // Tangani status dari backend secara spesifik
                    when (body?.status) {
                        "NEED_CONFIRMATION" -> _scannerState.value = ScannerState.NeedConfirmation(body)
                        "ALREADY_PRESENT" -> _scannerState.value = ScannerState.Error("Mahasiswa sudah presensi")
                        else -> {
                            scannedNims.add(nim)
                            _scannerState.value = ScannerState.Success(body!!)
                        }
                    }
                } else {
                    _scannerState.value = ScannerState.Error("QR Tidak Valid / Gagal Scan")
                }
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error("Masalah Jaringan: ${e.message}")
            }
        }
    }

    fun confirmManual(nim: String, scheduleId: Long, status: String) {
        val token = prefManager.getAuthToken() ?: return
        _scannerState.value = ScannerState.Loading

        viewModelScope.launch {
            try {
                val response = repository.confirmPresensi(token, nim, scheduleId, status)
                if (response.isSuccessful) {
                    scannedNims.add(nim)
                    // FIX: Gunakan body dari response yang sekarang sudah berupa ScanResponse JSON
                    _scannerState.value = ScannerState.Success(response.body()!!)
                } else {
                    _scannerState.value = ScannerState.Error("Gagal mengirim konfirmasi")
                }
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error("Kesalahan Jaringan")
            }
        }
    }

    val filteredHistory by derivedStateOf {
        if (searchQuery.isEmpty()) {
            scheduleHistory.value
        } else {
            scheduleHistory.value.filter {
                it.nama?.contains(searchQuery, ignoreCase = true) == true ||
                        it.nim.contains(searchQuery)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
    }

    fun deletePresensi(presensiId: Long, scheduleId: Long) {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = repository.deletePresensi(token, presensiId)
                if (response.isSuccessful) {
                    // Refresh list setelah dihapus
                    fetchHistoryBySchedule(scheduleId)
                }
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error("Gagal menghapus data")
            }
        }
    }

    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }

    fun clearScannedList() {
        scannedNims.clear()
        _scannerState.value = ScannerState.Idle
    }
}
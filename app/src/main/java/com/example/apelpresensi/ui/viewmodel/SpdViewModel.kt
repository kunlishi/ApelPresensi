package com.example.apelpresensi.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.RetrofitClient
import com.example.apelpresensi.data.remote.dto.PresensiRequest
import com.example.apelpresensi.data.remote.dto.PresensiResponse
import com.example.apelpresensi.data.repository.PresensiRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed class ScannerState {
    object Idle : ScannerState()
    object Loading : ScannerState()
    data class Success(val data: PresensiResponse) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

class SpdViewModel(
    private val repository: PresensiRepository,
    private val prefManager: PreferenceManager
) : ViewModel() {
    private val _scannerState = mutableStateOf<ScannerState>(ScannerState.Idle)
    val scannerState: State<ScannerState> = _scannerState

    fun processScannedNim(nim: String, tingkat: String, isTerlambat: Boolean = false) {
        val token = prefManager.getAuthToken() ?: return
        val tanggal = LocalDate.now().toString()
        val request = PresensiRequest(tanggal, tingkat, nim)

        viewModelScope.launch {
            _scannerState.value = ScannerState.Loading
            try {
                val response = if (isTerlambat) {
                    RetrofitClient.apiService.markTerlambat("Bearer $token", request)
                } else {
                    RetrofitClient.apiService.submitPresensi("Bearer $token", request)
                }

                if (response.isSuccessful) {
                    _scannerState.value = ScannerState.Success(response.body()!!)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Gagal mencatat presensi"
                    _scannerState.value = ScannerState.Error(errorMsg)
                }
            } catch (e: Exception) {
                _scannerState.value = ScannerState.Error("Koneksi gagal: ${e.message}")
            }
        }
    }

    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}
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

    // Set untuk menyimpan NIM yang sudah berhasil di-scan dalam sesi ini
    private val scannedNims = mutableSetOf<String>()

    fun processScannedNim(nim: String, tingkat: String, isTerlambat: Boolean = false) {
        // 1. Validasi Lokal: Cek apakah NIM sudah ada di daftar sukses
        if (scannedNims.contains(nim)) {
            _scannerState.value = ScannerState.Error("Mahasiswa dengan NIM $nim sudah berhasil di-input sebelumnya!")
            return
        }

        val token = prefManager.getAuthToken() ?: return
        val tanggal = LocalDate.now().toString()
        val request = PresensiRequest(tanggal, tingkat, nim)

        viewModelScope.launch {
            _scannerState.value = ScannerState.Loading
            try {
                // Menggunakan repository secara konsisten
                val response = if (isTerlambat) {
                    repository.markTerlambat(token, request)
                } else {
                    repository.recordPresensi(token, request)
                }

                if (response.isSuccessful) {
                    // 2. Jika sukses, masukkan NIM ke dalam daftar pengecekan
                    scannedNims.add(nim)
                    _scannerState.value = ScannerState.Success(response.body()!!)
                } else {
                    // Tangani jika server mengirimkan pesan "sudah presensi" (misal HTTP 400)
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

    // Fungsi untuk membersihkan daftar scan jika petugas berganti tingkat
    fun clearScannedList() {
        scannedNims.clear()
        _scannerState.value = ScannerState.Idle
    }
}
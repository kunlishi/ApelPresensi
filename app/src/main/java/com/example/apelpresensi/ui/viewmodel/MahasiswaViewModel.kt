package com.example.apelpresensi.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.dto.MahasiswaResponse
import com.example.apelpresensi.data.remote.dto.PresensiResponse
import com.example.apelpresensi.data.repository.MahasiswaRepository
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.emptyList

// State untuk Profil
sealed class MahasiswaState {
    object Loading : MahasiswaState()
    data class Success(val data: MahasiswaResponse) : MahasiswaState()
    data class Error(val message: String) : MahasiswaState()
}

// State untuk Upload Izin
sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

class MahasiswaViewModel(
    private val repository: MahasiswaRepository,
    private val prefManager: PreferenceManager
) : ViewModel() {

    // State Profil
    private val _profileState = mutableStateOf<MahasiswaState>(MahasiswaState.Loading)
    val profileState: State<MahasiswaState> = _profileState

    // State Upload (Sekarang sudah di dalam class)
    private val _uploadStatus = mutableStateOf<UploadState>(UploadState.Idle)
    val uploadStatus: State<UploadState> = _uploadStatus

    // Fungsi untuk mereset status upload (Dibutuhkan oleh IzinScreen)
    fun resetUploadStatus() {
        _uploadStatus.value = UploadState.Idle
    }

    fun fetchProfile() {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            _profileState.value = MahasiswaState.Loading
            try {
                // Gunakan repository agar konsisten dengan arsitektur Anda
                val response = repository.getProfile(token)
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

    fun uploadIzin(context: Context, uri: Uri, keterangan: String) {
        val token = prefManager.getAuthToken() ?: return
        val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            _uploadStatus.value = UploadState.Loading
            try {
                val filePart = prepareFilePart(context, "file", uri)
                val response = repository.submitIzin(token, keterangan, tanggal, filePart)

                if (response.isSuccessful) {
                    _uploadStatus.value = UploadState.Success("Pengajuan izin berhasil dikirim!")
                } else {
                    _uploadStatus.value = UploadState.Error("Gagal mengirim: ${response.message()}")
                }
            } catch (e: Exception) {
                _uploadStatus.value = UploadState.Error("Terjadi kesalahan jaringan.")
            }
        }
    }

    private fun prepareFilePart(context: Context, partName: String, fileUri: Uri): MultipartBody.Part {
        // 1. Dapatkan tipe MIME asli dari file (image/jpeg, application/pdf, dll)
        val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"

        // 2. Tentukan ekstensi file sementara berdasarkan tipe MIME
        val extension = when (mimeType) {
            "application/pdf" -> ".pdf"
            "image/png" -> ".png"
            else -> ".jpg"
        }

        val inputStream = context.contentResolver.openInputStream(fileUri)
        val tempFile = File.createTempFile("upload_", extension, context.cacheDir)
        tempFile.outputStream().use { output ->
            inputStream?.copyTo(output)
        }

        // 3. Gunakan mimeType yang dinamis untuk requestBody
        val requestFile = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

        return MultipartBody.Part.createFormData(partName, tempFile.name, requestFile)
    }
    private val _riwayatList = mutableStateOf<List<PresensiResponse>>(emptyList())
    val riwayatList: State<List<PresensiResponse>> = _riwayatList

    fun fetchRiwayat() {
        val token = prefManager.getAuthToken() ?: return
        viewModelScope.launch {
            try {
                val response = repository.getRiwayat(token)
                if (response.isSuccessful) {
                    _riwayatList.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                // Handle error jika diperlukan
            }
        }
    }
}
package com.example.apelpresensi.data.repository

import com.example.apelpresensi.data.remote.ApiService
import com.example.apelpresensi.data.remote.dto.MahasiswaResponse
import com.example.apelpresensi.data.remote.dto.PresensiResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class MahasiswaRepository(private val apiService: ApiService) {
    suspend fun getProfile(token: String): Response<MahasiswaResponse> {
        return apiService.getMyProfile("Bearer $token")
    }
    suspend fun submitIzin(
        token: String,
        keterangan: String,
        tanggal: String,
        file: MultipartBody.Part
    ): Response<Void> {
        val keteranganBody = keterangan.toRequestBody("text/plain".toMediaTypeOrNull())
        val tanggalBody = tanggal.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.submitIzin("Bearer $token", keteranganBody, tanggalBody, file)
    }
    suspend fun getRiwayat(token: String): Response<List<PresensiResponse>> {
        return apiService.getRiwayatPresensi("Bearer $token")
    }

}
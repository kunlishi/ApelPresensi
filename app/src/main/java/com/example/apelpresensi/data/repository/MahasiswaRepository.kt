package com.example.apelpresensi.data.repository

import com.example.apelpresensi.data.remote.ApiService
import com.example.apelpresensi.data.remote.dto.IzinResponse
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
        tanggal: String,
        tingkat: String,
        jenis: String,
        keterangan: String,
        file: MultipartBody.Part
    ): Response<Long> {
        val tingkatBody = tingkat.toRequestBody("text/plain".toMediaTypeOrNull())
        val keteranganBody = keterangan.toRequestBody("text/plain".toMediaTypeOrNull())
        val tanggalBody = tanggal.toRequestBody("text/plain".toMediaTypeOrNull())
        val jenisBody = jenis.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.submitIzin("Bearer $token", tanggalBody, tingkatBody, jenisBody, keteranganBody, file)
    }
    suspend fun getRiwayat(token: String): Response<List<PresensiResponse>> {
        return apiService.getRiwayatPresensi("Bearer $token")
    }

    suspend fun getMyIzin(token: String): Response<List<IzinResponse>> {
        return apiService.getMyIzin("Bearer $token")
    }
}
package com.example.apelpresensi.data.repository

import com.example.apelpresensi.data.remote.ApiService
import com.example.apelpresensi.data.remote.dto.IzinResponse
import com.example.apelpresensi.data.remote.dto.JadwalRequest
import com.example.apelpresensi.data.remote.dto.JadwalResponse
import com.example.apelpresensi.data.remote.dto.PresensiResponse
import retrofit2.Response

class AdminRepository(private val apiService: ApiService) {
    suspend fun createJadwal(token: String, request: JadwalRequest): Response<Long> =
        apiService.createJadwal("Bearer $token", request)

    suspend fun getAllJadwal(token: String): Response<List<JadwalResponse>> =
        apiService.listJadwal("Bearer $token")

    suspend fun deleteJadwal(token: String, id: Long): Response<Void> =
        apiService.deleteJadwal("Bearer $token", id)

    suspend fun getRekap(token: String, scheduleId: Long): Response<List<PresensiResponse>> {
        return apiService.getRekapByJadwal("Bearer $token", scheduleId)
    }

    suspend fun getAllIzin(token: String): Response<List<IzinResponse>> {
        return apiService.getAllIzin("Bearer $token")
    }

    suspend fun validateIzin(token: String, id: Long, status: String): Response<Unit> {
        return apiService.validateIzin("Bearer $token", id, status)
    }
}
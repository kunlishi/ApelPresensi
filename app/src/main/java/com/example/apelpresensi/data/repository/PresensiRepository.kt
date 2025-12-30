package com.example.apelpresensi.data.repository

import com.example.apelpresensi.data.remote.ApiService
import com.example.apelpresensi.data.remote.dto.PresensiRequest
import com.example.apelpresensi.data.remote.dto.PresensiResponse
import retrofit2.Response

class PresensiRepository(private val apiService: ApiService) {
    suspend fun recordPresensi(token: String, request: PresensiRequest): Response<PresensiResponse> {
        return apiService.submitPresensi("Bearer $token", request)
    }

    suspend fun markTerlambat(token: String, request: PresensiRequest): Response<PresensiResponse> {
        return apiService.markTerlambat("Bearer $token", request)
    }

    suspend fun scanQR(token: String, nim: String, scheduleId: Long) =
        apiService.scanQR("Bearer $token", nim, scheduleId)

    suspend fun confirmPresensi(token: String, nim: String, scheduleId: Long, status: String) =
        apiService.confirmPresensi("Bearer $token", nim, scheduleId, status)
}
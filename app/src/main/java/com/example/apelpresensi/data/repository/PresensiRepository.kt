package com.example.apelpresensi.data.repository

import com.example.apelpresensi.data.remote.ApiService
import com.example.apelpresensi.data.remote.dto.ApelSchedule
import com.example.apelpresensi.data.remote.dto.PresensiRecordResponse
import com.example.apelpresensi.data.remote.dto.PresensiRequest
import com.example.apelpresensi.data.remote.dto.PresensiResponse
import retrofit2.Response

class PresensiRepository(private val apiService: ApiService) {
    suspend fun getSchedulesForSpd(token: String, date: String? = null): Response<List<ApelSchedule>> {
        return apiService.getSchedulesForSpd("Bearer $token", date)
    }

    suspend fun getHistoryBySchedule(token: String, scheduleId: Long): Response<List<PresensiRecordResponse>> {
        return apiService.getHistoryBySchedule("Bearer $token", scheduleId)
    }

    suspend fun scanQR(token: String, nim: String, scheduleId: Long) =
        apiService.scanQR("Bearer $token", nim, scheduleId)

    suspend fun confirmPresensi(token: String, nim: String, scheduleId: Long, status: String) =
        apiService.confirmPresensi("Bearer $token", nim, scheduleId, status)

    suspend fun deletePresensi(token: String, presensiId: Long) =
        apiService.deletePresensi("Bearer $token", presensiId)
}
package com.example.apelpresensi.data.remote

import com.example.apelpresensi.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/user/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @PUT("api/user/password")
    suspend fun changePassword(
        @Header("Authorization") token: String,
        @Body request: ChangePasswordRequest
    ): Response<Unit>

    @POST("api/admin/jadwal")
    suspend fun createJadwal(
        @Header("Authorization") token: String,
        @Body request: JadwalRequest
    ): Response<Long>

    @GET("api/admin/jadwal")
    suspend fun listJadwal(
        @Header("Authorization") token: String
    ): Response<List<JadwalResponse>>

    @DELETE("api/admin/jadwal/{id}")
    suspend fun deleteJadwal(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    @GET("api/admin/presensi/rekap/{scheduleId}")
    suspend fun getRekapByJadwal(
        @Header("Authorization") token: String,
        @Path("scheduleId") scheduleId: Long
    ): Response<List<PresensiResponse>>

    @GET("api/admin/rekap/{scheduleId}")
    suspend fun getRekapPresensi(
        @Header("Authorization") token: String,
        @Path("scheduleId") scheduleId: Long
    ): Response<List<PresensiDetailResponse>>

    @GET("api/admin/izin")
    suspend fun getAllIzin(
        @Header("Authorization") token: String
    ): Response<List<IzinResponse>>

    @PUT("api/admin/izin/{id}") // Hapus /validate
    suspend fun validateIzin(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Query("status") status: String, // MENUNGGU, DITERIMA, DITOLAK
        @Query("catatanAdmin") catatanAdmin: String?
    ): Response<Unit>

    @POST("api/spd/presensi")
    suspend fun submitPresensi(
        @Header("Authorization") token: String,
        @Body request: PresensiRequest
    ): Response<PresensiResponse>

    @POST("api/spd/terlambat")
    suspend fun markTerlambat(
        @Header("Authorization") token: String,
        @Body request: PresensiRequest
    ): Response<PresensiResponse>

    @POST("api/spd/scan")
    suspend fun scanQR(
        @Header("Authorization") token: String,
        @Query("nim") nim: String,
        @Query("scheduleId") scheduleId: Long
    ): Response<ScanResponse>

    @POST("api/spd/confirm")
    suspend fun confirmPresensi(
        @Header("Authorization") token: String,
        @Query("nim") nim: String,
        @Query("scheduleId") scheduleId: Long,
        @Query("status") status: String // "HADIR" atau "TERLAMBAT"
    ): Response<ScanResponse>

    @GET("api/spd/schedules")
    suspend fun getSchedulesForSpd(
        @Header("Authorization") token: String,
        @Query("date") date: String? = null // Optional, default hari ini di backend
    ): Response<List<ApelSchedule>>

    @GET("api/spd/schedules/{scheduleId}/presensi")
    suspend fun getHistoryBySchedule(
        @Header("Authorization") token: String,
        @Path("scheduleId") scheduleId: Long
    ): Response<List<PresensiRecordResponse>>

    @DELETE("api/spd/presensi/{id}")
    suspend fun deletePresensi(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Response<Void>

    @GET("api/mahasiswa/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<MahasiswaResponse>

    @Multipart
    @POST("api/mahasiswa/izin")
    suspend fun submitIzin(
        @Header("Authorization") token: String,
        @Part("tanggal") tanggal: RequestBody,
        @Part("tingkat") tingkat: RequestBody,
        @Part("jenis") jenis: RequestBody,           // Mengirim "IZIN" atau "SAKIT"
        @Part("keterangan") keterangan: RequestBody, // Mengirim deskripsi detail
        @Part bukti: MultipartBody.Part              // Nama part harus 'bukti'
    ): Response<Long>

    @GET("api/mahasiswa/riwayat")
    suspend fun getRiwayatPresensi(
        @Header("Authorization") token: String
    ): Response<List<PresensiResponse>>

    @GET("api/mahasiswa/izin/me")
    suspend fun getMyIzin(
        @Header("Authorization") token: String
    ): Response<List<IzinResponse>>
}
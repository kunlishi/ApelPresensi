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

    @GET("api/admin/izin")
    suspend fun getAllIzin(
        @Header("Authorization") token: String
    ): Response<List<IzinResponse>> // IzinResponse sudah ada di MahasiswaDto.kt

    @PUT("api/admin/izin/{id}/validate")
    suspend fun validateIzin(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Query("status") status: String // "APPROVED" atau "REJECTED"
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

    @GET("api/mahasiswa/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<MahasiswaResponse>

    @Multipart
    @POST("api/mahasiswa/izin")
    suspend fun submitIzin(
        @Header("Authorization") token: String,
        @Part("alasan") alasan: RequestBody, // Ubah dari 'keterangan' ke 'alasan'
        @Part("tanggal") tanggal: RequestBody,
        @Part("jenis") jenis: RequestBody,   // Tambahkan parameter jenis
        @Part file: MultipartBody.Part
    ): Response<Void>

    @GET("api/mahasiswa/riwayat")
    suspend fun getRiwayatPresensi(
        @Header("Authorization") token: String
    ): Response<List<PresensiResponse>>
}
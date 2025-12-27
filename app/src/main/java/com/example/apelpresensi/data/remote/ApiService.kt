package com.example.apelpresensi.data.remote

import com.example.apelpresensi.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Header

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/mahasiswa/me")
    suspend fun getMyProfile(
        @Header("Authorization") token: String
    ): Response<MahasiswaResponse>

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
}
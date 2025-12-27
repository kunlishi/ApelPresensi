package com.example.apelpresensi.data.remote

import com.example.apelpresensi.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
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
}
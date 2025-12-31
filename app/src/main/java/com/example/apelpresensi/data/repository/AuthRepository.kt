package com.example.apelpresensi.data.repository

import com.example.apelpresensi.data.remote.ApiService
import com.example.apelpresensi.data.remote.dto.AuthResponse
import com.example.apelpresensi.data.remote.dto.ChangePasswordRequest
import com.example.apelpresensi.data.remote.dto.LoginRequest
import com.example.apelpresensi.data.remote.dto.RegisterRequest
import com.example.apelpresensi.data.remote.dto.UserResponse
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {
    // Fungsi untuk login sesuai dengan AuthService backend
    suspend fun login(request: LoginRequest): Response<AuthResponse> {
        return apiService.login(request)
    }

    // Fungsi untuk registrasi mahasiswa
    suspend fun register(request: RegisterRequest): Response<AuthResponse> {
        return apiService.register(request)
    }

    suspend fun getCurrentUser(token: String): Response<UserResponse> {
        return apiService.getCurrentUser("Bearer $token")
    }

    suspend fun changePassword(token: String, request: ChangePasswordRequest): Response<Unit> {
        return apiService.changePassword("Bearer $token", request)
    }
}
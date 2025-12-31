package com.example.apelpresensi.data.remote.dto

// Untuk POST /api/auth/login
data class LoginRequest(
    val username: String,
    val password: String
)

// Untuk response dari login/register
data class AuthResponse(
    val token: String,
    val role: String
)

// Untuk POST /api/auth/register
data class RegisterRequest(
    val username: String,
    val password: String,
    val nama: String,
    val kelas: String,
    val tingkat: String
)

// Response untuk data profil user
data class UserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val role: String
)

data class ChangePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

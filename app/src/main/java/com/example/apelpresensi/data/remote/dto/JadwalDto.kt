package com.example.apelpresensi.data.remote.dto

// Request untuk membuat jadwal baru
data class JadwalRequest(
    val tanggal: String, // Format: YYYY-MM-DD
    val waktu: String,   // Format: HH:mm:ss
    val tingkat: String, // 1, 2, 3, 4
    val keterangan: String?
)

// Response untuk menampilkan daftar jadwal
data class JadwalResponse(
    val id: Long,
    val tanggalApel: String,
    val waktuApel: String,
    val tingkat: String,
    val keterangan: String?
)

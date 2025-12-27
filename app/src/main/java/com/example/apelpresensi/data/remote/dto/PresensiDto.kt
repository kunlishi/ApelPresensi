package com.example.apelpresensi.data.remote.dto

// Request untuk mencatat satu kehadiran atau keterlambatan
data class PresensiRequest(
    val tanggal: String, // Format: YYYY-MM-DD
    val tingkat: String,
    val nim: String
)

// Response detail presensi yang diterima dari server
data class PresensiResponse(
    val scheduleId: Long,
    val tanggal: String,
    val tingkat: String,
    val nim: String,
    val nama: String,
    val waktuPresensi: String,
    val status: String, // HADIR atau TERLAMBAT
    val createdBySpd: String?
)
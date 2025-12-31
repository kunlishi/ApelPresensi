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
    val kelas : String,
    val waktuPresensi: String,
    val status: String, // HADIR atau TERLAMBAT
    val createdBySpd: String?
)

data class PresensiDetailResponse(
    val nim: String,
    val nama: String,
    val kelas: String,
    val status: String, // HADIR, TERLAMBAT, IZIN, SAKIT, ALFA
    val catatan: String?
)

data class ScanResponse(
    val status: String, // HADIR, TERLAMBAT, NEED_CONFIRMATION, ALREADY_PRESENT, ERROR
    val message: String,
    val nim: String?,
    val nama: String?
)

data class PresensiRecordResponse(
    val id: Long,
    val scheduleId: Long,
    val tanggal: String,          // Format: "yyyy-MM-dd"
    val tingkat: String,
    val nim: String,
    val nama: String,
    val kelas: String? = null,    // Optional sesuai backend
    val waktuPresensi: String,    // Format ISO LocalDateTime: "yyyy-MM-dd'T'HH:mm:ss"
    val status: String,           // HADIR, TERLAMBAT, TIDAK_HADIR
    val createdBySpd: String? = null
)
package com.example.apelpresensi.data.remote.dto

data class MahasiswaResponse(
    val nim: String,
    val nama: String,
    val kelas: String,
    val tingkat: String
)

data class IzinResponse(
    val id: Long,
    val scheduleId: Long,
    val tanggal: String,
    val tingkat: String,
    val mahasiswaNim: String,
    val mahasiswaNama: String,
    val jenis: String,
    val statusBukti: String, // Samakan dengan backend
    val keterangan: String,      // Samakan dengan backend
    val catatanAdmin: String?,
    val hasBukti: Boolean,
    val buktiBase64: String?
)
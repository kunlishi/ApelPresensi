package com.example.apelpresensi.data.remote.dto

data class MahasiswaResponse(
    val nim: String,
    val nama: String,
    val kelas: String,
    val tingkat: String
)

data class IzinResponse(
    val id: Long,
    val mahasiswaNim: String,
    val mahasiswaNama: String,
    val jenis: String, // IZIN atau SAKIT
    val tanggal: String,
    val keterangan: String,
    val status: String // PENDING, APPROVED, REJECTED
)
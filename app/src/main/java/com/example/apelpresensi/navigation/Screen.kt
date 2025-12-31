package com.example.apelpresensi.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object ChangePassword: Screen("change_password")
    object AdminDashboard : Screen("admin_dashboard")
    object SpdDashboard : Screen("spd_dashboard")
    object StudentDashboard : Screen("student_dashboard")
    object Izin: Screen("izin")
    object RiwayatPresensi: Screen("riwayat_presensi")
    object RekapPresensi: Screen("rekap_presensi/{id}")
    object IzinValidation : Screen("izin_validation")
    object RiwayatIzin : Screen("riwayat_izin")
}
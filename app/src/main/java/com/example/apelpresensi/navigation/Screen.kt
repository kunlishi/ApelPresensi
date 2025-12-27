package com.example.apelpresensi.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object AdminDashboard : Screen("admin_dashboard")
    object SpdDashboard : Screen("spd_dashboard")
    object StudentDashboard : Screen("student_dashboard")
}
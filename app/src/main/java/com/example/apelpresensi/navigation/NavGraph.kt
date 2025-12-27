package com.example.apelpresensi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.apelpresensi.ui.screens.auth.LoginScreen
import com.example.apelpresensi.ui.screens.mahasiswa.StudentDashboard
import com.example.apelpresensi.ui.screens.mahasiswa.StudentQrScreen
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaState
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // Layar Login
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    // Navigasi berdasarkan role dari backend
                    when (role) {
                        "ADMIN" -> navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        "SPD" -> navController.navigate(Screen.SpdDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                        "MAHASISWA" -> navController.navigate(Screen.StudentDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // Placeholder untuk Dashboard (Akan kita isi nanti)
        composable(Screen.AdminDashboard.route) {
            // Gantilah dengan AdminDashboardScreen() nanti
            Text("Selamat Datang, Admin!")
        }

        composable(Screen.SpdDashboard.route) {
            // Gantilah dengan SpdDashboardScreen() nanti
            Text("Selamat Datang, Petugas SPD!")
        }

        composable(Screen.StudentDashboard.route) {
            val mahasiswaViewModel: MahasiswaViewModel = viewModel() // Inisialisasi ViewModel
            StudentDashboard(
                viewModel = mahasiswaViewModel,
                onShowQrClick = {
                    // Ambil NIM dari state sukses untuk di-generate jadi QR
                    val nim = (mahasiswaViewModel.profileState.value as? MahasiswaState.Success)?.data?.nim ?: ""
                    navController.navigate("qr_screen/$nim")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Tambahkan rute untuk QR Screen
        composable("qr_screen/{nim}") { backStackEntry ->
            val nim = backStackEntry.arguments?.getString("nim") ?: ""
            StudentQrScreen(
                nim = nim,
                onBackClick = { navController.popBackStack() }
            ) // Gunakan fungsi QR yang kita buat sebelumnya
        }
    }
}
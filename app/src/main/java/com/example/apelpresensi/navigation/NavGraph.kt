package com.example.apelpresensi.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.repository.AdminRepository
import com.example.apelpresensi.data.repository.PresensiRepository
import com.example.apelpresensi.ui.screens.auth.LoginScreen
import com.example.apelpresensi.ui.screens.mahasiswa.StudentDashboard
import com.example.apelpresensi.ui.screens.mahasiswa.StudentQrScreen
import com.example.apelpresensi.ui.screens.spd.SpdDashboard
import com.example.apelpresensi.ui.screens.spd.QrScannerScreen
import com.example.apelpresensi.ui.screens.admin.AdminDashboard
import com.example.apelpresensi.ui.screens.auth.RegisterScreen

import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaState
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import com.example.apelpresensi.ui.viewmodel.SpdViewModel
import com.example.apelpresensi.ui.viewmodel.AdminViewModel


@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    adminRepository: AdminRepository,
    presensiRepository: PresensiRepository,
    prefManager: PreferenceManager
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
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route) // <--- PASTIKAN INI DITULIS
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    // Setelah daftar, arahkan ke dashboard mahasiswa atau login
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // Placeholder untuk Dashboard (Akan kita isi nanti)
        composable(Screen.AdminDashboard.route) {
            // Inisialisasi AdminViewModel di sini
            val adminViewModel: AdminViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return AdminViewModel(adminRepository, prefManager) as T
                    }
                }
            )
            AdminDashboard(
                viewModel = adminViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.SpdDashboard.route) {
            SpdDashboard(
                onScanClick = { tingkat ->
                    navController.navigate("qr_scanner/$tingkat")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
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

        composable("qr_scanner/{tingkat}") { backStackEntry ->
            val tingkat = backStackEntry.arguments?.getString("tingkat") ?: "1"
            val spdViewModel: SpdViewModel = viewModel()

            QrScannerScreen(
                tingkat = tingkat,
                viewModel = spdViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
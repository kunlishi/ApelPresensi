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
import com.example.apelpresensi.data.remote.RetrofitClient
import com.example.apelpresensi.data.repository.AdminRepository
import com.example.apelpresensi.data.repository.PresensiRepository
import com.example.apelpresensi.data.repository.MahasiswaRepository
import com.example.apelpresensi.ui.screens.auth.LoginScreen
import com.example.apelpresensi.ui.screens.mahasiswa.StudentDashboard
import com.example.apelpresensi.ui.screens.mahasiswa.StudentQrScreen
import com.example.apelpresensi.ui.screens.spd.SpdDashboard
import com.example.apelpresensi.ui.screens.spd.QrScannerScreen
import com.example.apelpresensi.ui.screens.admin.AdminDashboard
import com.example.apelpresensi.ui.screens.auth.RegisterScreen
import com.example.apelpresensi.ui.screens.mahasiswa.IzinScreen

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
    mahasiswaRepository: MahasiswaRepository,
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
                },
                onProfileClick = { /* Aksi profil admin */ }
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
                },
                onProfileClick = { /* Aksi profil petugas SPD */ }
            )
        }

        // Di dalam NavGraph.kt pada bagian composable StudentDashboard
        composable(Screen.StudentDashboard.route) {
            val mhsViewModel: MahasiswaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MahasiswaViewModel(mahasiswaRepository, prefManager) as T
                    }
                }
            )

            StudentDashboard(
                viewModel = mhsViewModel,
                onShowQrClick = {
                    val nim = (mhsViewModel.profileState.value as? MahasiswaState.Success)?.data?.nim ?: ""
                    navController.navigate("qr_screen/$nim")
                },
                onIzinClick = { navController.navigate(Screen.Izin.route) }, // Hubungkan ke Izin
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                },
                onProfileClick = { /* Info Akun */ }
            )
        }

        composable(Screen.Izin.route) {
            // Gunakan mahasiswaRepository dari parameter NavGraph, jangan inisialisasi ulang
            val mhsViewModel: MahasiswaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MahasiswaViewModel(mahasiswaRepository, prefManager) as T
                    }
                }
            )

            IzinScreen(
                viewModel = mhsViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
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

            // Pastikan menggunakan Factory agar ViewModel tidak null
            val spdViewModel: SpdViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return SpdViewModel(presensiRepository, prefManager) as T
                    }
                }
            )

            QrScannerScreen(
                tingkat = tingkat,
                viewModel = spdViewModel,
                onBack = { navController.popBackStack() }
            )
        }

    }
}
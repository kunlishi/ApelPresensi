package com.example.apelpresensi.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.repository.AdminRepository
import com.example.apelpresensi.data.repository.MahasiswaRepository
import com.example.apelpresensi.data.repository.PresensiRepository
import com.example.apelpresensi.ui.screens.admin.AdminDashboard
import com.example.apelpresensi.ui.screens.admin.IzinValidationScreen
import com.example.apelpresensi.ui.screens.admin.RekapPresensiScreen
import com.example.apelpresensi.ui.screens.auth.LoginScreen
import com.example.apelpresensi.ui.screens.auth.RegisterScreen
import com.example.apelpresensi.ui.screens.mahasiswa.IzinScreen
import com.example.apelpresensi.ui.screens.mahasiswa.RiwayatIzinScreen
import com.example.apelpresensi.ui.screens.mahasiswa.RiwayatPresensiScreen
import com.example.apelpresensi.ui.screens.mahasiswa.StudentDashboard
import com.example.apelpresensi.ui.screens.mahasiswa.StudentQrScreen
import com.example.apelpresensi.ui.screens.spd.QrScannerScreen
import com.example.apelpresensi.ui.screens.spd.SpdDashboard
import com.example.apelpresensi.ui.viewmodel.AdminViewModel
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaState
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import com.example.apelpresensi.ui.viewmodel.SpdViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    adminRepository: AdminRepository,
    presensiRepository: PresensiRepository,
    mahasiswaRepository: MahasiswaRepository,
    prefManager: PreferenceManager
) {
    // 1. Inisialisasi Shared ViewModels di level NavGraph agar instance tetap terjaga di semua layar
    val adminViewModel: AdminViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AdminViewModel(adminRepository, prefManager) as T
            }
        }
    )

    val mhsViewModel: MahasiswaViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MahasiswaViewModel(mahasiswaRepository, prefManager) as T
            }
        }
    )

    val spdViewModel: SpdViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SpdViewModel(presensiRepository, prefManager) as T
            }
        }
    )

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        // --- AUTHENTICATION ---
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    // Navigasi berdasarkan role
                    val target = when (role) {
                        "ADMIN" -> Screen.AdminDashboard.route
                        "SPD" -> Screen.SpdDashboard.route
                        else -> Screen.StudentDashboard.route
                    }
                    navController.navigate(target) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.StudentDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // --- ROLE: ADMIN ---
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                viewModel = adminViewModel,
                authViewModel = authViewModel,
                onRekapClick = { id -> navController.navigate("rekap_presensi/$id") },
                onIzinClick = { navController.navigate(Screen.IzinValidation.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(
            route = Screen.RekapPresensi.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: 0L
            RekapPresensiScreen(
                scheduleId = id,
                viewModel = adminViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }
        composable(Screen.IzinValidation.route) {
            IzinValidationScreen(
                viewModel = adminViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        // --- ROLE: SPD ---
        composable(Screen.SpdDashboard.route) {
            SpdDashboard(
                authViewModel = authViewModel,
                onScanClick = { tingkat -> navController.navigate("qr_scanner/$tingkat") },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable("qr_scanner/{scheduleId}") { backStackEntry ->
            val scheduleId = backStackEntry.arguments?.getString("scheduleId")?.toLong() ?: 0L
            QrScannerScreen(
                viewModel = spdViewModel,
                scheduleId = scheduleId,
                onBack = { navController.popBackStack() }
            )
        }

        // --- ROLE: MAHASISWA ---
        composable(Screen.StudentDashboard.route) {
            StudentDashboard(
                viewModel = mhsViewModel,
                authViewModel = authViewModel,
                onShowQrClick = {
                    val nim = (mhsViewModel.profileState.value as? MahasiswaState.Success)?.data?.nim ?: ""
                    navController.navigate("qr_screen/$nim") },
                onIzinClick = { navController.navigate(Screen.Izin.route) },
                onRiwayatClick = { navController.navigate(Screen.RiwayatPresensi.route) },
                onRiwayatIzinClick = { navController.navigate(Screen.RiwayatIzin.route) }, // Tambahkan ini
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.Izin.route) {
            IzinScreen(
                viewModel = mhsViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.RiwayatPresensi.route) {
            RiwayatPresensiScreen(
                viewModel = mhsViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.RiwayatIzin.route) {
            RiwayatIzinScreen(
                viewModel = mhsViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(
            route = "qr_screen/{nim}",
            arguments = listOf(navArgument("nim") { type = NavType.StringType })
        ) { entry ->
            val nim = entry.arguments?.getString("nim") ?: ""
            StudentQrScreen(
                nim = nim,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
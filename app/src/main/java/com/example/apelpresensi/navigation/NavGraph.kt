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
import com.example.apelpresensi.ui.screens.auth.ChangePasswordScreen
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
                    authViewModel.resetState()
                    navController.popBackStack()
                },
                onBackToLogin = {navController.popBackStack()}
            )
        }

        composable(Screen.ChangePassword.route) {
            ChangePasswordScreen(
                viewModel = authViewModel,
                onSuccess = {
                    navController.popBackStack() // Kembali setelah sukses
                },
                onBack = { navController.popBackStack() }
            )
        }

        // --- ROLE: ADMIN ---
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                viewModel = adminViewModel,
                authViewModel = authViewModel,
                onRekapClick = { id -> navController.navigate("rekap_presensi/$id") },
                onIzinClick = { navController.navigate(Screen.IzinValidation.route) },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
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
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
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
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        // --- ROLE: SPD ---
        composable(Screen.SpdDashboard.route) {
            SpdDashboard(
                viewModel = spdViewModel,
                authViewModel = authViewModel,
                onScheduleSelected = { id ->
                    // Navigasi ke scanner dengan membawa ID Jadwal (Long)
                    navController.navigate("qr_scanner/$id")
                },
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.SpdDashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "qr_scanner/{scheduleId}",
            arguments = listOf(
                navArgument("scheduleId") { type = NavType.LongType } // Menjamin tipe data Long
            )
        ) { backStackEntry ->
            // Mengambil ID dari arguments secara langsung sebagai Long
            val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L

            QrScannerScreen(
                viewModel = spdViewModel,
                scheduleId = scheduleId,
                onBack = {
                    // Reset state scanner sebelum kembali agar tidak nyangkut di state Success/Error
                    spdViewModel.resetScanner()
                    navController.popBackStack()
                }
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
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
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
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
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
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
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
                onNavigateToChangePassword = { navController.navigate(Screen.ChangePassword.route) },
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
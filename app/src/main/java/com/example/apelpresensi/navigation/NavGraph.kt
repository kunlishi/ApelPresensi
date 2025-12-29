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
import com.example.apelpresensi.data.repository.*
import com.example.apelpresensi.ui.screens.auth.*
import com.example.apelpresensi.ui.screens.mahasiswa.*
import com.example.apelpresensi.ui.screens.spd.*
import com.example.apelpresensi.ui.screens.admin.*
import com.example.apelpresensi.ui.viewmodel.*

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    adminRepository: AdminRepository,
    presensiRepository: PresensiRepository,
    mahasiswaRepository: MahasiswaRepository,
    prefManager: PreferenceManager
) {
    // 1. Deklarasi ViewModel di level atas NavGraph agar shared/dikenali semua rute
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
        // --- AUTH ---
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    when (role) {
                        "ADMIN" -> navController.navigate(Screen.AdminDashboard.route) { popUpTo(0) }
                        "SPD" -> navController.navigate(Screen.SpdDashboard.route) { popUpTo(0) }
                        "MAHASISWA" -> navController.navigate(Screen.StudentDashboard.route) { popUpTo(0) }
                    }
                },
                onRegisterClick = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = { navController.navigate(Screen.StudentDashboard.route) { popUpTo(0) } },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // --- ADMIN ---
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                adminViewModel = adminViewModel,
                authViewModel = authViewModel, // Untuk ProfileDialog
                onRekapClick = { id -> navController.navigate("rekap_presensi/$id") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(
            route = Screen.RekapPresensi.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("id") ?: 0L
            RekapPresensiScreen(
                scheduleId = id,
                adminViewModel = adminViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }, // Aktifkan tombol back
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        // --- SPD ---
        composable(Screen.SpdDashboard.route) {
            SpdDashboard(
                authViewModel = authViewModel,
                onScanClick = { tingkat -> navController.navigate("qr_scanner/$tingkat") },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable("qr_scanner/{tingkat}") { backStackEntry ->
            val tingkat = backStackEntry.arguments?.getString("tingkat") ?: "1"
            QrScannerScreen(
                tingkat = tingkat,
                viewModel = spdViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- MAHASISWA ---
        composable(Screen.StudentDashboard.route) {
            StudentDashboard(
                viewModel = mhsViewModel,
                authViewModel = authViewModel,
                onShowQrClick = {
                    val nim = (mhsViewModel.profileState.value as? MahasiswaState.Success)?.data?.nim ?: ""
                    navController.navigate("qr_screen/$nim")
                },
                onIzinClick = { navController.navigate(Screen.Izin.route) },
                onRiwayatClick = { navController.navigate(Screen.RiwayatPresensi.route) },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.Izin.route) {
            IzinScreen(
                viewModel = mhsViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable(Screen.RiwayatPresensi.route) {
            RiwayatPresensiScreen(
                viewModel = mhsViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(Screen.Login.route) { popUpTo(0) }
                }
            )
        }

        composable("qr_screen/{nim}") { backStackEntry ->
            val nim = backStackEntry.arguments?.getString("nim") ?: ""
            StudentQrScreen(nim = nim, onBackClick = { navController.popBackStack() })
        }
    }
}
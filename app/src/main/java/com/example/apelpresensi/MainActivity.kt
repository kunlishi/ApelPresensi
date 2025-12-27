package com.example.apelpresensi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.RetrofitClient
import com.example.apelpresensi.data.repository.AdminRepository
import com.example.apelpresensi.data.repository.AuthRepository
import com.example.apelpresensi.data.repository.PresensiRepository
import com.example.apelpresensi.navigation.NavGraph
import com.example.apelpresensi.ui.theme.ApelPresensiTheme
import com.example.apelpresensi.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inisialisasi Data Layer & Repositories
        // Context 'this' aman digunakan di sini untuk SharedPreferences
        val prefManager = PreferenceManager(this)
        val apiService = RetrofitClient.apiService

        val authRepository = AuthRepository(apiService)
        val adminRepository = AdminRepository(apiService)
        val presensiRepository = PresensiRepository(apiService)

        // 2. Inisialisasi AuthViewModel
        val authViewModel = AuthViewModel(authRepository, prefManager)

        setContent {
            ApelPresensiTheme {
                // Surface menyediakan background dasar sesuai tema (Light/Dark Mode)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Memasukkan semua dependensi ke NavGraph
                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        adminRepository = adminRepository,
                        presensiRepository = presensiRepository,
                        prefManager = prefManager
                    )
                }
            }
        }
    }
}
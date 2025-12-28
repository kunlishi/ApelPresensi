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
import com.example.apelpresensi.data.repository.MahasiswaRepository // Pastikan import ini ada

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefManager = PreferenceManager(this)
        val apiService = RetrofitClient.apiService

        val authRepository = AuthRepository(apiService)
        val adminRepository = AdminRepository(apiService)
        val presensiRepository = PresensiRepository(apiService)
        val mahasiswaRepository = MahasiswaRepository(apiService) // Tambahkan baris ini

        val authViewModel = AuthViewModel(authRepository, prefManager)

        setContent {
            ApelPresensiTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        adminRepository = adminRepository,
                        presensiRepository = presensiRepository,
                        mahasiswaRepository = mahasiswaRepository, // Masukkan ke sini
                        prefManager = prefManager
                    )
                }
            }
        }
    }
}
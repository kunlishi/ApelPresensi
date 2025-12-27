package com.example.apelpresensi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.apelpresensi.ui.theme.ApelPresensiTheme
import androidx.navigation.compose.rememberNavController
import com.example.apelpresensi.data.local.PreferenceManager
import com.example.apelpresensi.data.remote.RetrofitClient
import com.example.apelpresensi.data.repository.AuthRepository
import com.example.apelpresensi.navigation.NavGraph
import com.example.apelpresensi.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi Manual (Tanpa Dependency Injection untuk sementara)
        val prefManager = PreferenceManager(this)
        val authRepository = AuthRepository(RetrofitClient.apiService)
        val authViewModel = AuthViewModel(authRepository, prefManager)

        setContent {
            ApelPresensiTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }
        }
    }
}

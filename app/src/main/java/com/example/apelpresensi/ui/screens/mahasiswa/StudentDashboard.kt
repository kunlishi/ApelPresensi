package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.viewmodel.MahasiswaState
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel

@Composable
fun StudentDashboard(
    viewModel: MahasiswaViewModel,
    onShowQrClick: () -> Unit,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit // Tambahkan callback untuk profil
) {
    val state by viewModel.profileState

    // Ambil data profil saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Dashboard Mahasiswa",
                onLogoutClick = onLogout,
                onProfileClick = onProfileClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is MahasiswaState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MahasiswaState.Success -> {
                    val mhs = (state as MahasiswaState.Success).data

                    // Card Profil menggunakan desain baru
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = mhs.nama,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary // Maroon
                            )
                            Text(text = "NIM: ${mhs.nim}")
                            Text(text = "Kelas: ${mhs.kelas}")
                            Text(text = "Tingkat: ${mhs.tingkat}")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Tombol Utama (Gold)
                    Button(
                        onClick = onShowQrClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("TAMPILKAN QR PRESENSI")
                    }
                }
                is MahasiswaState.Error -> {
                    Text(
                        text = (state as MahasiswaState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Button(onClick = { viewModel.fetchProfile() }) {
                        Text("Coba Lagi")
                    }
                }
            }
        }
    }
}
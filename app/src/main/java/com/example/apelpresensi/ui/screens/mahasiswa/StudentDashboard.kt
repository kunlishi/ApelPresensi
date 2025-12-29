package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaState
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import kotlinx.coroutines.launch

@Composable
fun StudentDashboard(
    viewModel: MahasiswaViewModel,
    authViewModel: AuthViewModel, // Tambahkan ini
    onShowQrClick: () -> Unit,
    onIzinClick: () -> Unit,
    onRiwayatClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.profileState
    val scope = rememberCoroutineScope()
    var showProfile by remember { mutableStateOf(false) }

    // Ambil data profil saat layar dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
        authViewModel.fetchMe()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Dashboard Mahasiswa",
                onBackClick = null,
                onProfileClick = { showProfile = true }
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

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onRiwayatClick, // Tambahkan callback ini ke parameter StudentDashboard
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                    ) {
                        Text("LIHAT RIWAYAT PRESENSI")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onIzinClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("PENGAJUAN IZIN / SAKIT")
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
        if (showProfile) {
            ProfileDialog(
                user = authViewModel.currentUser,
                {scope.launch {
                    showProfile = false
                    kotlinx.coroutines.delay(150)
                    authViewModel.logout()
                    onNavigateToLogin()
                }
                },
                onDismiss = { showProfile = false }
            )
        }
    }
}
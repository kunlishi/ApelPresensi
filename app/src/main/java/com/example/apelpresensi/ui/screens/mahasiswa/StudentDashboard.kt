package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.viewmodel.MahasiswaState
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDashboard(
    viewModel: MahasiswaViewModel,
    onShowQrClick: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.profileState

    // Ambil data profil saat layar pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard Mahasiswa") })
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (state) {
                is MahasiswaState.Loading -> CircularProgressIndicator()
                is MahasiswaState.Success -> {
                    val mhs = (state as MahasiswaState.Success).data
                    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Nama: ${mhs.nama}", style = MaterialTheme.typography.titleMedium)
                            Text("NIM: ${mhs.nim}")
                            Text("Kelas: ${mhs.kelas}")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onShowQrClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tampilkan QR Code Presensi")
                    }
                }
                is MahasiswaState.Error -> Text("Error: ${(state as MahasiswaState.Error).message}")
            }

            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onLogout) { Text("Logout", color = Color.Red) }
        }
    }
}
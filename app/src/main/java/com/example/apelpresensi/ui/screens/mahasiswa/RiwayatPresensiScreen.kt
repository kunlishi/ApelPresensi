package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import kotlinx.coroutines.launch

@Composable
fun RiwayatPresensiScreen(
    viewModel: MahasiswaViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var showProfile by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    // Memicu pengambilan data riwayat saat layar pertama kali dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchRiwayat()
        authViewModel.fetchMe()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Riwayat Presensi",
                onBackClick = onBack,
                onProfileClick = { showProfile = true }
            )
        }
    ) { padding ->
        val riwayat = viewModel.riwayatList.value

        if (riwayat.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Belum ada riwayat presensi.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(riwayat) { item ->
                    RiwayatCard(item = item)
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
                onChangePassword = onNavigateToChangePassword,
                onDismiss = { showProfile = false }
            )
        }
    }
}

@Composable
fun RiwayatCard(item: com.example.apelpresensi.data.remote.dto.PresensiResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.tanggal, // Format: YYYY-MM-DD dari DTO
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Waktu: ${formatJam(item.waktuPresensi)}", // Menggunakan helper format jam
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Tingkat: ${item.tingkat}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            // Status Badge
            StatusBadge(status = item.status)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val backgroundColor = when (status.uppercase()) {
        "HADIR" -> Color(0xFF4CAF50) // Hijau
        "TERLAMBAT" -> Color(0xFFF44336) // Merah
        "IZIN" -> Color(0xFF2196F3) // Biru
        else -> Color.Gray
    }

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Helper untuk mengambil jam saja (HH:mm) dari string waktuPresensi.
 * Menangani format ISO (2025-12-28T07:00:00) atau format waktu saja (07:00:00).
 */
fun formatJam(waktuRaw: String): String {
    return try {
        if (waktuRaw.contains("T")) {
            // Format ISO: ambil bagian setelah 'T' lalu ambil 5 karakter pertama (HH:mm)
            waktuRaw.split("T")[1].substring(0, 5)
        } else if (waktuRaw.contains(" ")) {
            // Format "YYYY-MM-DD HH:mm:ss"
            waktuRaw.split(" ")[1].substring(0, 5)
        } else {
            // Format "HH:mm:ss" langsung
            waktuRaw.substring(0, 5)
        }
    } catch (e: Exception) {
        waktuRaw // Kembalikan string asli jika gagal parsing
    }
}
package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel

@Composable
fun RiwayatPresensiScreen(
    viewModel: MahasiswaViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.fetchRiwayat()
    }
    fun formatJam(waktuRaw: String): String {
        return try {
            // Jika formatnya "2025-12-28 07:30:00" atau "2025-12-28T07:30:00"
            if (waktuRaw.contains(" ") || waktuRaw.contains("T")) {
                val pemisah = if (waktuRaw.contains("T")) "T" else " "
                waktuRaw.split(pemisah)[1].substring(0, 5) // Ambil HH:mm
            } else {
                // Jika sudah format "07:30:00", ambil 5 karakter depan
                waktuRaw.substring(0, 5)
            }
        } catch (e: Exception) {
            waktuRaw // Kembalikan apa adanya jika gagal parsing
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Riwayat Presensi",
                onLogoutClick = onLogout,
                onProfileClick = onBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(viewModel.riwayatList.value) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = item.tanggal, fontWeight = FontWeight.Bold)
                            Text(text = "Waktu: ${formatJam(item.waktuPresensi)}",
                                style = MaterialTheme.typography.bodySmall)
                        }

                        // Badge Status
                        val statusColor = when (item.status) {
                            "HADIR" -> Color(0xFF2E7D32)
                            "TERLAMBAT" -> Color(0xFFE65100)
                            "IZIN" -> Color(0xFF1976D2)
                            else -> Color.Gray
                        }

                        Surface(
                            color = statusColor,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = item.status,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
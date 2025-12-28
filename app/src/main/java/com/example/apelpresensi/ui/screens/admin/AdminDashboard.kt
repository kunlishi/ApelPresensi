package com.example.apelpresensi.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.viewmodel.AdminViewModel

@Composable
fun AdminDashboard(
    viewModel: AdminViewModel,
    onLogout: () -> Unit,
    onProfileClick: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchJadwal() // Muat data jadwal dari repository
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Panel Admin",
                onLogoutClick = onLogout,
                onProfileClick = onProfileClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(viewModel.jadwalList) { jadwal ->
                ListItem(
                    headlineContent = { Text("Tingkat ${jadwal.tingkat} - ${jadwal.tanggalApel}") },
                    supportingContent = { Text("Jam: ${jadwal.waktuApel} | ${jadwal.keterangan ?: ""}") },
                    trailingContent = {
                        // Tombol Hapus Jadwal
                        IconButton(onClick = { viewModel.deleteJadwal(jadwal.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                HorizontalDivider()
            }
        }

        if (showDialog) {
            AddJadwalDialog(
                onDismiss = { showDialog = false },
                onConfirm = { tgl, jam, tkt, ket ->
                    viewModel.addJadwal(tgl, jam, tkt, ket) // Simpan via ViewModel
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun AddJadwalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var tanggal by remember { mutableStateOf("") }
    var waktu by remember { mutableStateOf("") }
    var tingkat by remember { mutableStateOf("1") }
    var keterangan by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Jadwal Apel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tanggal,
                    onValueChange = { tanggal = it },
                    label = { Text("Tanggal (YYYY-MM-DD)") },
                    placeholder = { Text("2025-12-30") }
                )
                OutlinedTextField(
                    value = waktu,
                    onValueChange = { waktu = it },
                    label = { Text("Waktu (HH:mm:ss)") },
                    placeholder = { Text("07:00:00") }
                )
                OutlinedTextField(
                    value = tingkat,
                    onValueChange = { tingkat = it },
                    label = { Text("Tingkat (1/2/3/4)") }
                )
                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (Opsional)") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tanggal, waktu, tingkat, keterangan) }) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
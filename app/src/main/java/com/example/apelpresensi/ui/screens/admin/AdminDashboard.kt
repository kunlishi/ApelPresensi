@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.apelpresensi.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.viewmodel.AdminViewModel
@Composable
fun AdminDashboard(viewModel: AdminViewModel, onLogout: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.fetchJadwal() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Panel Admin - Jadwal") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jadwal")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(viewModel.jadwalList) { jadwal ->
                ListItem(
                    headlineContent = { Text("Tingkat ${jadwal.tingkat} - ${jadwal.tanggalApel}") },
                    supportingContent = { Text("Jam: ${jadwal.waktuApel} | ${jadwal.keterangan ?: ""}") }
                )
                HorizontalDivider()
            }
        }

        // Dialog sederhana untuk input jadwal bisa ditambahkan di sini
        if (showDialog) {
            // Logika Dialog Input (Tanggal, Waktu, Tingkat)
        }
    }
}
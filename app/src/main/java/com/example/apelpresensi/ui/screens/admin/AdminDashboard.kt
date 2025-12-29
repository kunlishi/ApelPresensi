@file:OptIn(ExperimentalMaterial3Api::class)


package com.example.apelpresensi.ui.screens.admin

import androidx.compose.foundation.clickable
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
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboard(
    adminViewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onRekapClick: (Long) -> Unit,
    onProfileClick: () -> Unit
) {
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        adminViewModel.fetchJadwal()
        authViewModel.fetchMe() // Ambil data profil saat dashboard dibuka
    }

    LaunchedEffect(Unit) {
        adminViewModel.fetchJadwal() // Muat data jadwal dari repository
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Panel Admin",
                onBackClick = null,
                onProfileClick = onProfileClick
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showProfileDialog = true },
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
            items(adminViewModel.jadwalList) { jadwal ->
                ListItem(
                    modifier = Modifier.clickable { onRekapClick(jadwal.id) }, // Tambahkan onRekapClick di parameter AdminDashboard
                    headlineContent = { Text("Tingkat ${jadwal.tingkat} - ${jadwal.tanggalApel}") },
                    supportingContent = { Text("Jam: ${jadwal.waktuApel} | ${jadwal.keterangan ?: ""}") },
                    trailingContent = {
                        // Tombol Hapus Jadwal
                        IconButton(onClick = { adminViewModel.deleteJadwal(jadwal.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
                )
                HorizontalDivider()
            }
        }

        if (showProfileDialog) {
            AddJadwalDialog(
                onDismiss = { showProfileDialog = false },
                onConfirm = { tgl, jam, tkt, ket ->
                    adminViewModel.addJadwal(tgl, jam, tkt, ket) // Simpan via ViewModel
                    showProfileDialog = false
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
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var tingkat by remember { mutableStateOf("1") }
    var keterangan by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Jadwal Apel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Tombol Pilih Tanggal
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedDate.isEmpty()) "Pilih Tanggal" else "Tanggal: $selectedDate")
                }

                // Tombol Pilih Waktu
                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedTime.isEmpty()) "Pilih Waktu" else "Waktu: $selectedTime")
                }

                // Pilihan Tingkat
                Text("Tingkat:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1", "2", "3", "4").forEach { t ->
                        FilterChip(
                            selected = tingkat == t,
                            onClick = { tingkat = t },
                            label = { Text(t) }
                        )
                    }
                }

                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (Opsional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = selectedDate.isNotEmpty() && selectedTime.isNotEmpty(),
                onClick = { onConfirm(selectedDate, selectedTime, tingkat, keterangan) }
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } }
    )

    // Material 3 DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        selectedDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // Material 3 TimePicker Dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedTime =
                        String.format("%02d:%02d:00", timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}
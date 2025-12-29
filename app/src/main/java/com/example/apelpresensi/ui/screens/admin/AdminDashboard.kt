@file:OptIn(ExperimentalMaterial3Api::class)


package com.example.apelpresensi.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.alpha
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AdminViewModel
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboard(
    viewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onRekapClick: (Long) -> Unit,
    onIzinClick: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // 1. Tambahkan state baru khusus untuk dialog tambah jadwal
    var showProfileDialog by remember { mutableStateOf(false) }
    var showAddJadwalDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var scheduleIdToDelete by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val pendingCount = viewModel.izinList.count{ it.status == "PENDING"}
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkTransition")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f, // Tingkat transparansi terendah
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BlinkAlpha"
    )

    LaunchedEffect(Unit) {
        viewModel.fetchJadwal()
        viewModel.fetchIzin()
        authViewModel.fetchMe()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Panel Admin",
                onProfileClick = { showProfileDialog = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddJadwalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        },
        // Menerapkan Kotak Izin di BottomBar agar tidak bertabrakan dengan FAB
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
                        .clickable { onIzinClick() },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Validasi Izin Mahasiswa", fontWeight = FontWeight.Bold)
                            Text(
                                text = if (pendingCount > 0) "Ada $pendingCount pengajuan baru" else "Tidak ada pengajuan baru",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        // 2. Terapkan Animasi pada Badge
                        if (pendingCount > 0) {
                            Surface(
                                color = Color.Red,
                                shape = androidx.compose.foundation.shape.CircleShape,
                                // Terapkan alphaAnim di sini agar berkedip
                                modifier = Modifier.alpha(alphaAnim)
                            ) {
                                Text(
                                    text = pendingCount.toString(),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                text = "Daftar Jadwal Apel",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(viewModel.jadwalList) { jadwal ->
                    ListItem(
                        modifier = Modifier.clickable { onRekapClick(jadwal.id) },
                        headlineContent = { Text("Tingkat ${jadwal.tingkat} - ${jadwal.tanggalApel}") },
                        supportingContent = { Text("Jam: ${jadwal.waktuApel}") },
                        trailingContent = {
                            IconButton(onClick = { viewModel.deleteJadwal(jadwal.id) }) {
                                Icon(Icons.Default.Delete, "Hapus", tint = Color.Red)
                            }
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
        if (showDeleteConfirmation) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmation = false },
                title = { Text("Konfirmasi Hapus") },
                text = {
                    Text("Apakah Anda yakin ingin menghapus jadwal ini? " +
                            "PERINGATAN: Semua data presensi mahasiswa pada jadwal ini akan ikut terhapus selamanya.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scheduleIdToDelete?.let { viewModel.deleteJadwal(it) }
                            showDeleteConfirmation = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) { Text("Hapus") }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmation = false }) { Text("Batal") }
                }
            )
        }

        // Dialog Profil
        if (showProfileDialog) {
            ProfileDialog(
                user = authViewModel.currentUser,
                onLogout = {scope.launch {
                    showProfileDialog = false // 1. Hilangkan jendela profil
                    kotlinx.coroutines.delay(150) // 2. Tunggu sebentar agar animasi dismissal terlihat
                    authViewModel.logout() // 3. Hapus sesi
                    onNavigateToLogin() // 4. Navigasi balik ke Login
                }
                },
                onDismiss = { showProfileDialog = false }
            )
        }

        if (showAddJadwalDialog) {
            AddJadwalDialog(
                onDismiss = { showAddJadwalDialog = false },
                onConfirm = { tanggal, waktu, tingkat, ket ->
                    viewModel.addJadwal(tanggal, waktu, tingkat, ket)
                    showAddJadwalDialog = false
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
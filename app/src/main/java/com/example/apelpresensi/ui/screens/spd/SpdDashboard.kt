package com.example.apelpresensi.ui.screens.spd

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Schedule
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
import com.example.apelpresensi.ui.viewmodel.ScannerState
import com.example.apelpresensi.ui.viewmodel.SpdViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpdDashboard(
    viewModel: SpdViewModel,
    authViewModel: AuthViewModel,
    onScheduleSelected: (Long) -> Unit, // Berubah dari String ke Long (ID Jadwal)
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showProfile by remember { mutableStateOf(false) }

    // Ambil state dari ViewModel
    val schedules by viewModel.availableSchedules
    val scannerState by viewModel.scannerState

    // Ambil jadwal setiap kali layar dibuka
    LaunchedEffect(Unit) {
        viewModel.fetchTodaySchedules()
        authViewModel.fetchMe()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Petugas SPD",
                onBackClick = null,
                onProfileClick = { showProfile = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header Informasi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Selamat Datang, Petugas",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Pilih Jadwal Apel Hari Ini",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (scannerState is ScannerState.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Daftar Jadwal
            if (schedules.isEmpty() && scannerState !is ScannerState.Loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("Tidak ada jadwal apel untuk hari ini", color = Color.Gray)
                        Button(onClick = { viewModel.fetchTodaySchedules() }, Modifier.padding(top = 16.dp)) {
                            Text("Refresh")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(schedules) { schedule ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScheduleSelected(schedule.id) },
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tingkat ${schedule.tingkat}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Waktu: ${schedule.waktuApel}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    if (!schedule.keterangan.isNullOrBlank()) {
                                        Text(
                                            text = schedule.keterangan,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showProfile) {
            ProfileDialog(
                user = authViewModel.currentUser,
                onLogout = {
                    scope.launch {
                        showProfile = false
                        delay(150)
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
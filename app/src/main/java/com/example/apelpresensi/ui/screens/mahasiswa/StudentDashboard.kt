package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaState
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudentDashboard(
    viewModel: MahasiswaViewModel,
    authViewModel: AuthViewModel,
    onShowQrClick: () -> Unit,
    onIzinClick: () -> Unit,
    onRiwayatClick: () -> Unit,
    onRiwayatIzinClick: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val state by viewModel.profileState
    val scope = rememberCoroutineScope()
    var showProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchProfile()
        authViewModel.fetchMe()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Beranda Mahasiswa",
                onBackClick = null,
                onProfileClick = { showProfile = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (state) {
                is MahasiswaState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is MahasiswaState.Success -> {
                    val mhs = (state as MahasiswaState.Success).data

                    // 1. Welcome Section & Profile Card
                    Text(
                        text = "Selamat Datang,",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Text(
                        text = mhs.nama,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary // Marun
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Badge,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = "NIM: ${mhs.nim}", fontWeight = FontWeight.SemiBold)
                                Text(text = "Kelas: ${mhs.kelas} | Tingkat: ${mhs.tingkat}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "Menu Utama", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. Grid Menu Dashboard
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            MenuCard(
                                title = "QR Presensi",
                                subtitle = "Scan untuk Apel",
                                icon = Icons.Default.QrCodeScanner,
                                color = Color(0xFFFFC107), // Gold
                                onClick = onShowQrClick
                            )
                        }
                        item {
                            MenuCard(
                                title = "Riwayat",
                                subtitle = "Kehadiran Apel",
                                icon = Icons.Default.History,
                                color = MaterialTheme.colorScheme.tertiary,
                                onClick = onRiwayatClick
                            )
                        }
                        item {
                            MenuCard(
                                title = "Ajukan Izin",
                                subtitle = "Sakit / Berhalangan",
                                icon = Icons.Default.FileUpload,
                                color = MaterialTheme.colorScheme.secondary,
                                onClick = onIzinClick
                            )
                        }
                        item {
                            MenuCard(
                                title = "Status Izin",
                                subtitle = "Cek Hasil Validasi",
                                icon = Icons.Default.AssignmentTurnedIn,
                                color = Color(0xFF4CAF50), // Hijau
                                onClick = onRiwayatIzinClick
                            )
                        }
                    }
                }
                is MahasiswaState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = (state as MahasiswaState.Error).message, color = Color.Red)
                        Button(onClick = { viewModel.fetchProfile() }) { Text("Coba Lagi") }
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
                        // PENTING: Bersihkan form di MahasiswaViewModel agar tidak terbaca akun lain
                        viewModel.resetForm()
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
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(10.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
package com.example.apelpresensi.ui.screens.mahasiswa

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import com.example.apelpresensi.ui.viewmodel.UploadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IzinScreen(
    viewModel: MahasiswaViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showProfile by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Memantau status upload dari ViewModel
    val uploadStatus = viewModel.uploadStatus.value

    // Launcher untuk memilih file (Gambar/PDF)
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Simpan URI ke ViewModel agar tidak hilang saat aplikasi masuk background
        viewModel.formSelectedUri = uri
    }

    // Navigasi otomatis jika berhasil
    LaunchedEffect(uploadStatus) {
        if (uploadStatus is UploadState.Success) {
            delay(2000)
            viewModel.resetForm() // Bersihkan form di ViewModel
            onBack()
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Pengajuan Izin",
                onBackClick = onBack,
                onProfileClick = { showProfile = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Formulir Izin / Sakit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Isi detail alasan dan lampirkan bukti file.", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // 1. Pemilihan JENIS (Dulu bernama Alasan)
            Text("Jenis Pengajuan:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = viewModel.formJenisIzin == "IZIN",
                    onClick = { viewModel.formJenisIzin = "IZIN" },
                    label = { Text("Izin") }
                )
                FilterChip(
                    selected = viewModel.formJenisIzin == "SAKIT",
                    onClick = { viewModel.formJenisIzin = "SAKIT" },
                    label = { Text("Sakit") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Input KETERANGAN (Deskripsi detail)
            OutlinedTextField(
                value = viewModel.formKeterangan, // Variabel alasan di VM digunakan sebagai Keterangan
                onValueChange = { viewModel.formKeterangan = it },
                label = { Text("Keterangan Tambahan") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                placeholder = { Text("Contoh: Mengikuti lomba matematika / Sedang demam") },
                enabled = uploadStatus !is UploadState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Pratinjau Bukti (Gambar/PDF)
            Text(text = "Pratinjau Bukti:", modifier = Modifier.align(Alignment.Start), style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.formSelectedUri != null) {
                    val isPdf = context.contentResolver.getType(viewModel.formSelectedUri!!) == "application/pdf"
                    if (isPdf) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Description, null, Modifier.size(64.dp), MaterialTheme.colorScheme.primary)
                            Text("File PDF Terpilih")
                        }
                    } else {
                        AsyncImage(
                            model = viewModel.formSelectedUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Belum ada file dipilih", color = Color.Gray)
                        TextButton(onClick = { launcher.launch("*/*") }) { Text("PILIH GAMBAR/PDF") }
                    }
                }
            }

            if (viewModel.formSelectedUri != null && uploadStatus !is UploadState.Loading) {
                TextButton(onClick = { launcher.launch("*/*") }) { Text("Ganti File") }
            }

            Spacer(modifier = Modifier.height(32.dp))

            when (val status = uploadStatus) {
                is UploadState.Loading -> {
                    // Tampilkan loading dan pesan
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Sedang mengirim bukti, mohon tunggu...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                is UploadState.Success -> {
                    // Tampilkan sukses
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                        Text(status.message, color = Color(0xFF2E7D32), modifier = Modifier.padding(16.dp), fontWeight = FontWeight.Bold)
                    }
                }
                is UploadState.Error -> {
                    Text(status.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { viewModel.resetForm() }) { Text("Coba Lagi") }
                }
                else -> {
                    Button(
                        onClick = {
                            viewModel.formSelectedUri?.let { uri ->
                                viewModel.uploadIzin(
                                    context = context,
                                    uri = uri,
                                    jenis = viewModel.formJenisIzin,
                                    keterangan = viewModel.formKeterangan
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        // Validasi tombol agar tidak kosong
                        enabled = viewModel.formKeterangan.isNotBlank() && viewModel.formSelectedUri != null
                    ) {
                        Text("KIRIM PENGAJUAN", fontWeight = FontWeight.Bold)
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
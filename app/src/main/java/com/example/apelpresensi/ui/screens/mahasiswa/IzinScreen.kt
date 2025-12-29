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
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showProfile by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    // Memantau status upload dari ViewModel
    val uploadStatus = viewModel.uploadStatus

    // Launcher untuk memilih gambar dari galeri HP
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.selectedUri = uri
    }

    // Efek samping: Jika sukses, tunggu 2 detik lalu kembali ke Dashboard
    LaunchedEffect(viewModel.uploadStatus) {
        if (viewModel.uploadStatus is UploadState.Success) {
            delay(2000)
            viewModel.resetForm() // Reset data setelah sukses
            onBack()
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Pengajuan Izin",
                onBackClick = onBack,
                onProfileClick = { showProfile = true } // FIX: Berikan nilai true agar dialog muncul
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp).verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Formulir Izin / Sakit", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Isi alasan dan lampirkan bukti foto surat keterangan.", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Pemilihan Jenis Izin
            Text("Jenis Pengajuan:", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = viewModel.jenisIzin == "IZIN", onClick = { viewModel.jenisIzin = "IZIN" }, label = { Text("Izin") })
                FilterChip(selected = viewModel.jenisIzin == "SAKIT", onClick = { viewModel.jenisIzin = "SAKIT" }, label = { Text("Sakit") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Alasan (Keterangan)
            OutlinedTextField(
                value = viewModel.keterangan,
                onValueChange = { viewModel.keterangan = it },
                label = { Text("Alasan Ketidakhadiran") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = uploadStatus !is UploadState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Pratinjau Bukti (Gambar/PDF)
            Text(text = "Pratinjau Bukti:", modifier = Modifier.align(Alignment.Start), style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(250.dp).background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.selectedUri != null) {
                    val isPdf = context.contentResolver.getType(viewModel.selectedUri!!) == "application/pdf"
                    if (isPdf) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Description, null, Modifier.size(64.dp), MaterialTheme.colorScheme.primary)
                            Text("File PDF Terpilih")
                        }
                    } else {
                        AsyncImage(model = viewModel.selectedUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Belum ada file dipilih", color = Color.Gray)
                        TextButton(onClick = { launcher.launch("*/*") }) { Text("PILIH GAMBAR/PDF") }
                    }
                }
            }

            if (viewModel.selectedUri != null && uploadStatus !is UploadState.Loading) {
                TextButton(onClick = { launcher.launch("*/*") }) { Text("Ganti File") }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status Upload
            when (uploadStatus) {
                is UploadState.Loading -> CircularProgressIndicator()

                is UploadState.Success -> Text(uploadStatus.message, color = Color(0xFF2E7D32))
                is UploadState.Error -> Text(uploadStatus.message, color = MaterialTheme.colorScheme.error)
                else -> {
                    Button(
                        onClick = { viewModel.selectedUri?.let { viewModel.uploadIzin(context, it, viewModel.keterangan, viewModel.jenisIzin) } },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = viewModel.keterangan.isNotEmpty() && viewModel.selectedUri != null
                    ) { Text("KIRIM PENGAJUAN") }
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
                onDismiss = { showProfile = false }
            )
        }
    }
}
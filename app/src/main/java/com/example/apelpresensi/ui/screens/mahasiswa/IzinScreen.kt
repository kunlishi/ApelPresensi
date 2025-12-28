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
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import com.example.apelpresensi.ui.viewmodel.UploadState
import kotlinx.coroutines.delay

@Composable
fun IzinScreen(
    viewModel: MahasiswaViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var keterangan by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Memantau status upload dari ViewModel
    val uploadStatus = viewModel.uploadStatus

    // Launcher untuk memilih gambar dari galeri HP
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Efek samping: Jika sukses, tunggu 2 detik lalu kembali ke Dashboard
    LaunchedEffect(uploadStatus) {
        if (uploadStatus is UploadState.Success) {
            delay(2000)
            viewModel.resetUploadStatus() // Reset state agar tidak loop
            onBack()
        }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Pengajuan Izin",
                onLogoutClick = onLogout,
                onProfileClick = onBack
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
            // Header Judul
            Text(
                text = "Formulir Izin / Sakit",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Isi alasan dan lampirkan bukti foto surat keterangan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Input Keterangan
            OutlinedTextField(
                value = keterangan,
                onValueChange = { keterangan = it },
                label = { Text("Alasan Ketidakhadiran") },
                placeholder = { Text("Contoh: Sakit tipes, sedang dirawat") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                enabled = uploadStatus !is UploadState.Loading
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Label Preview
            Text(
                text = "Pratinjau Bukti Foto:",
                modifier = Modifier.align(Alignment.Start),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Kotak Preview Foto
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    // Cek apakah file adalah PDF (berdasarkan skema Uri atau ekstensi)
                    val isPdf = context.contentResolver.getType(selectedImageUri!!) == "application/pdf"

                    if (isPdf) {
                        // Tampilkan ikon PDF jika yang dipilih adalah file PDF
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text("File PDF Terpilih", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        // Tampilkan gambar jika file adalah gambar
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Preview Bukti",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Foto belum dipilih",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        TextButton(onClick = { launcher.launch("*/*") }) { // Gunakan */* agar bisa pilih semua
                            Text("PILIH FILE (GAMBAR/PDF)")
                        }
                    }
                }
            }

            // Tombol Ganti jika sudah ada foto
            if (selectedImageUri != null && uploadStatus !is UploadState.Loading) {
                TextButton(onClick = { launcher.launch("*/*") }) {
                    Text("Ganti File")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bagian Feedback Status (Loading/Success/Error)
            when (uploadStatus) {
                is UploadState.Loading -> {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Sedang mengirim data...", style = MaterialTheme.typography.bodySmall)
                }
                is UploadState.Success -> {
                    Text(
                        text = uploadStatus.message,
                        color = Color(0xFF2E7D32), // Hijau Sukses
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                is UploadState.Error -> {
                    Text(
                        text = uploadStatus.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {
                    // Tombol Kirim Utama
                    Button(
                        onClick = {
                            selectedImageUri?.let { uri ->
                                viewModel.uploadIzin(context, uri, keterangan)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = keterangan.isNotEmpty() && selectedImageUri != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "KIRIM PENGAJUAN",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
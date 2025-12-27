package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.util.QrGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQrScreen(
    nim: String,
    onBackClick: () -> Unit
) {
    // Generate QR secara lokal menggunakan NIM mahasiswa
    val qrBitmap = remember(nim) { QrGenerator.generateQrCode(nim) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Presensi Saya") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tunjukkan QR ini ke Petugas SPD",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Box untuk membungkus QR Code agar terlihat menonjol
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.size(300.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "QR Code NIM $nim",
                            modifier = Modifier.size(260.dp)
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "NIM: $nim",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
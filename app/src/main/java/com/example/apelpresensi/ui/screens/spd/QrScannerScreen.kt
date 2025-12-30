package com.example.apelpresensi.ui.screens.spd

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.apelpresensi.ui.viewmodel.ScannerState
import com.example.apelpresensi.ui.viewmodel.SpdViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@Composable
fun QrScannerScreen(
    scheduleId: Long, // Ganti tingkat menjadi scheduleId sesuai endpoint backend
    viewModel: SpdViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission) {
            // TERHUBUNG: Gunakan scheduleId
            QrScannerContent(scheduleId, viewModel, onBack)
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Izin kamera diperlukan untuk melakukan scan.")
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Berikan Izin")
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding()
                .align(Alignment.TopStart)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }
    }
}

@Composable
fun QrScannerContent(
    scheduleId: Long,
    viewModel: SpdViewModel,
    onBack: () -> Unit
) {
    val scannerState by viewModel.scannerState
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Memantau state untuk memicu Dialog Konfirmasi
    LaunchedEffect(scannerState) {
        if (scannerState is ScannerState.NeedConfirmation) {
            showConfirmDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(onQrScanned = { nim ->
            if (scannerState is ScannerState.Idle) {
                // SINKRONISASI: Panggil scan dengan scheduleId
                viewModel.scanQR(nim, scheduleId)
            }
        })

        // Overlay Hasil Scan
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp, start = 24.dp, end = 24.dp)
        ) {
            when (scannerState) {
                is ScannerState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))

                is ScannerState.Success -> {
                    val data = (scannerState as ScannerState.Success).data
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("✅ Berhasil: ${data.nama}", fontWeight = FontWeight.Bold)
                            Text("Status: ${data.status}", style = MaterialTheme.typography.bodySmall)
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.resetScanner() }, Modifier.fillMaxWidth()) {
                                Text("SCAN BERIKUTNYA")
                            }
                        }
                    }
                }

                is ScannerState.Error -> {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("❌ Gagal!", fontWeight = FontWeight.Bold)
                            Text((scannerState as ScannerState.Error).message)
                            Button(onClick = { viewModel.resetScanner() }, Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                Text("COBA LAGI")
                            }
                        }
                    }
                }
                else -> {}
            }
        }

        // DIALOG KONFIRMASI (±5 MENIT)
        if (showConfirmDialog && scannerState is ScannerState.NeedConfirmation) {
            val data = (scannerState as ScannerState.NeedConfirmation).data
            AlertDialog(
                onDismissRequest = {
                    showConfirmDialog = false
                    viewModel.resetScanner()
                },
                title = { Text("Konfirmasi SPD") },
                text = { Text("Mahasiswa ${data.nama} berada di rentang waktu transisi. Tentukan statusnya:") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.confirmManual(data.nim!!, scheduleId, "HADIR")
                            showConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Hadir") }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            viewModel.confirmManual(data.nim!!, scheduleId, "TERLAMBAT")
                            showConfirmDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Terlambat") }
                }
            )
        }
    }
}
@Composable
fun QrScannerContent(
    tingkat: String,
    viewModel: SpdViewModel,
    onBack: () -> Unit
) {
    val scannerState by viewModel.scannerState

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Lapisan Kamera
        CameraPreview(onQrScanned = { nim ->
            // Hanya kirim request jika state sedang Idle
            if (scannerState is ScannerState.Idle) {
                viewModel.processScannedNim(nim, tingkat)
            }
        })

        // 2. Lapisan Overlay UI (Informasi Hasil Scan)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp, start = 24.dp, end = 24.dp)
        ) {
            when (scannerState) {
                is ScannerState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is ScannerState.Success -> {
                    val data = (scannerState as ScannerState.Success).data
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Berhasil: ${data.nama}", style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${data.status}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.resetScanner() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("SCAN BERIKUTNYA")
                            }
                        }
                    }
                }

                is ScannerState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Gagal!", style = MaterialTheme.typography.titleMedium)
                            Text((scannerState as ScannerState.Error).message)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.resetScanner() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("COBA LAGI", color = Color.White)
                            }
                        }
                    }
                }
                else -> { /* State Idle: Tidak menampilkan apa-apa */ }
            }
        }
    }
}

@Composable
fun CameraPreview(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember { PreviewView(context) }

    // Gunakan LaunchedEffect agar binding hanya terjadi SEKALI saat layar dibuka
    LaunchedEffect(Unit) {
        val cameraProviderFuture = androidx.camera.lifecycle.ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy, onQrScanned)
            }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // AndroidView sekarang hanya bertanggung jawab menampilkan View-nya saja
    AndroidView(
        factory = { previewView },
        modifier = Modifier.fillMaxSize()
    )
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(imageProxy: ImageProxy, onQrScanned: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { nim ->
                        onQrScanned(nim)
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
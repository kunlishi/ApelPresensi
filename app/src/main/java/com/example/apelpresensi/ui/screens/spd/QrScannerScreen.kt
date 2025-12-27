@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.example.apelpresensi.ui.screens.spd

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.example.apelpresensi.ui.viewmodel.ScannerState
import com.example.apelpresensi.ui.viewmodel.SpdViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun QrScannerScreen(
    tingkat: String,
    viewModel: SpdViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scannerState by viewModel.scannerState

    // Reset state saat layar dibuka
    LaunchedEffect(Unit) { viewModel.resetScanner() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan QR - Tingkat $tingkat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Tampilan Kamera
            CameraPreview(onQrScanned = { nim ->
                if (scannerState is ScannerState.Idle) {
                    viewModel.processScannedNim(nim, tingkat)
                }
            })

            // Overlay Informasi (Sukses/Error)
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp)
            ) {
                when (scannerState) {
                    is ScannerState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    is ScannerState.Success -> {
                        val data = (scannerState as ScannerState.Success).data
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Berhasil: ${data.nama}", style = MaterialTheme.typography.titleMedium)
                                Text("Status: ${data.status}")
                                Button(onClick = { viewModel.resetScanner() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Scan Selanjutnya")
                                }
                            }
                        }
                    }
                    is ScannerState.Error -> {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Gagal: ${(scannerState as ScannerState.Error).message}")
                                Button(onClick = { viewModel.resetScanner() }, modifier = Modifier.fillMaxWidth()) {
                                    Text("Coba Lagi")
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun CameraPreview(onQrScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                val scanner = BarcodeScanning.getClient()
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    processImageProxy(scanner, imageProxy, onQrScanned)
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis)
                } catch (e: Exception) { Log.e("Camera", "Binding failed", e) }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(scanner: BarcodeScanner, imageProxy: ImageProxy, onQrScanned: (String) -> Unit) {
    imageProxy.image?.let { mediaImage ->
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { onQrScanned(it) }
            }
            .addOnCompleteListener { imageProxy.close() }
    } ?: imageProxy.close()
}
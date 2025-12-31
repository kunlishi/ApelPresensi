package com.example.apelpresensi.ui.screens.spd

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.apelpresensi.ui.screens.admin.StatusBadge
import com.example.apelpresensi.ui.viewmodel.ScannerState
import com.example.apelpresensi.ui.viewmodel.SpdViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    scheduleId: Long,
    viewModel: SpdViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Scanner", "Riwayat")

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    // 1. Izin Kamera hanya dipicu sekali saat masuk screen
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    // 2. TRIGGER UTAMA: Panggil data riwayat setiap kali selectedTab berubah ke 1
    LaunchedEffect(selectedTab) {
        if (selectedTab == 1) {
            viewModel.fetchHistoryBySchedule(scheduleId)
        } else {
            // Saat kembali ke tab scanner, pastikan state kembali ke Idle
            viewModel.resetScanner()
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Presensi Apel") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (selectedTab == 0) Color.Black else MaterialTheme.colorScheme.surface,
                        titleContentColor = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = if (selectedTab == 0) Color.Black else MaterialTheme.colorScheme.surface,
                    contentColor = if (selectedTab == 0) Color.White else MaterialTheme.colorScheme.primary
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) },
                            icon = {
                                Icon(
                                    if (index == 0) Icons.Default.QrCodeScanner else Icons.Default.History,
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (selectedTab == 0) {
                if (hasCameraPermission) {
                    QrScannerContent(scheduleId, viewModel)
                } else {
                    PermissionDeniedView { launcher.launch(Manifest.permission.CAMERA) }
                }
            } else {
                // Tab Riwayat akan menampilkan data yang sudah di-fetch oleh LaunchedEffect(selectedTab)
                ScannerHistoryList(viewModel, scheduleId)
            }
        }
    }
}

@Composable
fun QrScannerContent(
    scheduleId: Long,
    viewModel: SpdViewModel
) {
    val scannerState by viewModel.scannerState
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(scannerState) {
        if (scannerState is ScannerState.NeedConfirmation) {
            showConfirmDialog = true
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        CameraPreview(onQrScanned = { nim ->
            if (scannerState is ScannerState.Idle) {
                viewModel.scanQR(nim, scheduleId)
            }
        })

        // 1. Frame Focus Scanner
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.Center)
        ) {
            ScannerFrameGuide()
        }

        // 2. Overlay Hasil Scan
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp, start = 24.dp, end = 24.dp)
        ) {
            ScannerResultOverlay(scannerState) { viewModel.resetScanner() }
        }

        // 3. Dialog Konfirmasi Manual SPD (±5 MENIT)
        if (showConfirmDialog && scannerState is ScannerState.NeedConfirmation) {
            val data = (scannerState as ScannerState.NeedConfirmation).data
            ConfirmationDialog(
                data = data,
                onConfirm = {
                    viewModel.confirmManual(data.nim!!, scheduleId, "HADIR")
                    showConfirmDialog = false
                },
                onDeny = {
                    viewModel.confirmManual(data.nim!!, scheduleId, "TERLAMBAT")
                    showConfirmDialog = false
                },
                onDismiss = {
                    showConfirmDialog = false
                    viewModel.resetScanner()
                }
            )
        }
    }
}

@Composable
fun ScannerHistoryList(viewModel: SpdViewModel, scheduleId: Long) {
    val history = viewModel.filteredHistory
    val searchQuery = viewModel.searchQuery
    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Kolom Pencarian
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Cari Nama atau NIM...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (history.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Data tidak ditemukan", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(history) { record ->
                    ListItem(
                        headlineContent = { Text(record.nama ?: record.nim, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(text ="NIM: ${record.nim} | Kelas: ${record.kelas}   ${record.waktuPresensi}", style = MaterialTheme.typography.labelSmall) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Status Badge
                                StatusBadge(record.status.toString())

                                // 2. Tombol Hapus
                                IconButton(onClick = { showDeleteConfirm = record.id }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                                }
                            }
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus
    showDeleteConfirm?.let { id ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("Hapus Presensi?") },
            text = { Text("Apakah Anda yakin ingin menghapus data presensi ini? Mahasiswa harus melakukan scan ulang.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePresensi(id, scheduleId)
                    showDeleteConfirm = null
                }) { Text("HAPUS", color = Color.Red) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) { Text("BATAL") }
            }
        )
    }
}

@Composable
fun ScannerFrameGuide() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = 4.dp.toPx()
        val cornerLength = 40.dp.toPx()
        val color = Color.White

        // Top Left
        drawLine(color, Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth)
        drawLine(color, Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth)

        // Top Right
        drawLine(color, Offset(size.width, 0f), Offset(size.width - cornerLength, 0f), strokeWidth)
        drawLine(color, Offset(size.width, 0f), Offset(size.width, cornerLength), strokeWidth)

        // Bottom Left
        drawLine(color, Offset(0f, size.height), Offset(cornerLength, size.height), strokeWidth)
        drawLine(color, Offset(0f, size.height), Offset(0f, size.height - cornerLength), strokeWidth)

        // Bottom Right
        drawLine(color, Offset(size.width, size.height), Offset(size.width - cornerLength, size.height), strokeWidth)
        drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - cornerLength), strokeWidth)
    }
}

@Composable
fun ScannerResultOverlay(state: ScannerState, onReset: () -> Unit) {
    when (state) {
        is ScannerState.Loading -> {
            CircularProgressIndicator(color = Color.White)
        }
        is ScannerState.Success -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ BERHASIL", fontWeight = FontWeight.Black, color = Color.Green)
                    Text(state.data.nama ?: "", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Status: ${state.data.status}", color = Color.LightGray)
                    Button(onClick = onReset, modifier = Modifier.padding(top = 12.dp)) {
                        Text("SCAN BERIKUTNYA")
                    }
                }
            }
        }
        is ScannerState.Error -> {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C).copy(alpha = 0.9f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ GAGAL!", fontWeight = FontWeight.Black, color = Color.White)
                    Text(state.message, color = Color.White, textAlign = TextAlign.Center)
                    Button(onClick = onReset, modifier = Modifier.padding(top = 12.dp)) {
                        Text("COBA LAGI")
                    }
                }
            }
        }
        else -> {}
    }
}

@Composable
fun ConfirmationDialog(data: com.example.apelpresensi.data.remote.dto.ScanResponse, onConfirm: () -> Unit, onDeny: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Help, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Verifikasi Masa Transisi") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(data.nama ?: "Mahasiswa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(data.nim ?: "", color = Color.Gray)
                Spacer(Modifier.height(8.dp))
                Text("Berada di jendela ±5 menit. Tentukan status:", textAlign = TextAlign.Center)
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
                Text("HADIR")
            }
        },
        dismissButton = {
            Button(onClick = onDeny, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                Text("TERLAMBAT")
            }
        }
    )
}

@Composable
fun PermissionDeniedView(onRequest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Izin kamera diperlukan untuk melakukan scan.", color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRequest) { Text("Berikan Izin") }
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

@OptIn(ExperimentalGetImage::class)
private fun processImageProxy(imageProxy: ImageProxy, onQrScanned: (String) -> Unit) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { rawText ->
                        val parsedNim = rawText.substringBefore(";")
                        onQrScanned(parsedNim)
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
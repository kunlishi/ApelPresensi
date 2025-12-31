package com.example.apelpresensi.ui.screens.admin

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.apelpresensi.data.remote.dto.IzinResponse
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AdminViewModel
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IzinValidationScreen(
    viewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State Tabs: 0 = Pending, 1 = Riwayat (Diterima/Ditolak)
    var selectedTab by remember { mutableIntStateOf(0) }

    // State Filter Tanggal
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var filterDate by remember { mutableStateOf("") }

    var selectedIzinForBukti by remember { mutableStateOf<IzinResponse?>(null) }
    var showProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchIzin()
        authViewModel.fetchMe()
    }

    // Fungsi Format Tanggal
    fun formatMillisToDate(millis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    // Logika Filtering
    val filteredList = remember(selectedTab, filterDate, viewModel.izinList) {
        viewModel.izinList.filter { izin ->
            val matchesStatus = if (selectedTab == 0) {
                izin.statusBukti == "MENUNGGU"
            } else {
                izin.statusBukti == "DITERIMA" || izin.statusBukti == "DITOLAK"
            }
            val matchesDate = filterDate.isEmpty() || izin.tanggal == filterDate
            matchesStatus && matchesDate
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MainTopAppBar(
                title = "Validasi Izin",
                onBackClick = onBack,
                onProfileClick = { showProfile = true }
            )
        },
        floatingActionButton = {
            // Tombol Export hanya muncul di Tab Riwayat
            if (selectedTab == 1 && filteredList.isNotEmpty()) {
                FloatingActionButton(onClick = {
                    exportIzinToCSV(context, filteredList, "Rekap_Izin_${filterDate.ifEmpty { "Semua" }}")
                }) {
                    Icon(Icons.Default.Download, contentDescription = "Export CSV")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // 1. Tab Selector
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; filterDate = "" }) {
                    Text("Pending", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Riwayat", modifier = Modifier.padding(16.dp))
                }
            }

            // 2. Filter Tanggal (Hanya di Tab Riwayat)
            if (selectedTab == 1) {
                OutlinedTextField(
                    value = if (filterDate.isEmpty()) "Semua Tanggal" else filterDate,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Filter Berdasarkan Tanggal") },
                    leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                    trailingIcon = {
                        if (filterDate.isNotEmpty()) {
                            IconButton(onClick = { filterDate = "" }) { Icon(Icons.Default.Clear, null) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).clickable { showDatePicker = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (viewModel.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            // 3. List Izin
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                if (filteredList.isEmpty() && !viewModel.isLoading) {
                    item {
                        Text("Tidak ada data pengajuan.", modifier = Modifier.padding(16.dp), color = Color.Gray)
                    }
                }

                items(filteredList) { izin ->
                    var catatanAdmin by remember { mutableStateOf(izin.catatanAdmin ?: "") }
                    var isError by remember { mutableStateOf(false) }
                    val isPdf = izin.buktiBase64?.startsWith("JVBER") ?: false
                    var icon: ImageVector = if(isPdf) Icons.Default.PictureAsPdf else Icons.Default.Image

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = izin.mahasiswaNama, fontWeight = FontWeight.Bold)
                                if (selectedTab == 1) {
                                    StatusBadge(izin.statusBukti)
                                }
                            }
                            Text(text = "NIM: ${izin.mahasiswaNim} | ${izin.tanggal}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Jenis: ${izin.jenis}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            Text(text = "Keterangan: ${izin.keterangan}", modifier = Modifier.padding(vertical = 8.dp))

                            // Tombol Lihat Bukti
                            if (izin.hasBukti && !izin.buktiBase64.isNullOrEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        if (izin.buktiBase64.startsWith("JVBER")) {
                                            openPdfFromBase64(context, izin.buktiBase64, "bukti_${izin.id}")
                                        } else {
                                            selectedIzinForBukti = izin
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(icon, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Lihat Lampiran")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Input Catatan (Hanya di Tab Pending)
                            if (selectedTab == 0) {
                                OutlinedTextField(
                                    value = catatanAdmin,
                                    onValueChange = { catatanAdmin = it; isError = false },
                                    label = { Text("Catatan Admin") },
                                    modifier = Modifier.fillMaxWidth(),
                                    isError = isError,
                                    supportingText = { if (isError) Text("Wajib diisi jika menolak") }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.validateIzin(izin.id, "DITERIMA", catatanAdmin) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Terima") }
                                    Button(
                                        onClick = {
                                            if (catatanAdmin.isBlank()) {
                                                isError = true
                                                scope.launch { snackbarHostState.showSnackbar("Isi catatan untuk menolak") }
                                            } else {
                                                viewModel.validateIzin(izin.id, "DITOLAK", catatanAdmin)
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f)
                                    ) { Text("Tolak") }
                                }
                            } else {
                                // Tampilan Catatan di Tab Riwayat
                                if (!izin.catatanAdmin.isNullOrBlank()) {
                                    Text(
                                        text = "Catatan Admin: ${izin.catatanAdmin}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { filterDate = formatMillisToDate(it) }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } }
            ) { DatePicker(state = datePickerState) }
        }

        // Image Preview Dialog
        selectedIzinForBukti?.let { izin ->
            ImagePreviewDialog(base64String = izin.buktiBase64 ?: "", onDismiss = { selectedIzinForBukti = null })
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

@Composable
fun StatusBadge(status: String) {
    val color = when (status) {
        "DITERIMA" -> Color(0xFF4CAF50)
        "DITOLAK" -> Color.Red
        else -> Color.Gray
    }
    Surface(color = color.copy(alpha = 0.1f), shape = MaterialTheme.shapes.extraSmall) {
        Text(
            text = status,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontWeight = FontWeight.Bold
        )
    }
}

// Fungsi Export CSV Khusus Izin
fun exportIzinToCSV(context: Context, data: List<IzinResponse>, fileName: String) {
    val header = "Nama,NIM,Tanggal,Jenis,Alasan,Status,Catatan Admin\n"
    val content = data.joinToString("\n") { item ->
        val keterangan = item.keterangan.replace(",", " ")
        val catatan = item.catatanAdmin?.replace(",", " ") ?: ""
        "${item.mahasiswaNama},${item.mahasiswaNim},${item.tanggal},${item.jenis},$keterangan,${item.statusBukti},$catatan"
    }
    val fullData = header + content
    try {
        val file = File(context.cacheDir, "$fileName.csv")
        FileOutputStream(file).use { it.write(fullData.toByteArray()) }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Bagikan Rekap Izin"))
    } catch (e: Exception) { e.printStackTrace() }
}
@Composable
fun ImagePreviewDialog(base64String: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(16.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                val bitmap = remember(base64String) {
                    val decodedString = Base64.decode(base64String, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                }

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Bukti Izin",
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text("Tutup") }
            }
        }
    }
}

// Fungsi untuk membuka PDF dari Base64 menggunakan Intent
fun openPdfFromBase64(context: Context, base64String: String, fileName: String) {
    try {
        val pdfAsBytes = Base64.decode(base64String, Base64.DEFAULT)
        val file = File(context.cacheDir, "$fileName.pdf")
        FileOutputStream(file).use { it.write(pdfAsBytes) }

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Buka File PDF"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
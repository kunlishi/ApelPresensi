package com.example.apelpresensi.ui.screens.admin

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.apelpresensi.data.remote.dto.PresensiDetailResponse
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AdminViewModel
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun RekapPresensiScreen(
    scheduleId: Long,
    viewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var showProfile by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("Semua") }
    var selectedClass by remember { mutableStateOf("Semua") }

    LaunchedEffect(scheduleId) {
        // Pastikan nama method di AdminViewModel adalah fetchRekapPresensi
        // sesuai diskusi sinkronisasi backend sebelumnya
        viewModel.fetchRekap(scheduleId)
        authViewModel.fetchMe()
    }

    val filteredList = remember(searchQuery, selectedStatus, selectedClass, viewModel.rekapList.value) {
        viewModel.rekapList.value
            .filter { item ->
                (searchQuery.isEmpty() || item.nama.contains(searchQuery, ignoreCase = true) || item.nim.contains(searchQuery)) &&
                        (selectedStatus == "Semua" || item.status == selectedStatus) &&
                        (selectedClass == "Semua" || item.kelas == selectedClass)
            }
            .sortedBy { it.nama }
    }

    val availableClasses = remember(viewModel.rekapList.value) {
        listOf("Semua") + viewModel.rekapList.value.map { it.kelas }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Rekap Kehadiran",
                onBackClick = onBack,
                onProfileClick = { showProfile = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                exportToCSVFile(context, filteredList, "Rekap_Apel_$scheduleId")
            }) {
                Icon(Icons.Default.Share, contentDescription = "Export CSV File")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Cari Nama atau NIM...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // FILTER STATUS - Diperbarui agar sesuai status dari Backend baru
                var statusExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { statusExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedStatus)
                    }
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        // Tambahkan status IZIN, SAKIT, dan TIDAK_HADIR
                        listOf("Semua", "HADIR", "TERLAMBAT", "IZIN", "SAKIT", "TIDAK_HADIR").forEach { status ->
                            DropdownMenuItem(
                                text = { Text(status) },
                                onClick = { selectedStatus = status; statusExpanded = false }
                            )
                        }
                    }
                }

                var classExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(onClick = { classExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                        Text(selectedClass)
                    }
                    DropdownMenu(expanded = classExpanded, onDismissRequest = { classExpanded = false }) {
                        availableClasses.forEach { kelas ->
                            DropdownMenuItem(
                                text = { Text(kelas) },
                                onClick = { selectedClass = kelas; classExpanded = false }
                            )
                        }
                    }
                }
            }

            if (viewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.nama, fontWeight = FontWeight.Bold)
                                Text(text = "NIM: ${item.nim} | Kelas: ${item.kelas}", style = MaterialTheme.typography.bodySmall)
                                // Tampilkan catatan jika ada (misal alasan izin)
                                if (!item.catatan.isNullOrBlank()) {
                                    Text(text = "Catatan: ${item.catatan}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                }
                            }

                            // WARNA BADGE - Diperbarui agar lebih detail
                            val (color, text) = when (item.status) {
                                "HADIR" -> Color(0xFF4CAF50) to "HADIR"
                                "TERLAMBAT" -> Color(0xFFFFA000) to "TERLAMBAT"
                                "IZIN", "SAKIT" -> Color(0xFF2196F3) to item.status
                                "TIDAK_HADIR" -> Color.Red to "TIDAK_HADIR"
                                else -> Color.Gray to item.status
                            }

                            Surface(color = color, shape = MaterialTheme.shapes.extraSmall) {
                                Text(
                                    text = text,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall
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
// Fungsi Export CSV
fun exportToCSVFile(context: Context, data: List<PresensiDetailResponse>, fileName: String) {
    val header = "Kelas,NIM,Nama,Status,Catatan\n"
    val content = data.joinToString("\n") { item ->
        // Langsung gunakan item.status karena sudah deskriptif (HADIR/TIDAK_HADIR/IZIN dll)
        val catatan = item.catatan?.replace(",", " ") ?: "" // Bersihkan koma agar CSV tidak berantakan
        "${item.kelas},${item.nim},${item.nama},${item.status},$catatan"
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
        context.startActivity(Intent.createChooser(intent, "Bagikan File Rekap"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
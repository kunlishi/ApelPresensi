package com.example.apelpresensi.ui.screens.mahasiswa

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.data.remote.dto.IzinResponse
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import com.example.apelpresensi.ui.viewmodel.MahasiswaViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatIzinScreen(
    viewModel: MahasiswaViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToChangePassword: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showProfile by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val izinList by viewModel.izinHistory
    val isLoading by viewModel.isFetchingIzin

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    var selectedDateText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.fetchMyIzin()
    }

    fun formatMillisToDate(millis: Long): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(Date(millis))
    }

    // Filter list berdasarkan tanggal yang dipilih
    val filteredList = if (selectedDateText.isEmpty()) {
        izinList
    } else {
        izinList.filter { it.tanggal == selectedDateText }
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Riwayat Izin",
                onBackClick = onBack,
                onProfileClick = { /* Handle profile click */ }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Area Filter Tanggal
            OutlinedTextField(
                value = if (selectedDateText.isEmpty()) "Semua Tanggal" else selectedDateText,
                onValueChange = {},
                readOnly = true, // Agar tidak bisa diketik manual
                label = { Text("Filter Tanggal") },
                leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                trailingIcon = {
                    if (selectedDateText.isNotEmpty()) {
                        IconButton(onClick = { selectedDateText = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable { showDatePicker = true },
                enabled = false, // Dinonaktifkan agar klik tertangkap oleh modifier clickable
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (filteredList.isEmpty() && !isLoading) {
                    item {
                        Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Tidak ada riwayat perizinan.", color = Color.Gray)
                        }
                    }
                }

                items(filteredList) { izin ->
                    IzinHistoryCard(izin)
                }
            }
        }

        // Dialog DatePicker
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateText = formatMillisToDate(it)
                        }
                        showDatePicker = false
                    }) { Text("Pilih") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Batal") }
                }
            ) {
                DatePicker(state = datePickerState)
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
                onChangePassword = onNavigateToChangePassword,
                onDismiss = { showProfile = false }
            )
        }
    }
}

@Composable
fun IzinHistoryCard(izin: IzinResponse) {
    val statusColor = when (izin.statusBukti) {
        "DITERIMA" -> Color(0xFF4CAF50)
        "DITOLAK" -> Color.Red
        else -> Color.Gray // MENUNGGU
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = izin.tanggal, fontWeight = FontWeight.Bold)
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = izin.statusBukti,
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(text = "Jenis: ${izin.jenis}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Keterangan: ${izin.keterangan}", style = MaterialTheme.typography.bodyMedium)

            if (!izin.catatanAdmin.isNullOrBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Catatan Admin:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = izin.catatanAdmin,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
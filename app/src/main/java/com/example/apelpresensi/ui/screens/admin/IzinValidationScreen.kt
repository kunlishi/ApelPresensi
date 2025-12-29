package com.example.apelpresensi.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AdminViewModel
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun IzinValidationScreen(
    viewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var showProfile by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.fetchIzin()
        authViewModel.fetchMe()
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Validasi Izin",
                onBackClick = onBack,
                onProfileClick = { showProfile = true }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pendingList = viewModel.izinList.filter { it.status == "MENUNGGU" }

                if (pendingList.isEmpty() && !viewModel.isLoading) {
                    item { Text("Tidak ada pengajuan izin pending.", modifier = Modifier.padding(16.dp)) }
                }

                items(pendingList) { izin ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = izin.mahasiswaNama, fontWeight = FontWeight.Bold)
                            Text(text = "Jenis: ${izin.jenis} | Tanggal: ${izin.tanggal}", style = MaterialTheme.typography.bodySmall)
                            Text(text = "Alasan: ${izin.keterangan}", modifier = Modifier.padding(vertical = 8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { viewModel.validateIzin(izin.id, "DITERIMA") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                ) { Text("Terima") }

                                Button(
                                    onClick = { viewModel.validateIzin(izin.id, "DITOLAK") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                                ) { Text("Tolak") }
                            }
                        }
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
                        authViewModel.logout()
                        onNavigateToLogin()
                    }
                },
                onDismiss = { showProfile = false }
            )
        }
    }
}
package com.example.apelpresensi.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AdminViewModel
import com.example.apelpresensi.ui.viewmodel.AuthViewModel

@Composable
fun RekapPresensiScreen(
    scheduleId: Long,
    viewModel: AdminViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    var showProfile by remember { mutableStateOf(false) }

    LaunchedEffect(scheduleId) {
        viewModel.fetchRekap(scheduleId)
    }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Rekap Kehadiran",
                onBackClick = onBack,
                onProfileClick = { showProfile = true }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (viewModel.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.rekapList.value) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.nama, style = MaterialTheme.typography.titleSmall)
                                Text(text = "NIM: ${item.nim}", style = MaterialTheme.typography.bodySmall)
                            }

                            // Badge Status
                            val (color, text) = when (item.status) {
                                "HADIR" -> Color(0xFF4CAF50) to "HADIR"
                                "TERLAMBAT" -> Color(0xFFFFA000) to "TERLAMBAT"
                                else -> Color.Red to "TIDAK HADIR"
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
                onLogout = onLogout,
                onDismiss = { showProfile = false }
            )
        }
    }
}
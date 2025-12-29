package com.example.apelpresensi.ui.screens.spd

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.components.MainTopAppBar
import com.example.apelpresensi.ui.components.ProfileDialog
import com.example.apelpresensi.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@Composable
fun SpdDashboard(
    authViewModel: AuthViewModel, // Tambahkan ini
    onScanClick: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var selectedTingkat by remember { mutableStateOf("1") }
    val tingkatOptions = listOf("1", "2", "3", "4")
    val scope = rememberCoroutineScope()
    var showProfile by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Petugas SPD",
                onBackClick = null,
                onProfileClick = { showProfile = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Pilih Tingkat Apel:",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tingkatOptions.forEach { tingkat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedTingkat == tingkat),
                            onClick = { selectedTingkat = tingkat }
                        )
                        Text(text = tingkat, color = MaterialTheme.colorScheme.onBackground)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onScanClick(selectedTingkat) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("BUKA SCANNER QR")
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
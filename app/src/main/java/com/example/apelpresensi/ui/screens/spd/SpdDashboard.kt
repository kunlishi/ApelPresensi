package com.example.apelpresensi.ui.screens.spd

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpdDashboard(
    onScanClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTingkat by remember { mutableStateOf("1") }
    val tingkatOptions = listOf("1", "2", "3", "4")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Petugas SPD") }) }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Pilih Tingkat Apel:", style = MaterialTheme.typography.titleMedium)

            Row(Modifier.padding(8.dp)) {
                tingkatOptions.forEach { tingkat ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (selectedTingkat == tingkat),
                            onClick = { selectedTingkat = tingkat }
                        )
                        Text(tingkat)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onScanClick(selectedTingkat) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Buka Scanner QR")
            }

            Spacer(modifier = Modifier.weight(1f))
            TextButton(onClick = onLogout) { Text("Logout", color = MaterialTheme.colorScheme.error) }
        }
    }
}
package com.example.apelpresensi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.data.remote.dto.UserResponse

@Composable
fun ProfileDialog(
    user: UserResponse?,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profil Pengguna") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (user != null) {
                    Text("Nama: ${user.name}")
                    Text("Username: ${user.username}")
                    Text("Role: ${user.role}")
                } else {
                    CircularProgressIndicator()
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) { Text("Logout") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        }
    )
}
package com.example.apelpresensi.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.Black // Sesuai desain Gold header, teks hitam
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary // Menggunakan Gold (#F9CD19)
        ),
        navigationIcon = {
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Info Akun",
                    tint = Color.Black
                )
            }
        },
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.secondary // Gunakan Maroon (#7D101F) untuk Logout
                )
            }
        }
    )
}
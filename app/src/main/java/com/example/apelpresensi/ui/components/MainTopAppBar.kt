package com.example.apelpresensi.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.apelpresensi.data.remote.dto.UserResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    title: String,
    onBackClick: (() -> Unit)? = null, // null berarti tidak tampil tombol back
    onProfileClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleMedium) },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack, contentDescription = "Kembali")
                }
            }
        },
        actions = {
            IconButton(onClick = onProfileClick) {
                Icon(imageVector = androidx.compose.material.icons.Icons.Default.AccountCircle, contentDescription = "Profil")
            }
        }
    )
}
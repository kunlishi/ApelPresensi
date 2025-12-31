package com.example.apelpresensi.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.ui.viewmodel.AuthState
import com.example.apelpresensi.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // State visibilitas untuk masing-masing field
    var oldVisible by remember { mutableStateOf(false) }
    var newVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ganti Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Keamanan Akun",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Pastikan password baru Anda sulit ditebak.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- Field Password Lama ---
            PasswordField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                label = "Password Saat Ini",
                isVisible = oldVisible,
                onToggle = { oldVisible = !oldVisible }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Field Password Baru ---
            PasswordField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = "Password Baru",
                isVisible = newVisible,
                onToggle = { newVisible = !newVisible }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Field Konfirmasi Password ---
            PasswordField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Ulangi Password Baru",
                isVisible = confirmVisible,
                onToggle = { confirmVisible = !confirmVisible }
            )

            // Validasi kecocokan password baru
            if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                Text(
                    text = "Konfirmasi password tidak cocok!",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Tombol Submit
            Button(
                onClick = {
                    // Sesuai endpoint PUT api/user/password
                    viewModel.changePassword(oldPassword, newPassword)
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = authState !is AuthState.Loading &&
                        newPassword == confirmPassword &&
                        newPassword.isNotEmpty() &&
                        oldPassword.isNotEmpty()
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Perbarui Password", fontWeight = FontWeight.Bold)
                }
            }

            // Pesan Error dari Backend (misal: Password lama salah)
            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Navigasi jika sukses
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onSuccess()
            viewModel.resetState() // Bersihkan state setelah sukses
        }
    }
}

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isVisible) "Sembunyikan" else "Tampilkan"
                )
            }
        }
    )
}
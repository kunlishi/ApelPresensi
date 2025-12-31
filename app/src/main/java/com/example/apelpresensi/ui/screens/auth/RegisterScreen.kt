package com.example.apelpresensi.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.apelpresensi.data.remote.dto.RegisterRequest
import com.example.apelpresensi.ui.viewmodel.AuthState
import com.example.apelpresensi.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var nim by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var nama by remember { mutableStateOf("") }
    var kelas by remember { mutableStateOf("") }
    var tingkat by remember { mutableStateOf("") }

    // State untuk kontrol visibilitas password
    var isPasswordVisible by remember { mutableStateOf(false) }

    val authState by viewModel.authState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Daftar Akun Mahasiswa",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Input Field NIM
        OutlinedTextField(
            value = nim,
            onValueChange = { nim = it },
            label = { Text("NIM") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input Field Nama
        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input Field Kelas
        OutlinedTextField(
            value = kelas,
            onValueChange = { kelas = it },
            label = { Text("Kelas") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input Field Tingkat
        OutlinedTextField(
            value = tingkat,
            onValueChange = { tingkat = it },
            label = { Text("Tingkat (1/2/3/4)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Input Field Password dengan Tombol Mata

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            // Logika perubahan tampilan teks password
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Daftar
        Button(
            onClick = {
                viewModel.register(RegisterRequest(nim, password, nama, kelas, tingkat))
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = authState !is AuthState.Loading && nim.isNotEmpty() && password.isNotEmpty()
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Daftar Sekarang")
            }
        }

        TextButton(onClick = onBackToLogin) {
            Text("Sudah punya akun? Login di sini")
        }

        // Penanganan Error
        if (authState is AuthState.Error) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Efek Navigasi Sukses
        LaunchedEffect(authState) {
            if (authState is AuthState.Success) {
                onRegisterSuccess()
            }
        }
    }
}
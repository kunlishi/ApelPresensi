package com.example.apelpresensi.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    val authState by viewModel.authState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Daftar Akun Mahasiswa", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(value = nim, onValueChange = { nim = it }, label = { Text("NIM") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama Lengkap") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = kelas, onValueChange = { kelas = it }, label = { Text("Kelas") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = tingkat, onValueChange = { tingkat = it }, label = { Text("Tingkat (1/2/3/4)") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.register(RegisterRequest(nim, password, nama, kelas, tingkat))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authState !is AuthState.Loading
        ) {
            Text("Daftar Sekarang")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Sudah punya akun? Login di sini")
        }

        if (authState is AuthState.Error) {
            Text((authState as AuthState.Error).message, color = MaterialTheme.colorScheme.error)
        }

        LaunchedEffect(authState) {
            if (authState is AuthState.Success) {
                onRegisterSuccess()
            }
        }
    }
}
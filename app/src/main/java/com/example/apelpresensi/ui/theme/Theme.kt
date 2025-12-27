package com.example.apelpresensi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = Color.Black,
    secondary = SecondaryMaroon,
    onSecondary = Color.White,
    // Tambahkan parameter berikut agar tampilan Dark Mode sempurna
    background = DarkBackground,
    onBackground = BackgroundBeige, // Gunakan Beige sebagai warna teks agar nyaman di mata
    surface = DarkSurface,
    onSurface = BackgroundBeige
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGold,
    onPrimary = OnPrimaryColor,
    secondary = SecondaryMaroon,
    onSecondary = OnSecondaryColor,
    background = BackgroundBeige,
    onBackground = BodyGrey, // Warna teks di atas background
    surface = BackgroundBeige,
    onSurface = BodyGrey     // Warna teks di atas surface (Card, Button, dll)
)

@Composable
fun ApelPresensiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Otomatis mengikuti setting HP
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
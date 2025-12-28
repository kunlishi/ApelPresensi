// Lokasi: app/src/main/java/com/example/apelpresensi/util/QrGenerator.kt

package com.example.apelpresensi.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

object QrGenerator {
    fun generateQrCode(text: String): Bitmap? {
        if (text.isEmpty()) return null // Hindari generate QR dari teks kosong

        val writer = QRCodeWriter()
        return try {
            // Tambahkan Hint agar QR memiliki margin (Quiet Zone) yang cukup
            val hints = mapOf(EncodeHintType.MARGIN to 2)

            val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height

            // Gunakan ARGB_8888 untuk kualitas warna yang lebih stabil di berbagai layar
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }
}
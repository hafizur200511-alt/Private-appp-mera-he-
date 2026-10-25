package com.example.data

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap

object QrCodeGenerator {
    fun generateUpiQrBitmap(
        upiId: String,
        payeeName: String,
        amountInRupees: String,
        transactionNote: String = "MEGA SMM Recharge",
        size: Int = 512
    ): Bitmap? {
        return try {
            val amountParam = if (amountInRupees.isNotBlank() && amountInRupees.toDoubleOrNull() != null) {
                "&am=$amountInRupees"
            } else ""

            val upiUri = "upi://pay?pa=$upiId&pn=${java.net.URLEncoder.encode(payeeName, "UTF-8")}$amountParam&cu=INR&tn=${java.net.URLEncoder.encode(transactionNote, "UTF-8")}"

            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(upiUri, BarcodeFormat.QR_CODE, size, size, hints)

            val width = bitMatrix.width
            val height = bitMatrix.height
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

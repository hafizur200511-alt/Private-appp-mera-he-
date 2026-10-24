package com.example.data

import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyUtils {
    fun formatPaiseToRupee(paise: Long): String {
        val bd = BigDecimal(paise).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
        return "₹${bd.toPlainString()}"
    }

    fun rupeeStringToPaise(rupeeText: String): Long? {
        val clean = rupeeText.replace("₹", "").replace(",", "").trim()
        if (clean.isBlank()) return null
        return try {
            val bd = BigDecimal(clean)
            if (bd < BigDecimal.ZERO) return null
            bd.multiply(BigDecimal(100)).toLong()
        } catch (e: Exception) {
            null
        }
    }
}

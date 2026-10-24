package com.example.model

data class WalletLedgerEntry(
    val id: String = "",
    val userId: String = "",
    val type: String = "CREDIT", // "CREDIT", "DEBIT", "REFUND"
    val amountPaise: Long = 0L,
    val balanceAfterPaise: Long = 0L,
    val description: String = "",
    val referenceId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val formattedAmount: String
        get() {
            val sign = if (type == "CREDIT" || type == "REFUND") "+" else "-"
            return String.format("%s₹%.2f", sign, amountPaise / 100.0)
        }
}

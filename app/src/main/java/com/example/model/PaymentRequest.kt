package com.example.model

data class PaymentRequest(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val amountPaise: Long = 0L,
    val utrNumber: String = "",
    val paymentMethod: String = "UPI_QR",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long = 0L,
    val reviewedBy: String = ""
) {
    val formattedAmount: String
        get() = String.format("₹%.2f", amountPaise / 100.0)
}

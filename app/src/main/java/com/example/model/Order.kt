package com.example.model

data class Order(
    val id: String = "",
    val orderId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val platform: String = "Instagram",
    val category: String = "Followers",
    val targetLink: String = "",
    val quantity: Long = 0L,
    val pricePaise: Long = 0L,
    val deliveryMethod: String = "MANUAL_ADMIN", // "AUTOMATIC_API" or "MANUAL_ADMIN"
    val providerId: String = "",
    val providerServiceId: String = "",
    val providerOrderId: String = "",
    val status: String = "PENDING", // "PENDING", "AWAITING_ADMIN", "IN_PROGRESS", "COMPLETED", "PARTIAL", "CANCELLED", "FAILED"
    val adminNotes: String = "",
    val refundedAmountPaise: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val formattedPrice: String
        get() = String.format("₹%.2f", pricePaise / 100.0)

    val formattedRefunded: String
        get() = String.format("₹%.2f", refundedAmountPaise / 100.0)

    val canBeCancelledOrRefunded: Boolean
        get() = status != "COMPLETED" && status != "CANCELLED" && status != "REFUNDED"
}

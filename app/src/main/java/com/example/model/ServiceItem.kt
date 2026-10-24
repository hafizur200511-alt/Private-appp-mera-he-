package com.example.model

data class ServiceItem(
    val id: String = "",
    val serviceId: String = "",
    val name: String = "",
    val platform: String = "Instagram",
    val category: String = "Followers",
    val description: String = "",
    val minQuantity: Long = 100L,
    val maxQuantity: Long = 10000L,
    val pricePaise: Long = 4000L, // Price in paise (e.g. 4000 paise = ₹40.00)
    val priceUnit: String = "PER_1000", // "PER_1000" or "FIXED"
    val deliveryMethod: String = "MANUAL_ADMIN", // "AUTOMATIC_API" or "MANUAL_ADMIN"
    val providerId: String = "",
    val providerServiceId: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE", "UNAVAILABLE"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun isAvailableForCustomers(): Boolean = status.equals("ACTIVE", ignoreCase = true)

    val formattedPriceUnit: String
        get() = if (priceUnit == "PER_1000") {
            String.format("₹%.2f per 1000", pricePaise / 100.0)
        } else {
            String.format("₹%.2f fixed", pricePaise / 100.0)
        }

    fun calculateTotalPaise(quantity: Long): Long {
        return if (priceUnit == "FIXED") {
            pricePaise
        } else {
            // Price per 1000: avoid floating-point inaccuracies
            // total = (quantity * pricePaise) / 1000
            val product = quantity * pricePaise
            (product + 999) / 1000 // ceiling division to prevent undercharging
        }
    }
}

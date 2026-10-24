package com.example.model

data class ApiProvider(
    val id: String = "",
    val name: String = "",
    val baseUrl: String = "",
    val apiKey: String = "",
    val authType: String = "PARAM", // "PARAM" (key in POST body) or "BEARER"
    val enabled: Boolean = true,
    val balance: String = "₹0.00",
    val currency: String = "INR",
    val lastChecked: Long = System.currentTimeMillis()
)

package com.example.model

data class UpiSettings(
    val upiId: String = "rahmansmm@fam",
    val payeeName: String = "MEGA SMM INDIA",
    val supportInstagram: String = "megasmmindia",
    val supportEmail: String = "support@megasmm.in",
    val minRechargePaise: Long = 1000L, // ₹10 minimum
    val isRechargeEnabled: Boolean = true
)

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "INFO", // "INFO", "SUCCESS", "WARNING"
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

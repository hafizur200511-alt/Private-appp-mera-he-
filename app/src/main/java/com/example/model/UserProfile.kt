package com.example.model

import com.google.firebase.auth.FirebaseUser

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val provider: String = "email", // "google" or "email"
    val role: String = "user", // "admin" or "user"
    val walletBalancePaise: Long = 0L,
    val totalOrders: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Priority fallback requirement:
     * Firestore displayName -> Firebase Auth displayName -> email prefix -> Member
     * Never display null, undefined, or an empty string.
     */
    fun getEffectiveName(firebaseUser: FirebaseUser? = null): String {
        if (displayName.isNotBlank()) return displayName.trim()
        val authName = firebaseUser?.displayName
        if (!authName.isNullOrBlank()) return authName.trim()
        val targetEmail = if (email.isNotBlank()) email else firebaseUser?.email ?: ""
        if (targetEmail.isNotBlank()) {
            val prefix = targetEmail.substringBefore("@").trim()
            if (prefix.isNotBlank()) return prefix
        }
        return "Member"
    }

    fun isUserAdmin(firebaseUser: FirebaseUser? = null): Boolean {
        val emailToCheck = if (email.isNotBlank()) email.lowercase().trim() else firebaseUser?.email?.lowercase()?.trim() ?: ""
        return role.lowercase() == "admin" || emailToCheck == "hafizur200511@gmail.com"
    }

    val formattedBalance: String
        get() = String.format("₹%.2f", walletBalancePaise / 100.0)
}

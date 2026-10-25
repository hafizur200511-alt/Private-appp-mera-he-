package com.example.model

data class CategoryItem(
    val id: String = "",
    val name: String = "",
    val platform: String = "Instagram",
    val displayOrder: Int = 0,
    val isActive: Boolean = true,
    val iconName: String = "default"
)

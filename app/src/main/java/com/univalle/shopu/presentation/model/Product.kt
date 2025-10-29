package com.univalle.shopu.presentation.model

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String? = null,
    val category: String = "General"
)

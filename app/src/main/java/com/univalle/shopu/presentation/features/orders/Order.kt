package com.univalle.shopu.presentation.features.orders

data class Order(
    val id: String = "",
    val customer: String = "",
    val total: Double = 0.0,
    val status: String = "pendiente",
    val createdAt: Long = System.currentTimeMillis()
)

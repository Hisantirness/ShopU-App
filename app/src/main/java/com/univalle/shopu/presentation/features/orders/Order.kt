package com.univalle.shopu.presentation.features.orders

data class Order(
    val id: String = "",
    val customer: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val total: Double = 0.0,
    val status: String = "pendiente",
    val createdAt: Long = System.currentTimeMillis(),
    val paymentInfo: Map<String, String> = emptyMap()
)

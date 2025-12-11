package com.univalle.shopu.presentation.ui.orders

data class OrdersUiState(
    val search: String = "",
    val selectedTab: Int = 0,
    val orders: List<Order> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val message: String? = null,
    val localChanges: Map<String, String> = emptyMap()
)

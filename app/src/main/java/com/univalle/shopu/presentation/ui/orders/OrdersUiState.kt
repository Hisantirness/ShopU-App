package com.univalle.shopu.presentation.ui.orders

import com.univalle.shopu.domain.model.Order

data class OrdersUiState(
    val orders: List<Order> = emptyList(),
    val search: String = "",
    val selectedTab: Int = 0,
    val localChanges: Map<String, String> = emptyMap(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null
)

sealed class OrdersEvent {
    data class OnSearchChange(val value: String) : OrdersEvent()
    data class OnTabChange(val index: Int) : OrdersEvent()
    data class OnOrderStatusChange(val orderId: String, val newStatus: String) : OrdersEvent()
    object SaveChanges : OrdersEvent()
    object ClearMessage : OrdersEvent()
}

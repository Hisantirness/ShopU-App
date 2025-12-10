package com.univalle.shopu.presentation.ui.orders

sealed interface OrdersEvent {
    data class OnSearchChange(val value: String): OrdersEvent
    data class OnTabChange(val index: Int): OrdersEvent
    data class OnOrderStatusChange(val orderId: String, val newStatus: String): OrdersEvent
    data object SaveChanges: OrdersEvent
    data object ClearMessage: OrdersEvent
}

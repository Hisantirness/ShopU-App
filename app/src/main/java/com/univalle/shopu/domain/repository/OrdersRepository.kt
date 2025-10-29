package com.univalle.shopu.domain.repository

import com.univalle.shopu.presentation.features.orders.Order
import com.google.firebase.firestore.ListenerRegistration

interface OrdersRepository {
    fun observeOrders(onChange: (List<Order>) -> Unit): ListenerRegistration
    fun updateOrderStatus(orderId: String, newStatus: String, onFailure: () -> Unit)
}

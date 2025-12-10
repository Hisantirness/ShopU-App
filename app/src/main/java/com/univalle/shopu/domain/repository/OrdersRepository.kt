package com.univalle.shopu.domain.repository

import com.univalle.shopu.domain.model.Order
import com.google.firebase.firestore.ListenerRegistration

interface OrdersRepository {
    fun observeOrders(onChange: (List<Order>) -> Unit): ListenerRegistration
    suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit>
}

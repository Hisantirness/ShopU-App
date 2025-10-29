package com.univalle.shopu.data.repository.impl

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.domain.repository.OrdersRepository
import com.univalle.shopu.presentation.features.orders.Order

class FirebaseOrdersRepository : OrdersRepository {
    private val db = Firebase.firestore

    override fun observeOrders(onChange: (List<Order>) -> Unit): ListenerRegistration {
        return db.collection("orders").addSnapshotListener { snap, _ ->
            val orders = snap?.documents?.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) } ?: emptyList()
            onChange(orders)
        }
    }

    override fun updateOrderStatus(orderId: String, newStatus: String, onFailure: () -> Unit) {
        db.collection("orders").document(orderId)
            .update(mapOf("status" to newStatus))
            .addOnFailureListener { onFailure() }
    }
}

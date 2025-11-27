package com.univalle.shopu.data.repository.impl

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.domain.repository.OrdersRepository
import com.univalle.shopu.presentation.features.orders.Order
import com.univalle.shopu.domain.model.OrderStatus
import kotlinx.coroutines.tasks.await

class FirebaseOrdersRepository : OrdersRepository {
    private val db = Firebase.firestore

    override fun observeOrders(onChange: (List<Order>) -> Unit): ListenerRegistration {
        return db.collection("orders").addSnapshotListener { snap, _ ->
            val orders = snap?.documents?.mapNotNull { it.toObject(Order::class.java)?.copy(id = it.id) } ?: emptyList()
            onChange(orders)
        }
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: String): Result<Unit> {
        return try {
            // Primero obtener el estado actual del pedido
            val orderDoc = db.collection("orders").document(orderId).get().await()
            val previousStatus = orderDoc.getString("status") ?: ""
            
            android.util.Log.d("FirebaseOrdersRepo", "Updating order $orderId: $previousStatus -> $newStatus")
            
            // Actualizar el estado del pedido
            db.collection("orders").document(orderId)
                .update(mapOf("status" to newStatus))
                .await()
            
            android.util.Log.d("FirebaseOrdersRepo", "Order $orderId status updated to $newStatus")
            
            // Manejar el stock según el cambio de estado
            when {
                // Si cambia A "entregado": descontar stock
                newStatus == OrderStatus.DELIVERED && previousStatus != OrderStatus.DELIVERED -> {
                    try {
                        deductStock(orderId)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseOrdersRepo", "Stock deduction failed but order status was updated", e)
                    }
                }
                // Si cambia DE "entregado" a otro estado: devolver stock
                previousStatus == OrderStatus.DELIVERED && newStatus != OrderStatus.DELIVERED -> {
                    try {
                        restoreStock(orderId)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseOrdersRepo", "Stock restoration failed but order status was updated", e)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseOrdersRepo", "Error updating order status to $newStatus for order $orderId", e)
            Result.failure(e)
        }
    }
    
    private suspend fun deductStock(orderId: String) {
        db.runTransaction { transaction ->
            val orderRef = db.collection("orders").document(orderId)
            val orderSnap = transaction.get(orderRef)
            
            val items = orderSnap.get("items") as? List<Map<String, Any>> ?: emptyList()
            android.util.Log.d("FirebaseOrdersRepo", "Processing ${items.size} items for stock deduction")
            
            items.forEach { item ->
                val productId = item["id"] as? String
                val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                
                android.util.Log.d("FirebaseOrdersRepo", "Item: productId=$productId, quantity=$quantity")
                
                if (productId != null && quantity > 0) {
                    val productRef = db.collection("products").document(productId)
                    val productSnap = transaction.get(productRef)
                    
                    if (productSnap.exists()) {
                        val quantityObj = productSnap.get("quantity")
                        val currentStock = when (quantityObj) {
                            is Number -> quantityObj.toInt()
                            is String -> quantityObj.toIntOrNull() ?: 0
                            else -> 0
                        }
                        val newStock = (currentStock - quantity).coerceAtLeast(0)
                        
                        android.util.Log.d("FirebaseOrdersRepo", "Product $productId: $currentStock -> $newStock")
                        transaction.update(productRef, "quantity", newStock)
                    } else {
                        android.util.Log.w("FirebaseOrdersRepo", "Product $productId not found")
                    }
                } else {
                    android.util.Log.e("FirebaseOrdersRepo", "Invalid item: productId=$productId, quantity=$quantity")
                }
            }
        }.await()
        
        android.util.Log.d("FirebaseOrdersRepo", "Stock deduction completed for order $orderId")
    }
    
    private suspend fun restoreStock(orderId: String) {
        db.runTransaction { transaction ->
            val orderRef = db.collection("orders").document(orderId)
            val orderSnap = transaction.get(orderRef)
            
            val items = orderSnap.get("items") as? List<Map<String, Any>> ?: emptyList()
            android.util.Log.d("FirebaseOrdersRepo", "Restoring stock for ${items.size} items")
            
            items.forEach { item ->
                val productId = item["id"] as? String
                val quantity = (item["quantity"] as? Number)?.toInt() ?: 0
                
                android.util.Log.d("FirebaseOrdersRepo", "Restoring: productId=$productId, quantity=$quantity")
                
                if (productId != null && quantity > 0) {
                    val productRef = db.collection("products").document(productId)
                    val productSnap = transaction.get(productRef)
                    
                    if (productSnap.exists()) {
                        val quantityObj = productSnap.get("quantity")
                        val currentStock = when (quantityObj) {
                            is Number -> quantityObj.toInt()
                            is String -> quantityObj.toIntOrNull() ?: 0
                            else -> 0
                        }
                        val newStock = currentStock + quantity
                        
                        android.util.Log.d("FirebaseOrdersRepo", "Product $productId: $currentStock -> $newStock (restored)")
                        transaction.update(productRef, "quantity", newStock)
                    } else {
                        android.util.Log.w("FirebaseOrdersRepo", "Product $productId not found for restoration")
                    }
                } else {
                    android.util.Log.e("FirebaseOrdersRepo", "Invalid item for restoration: productId=$productId, quantity=$quantity")
                }
            }
        }.await()
        
        android.util.Log.d("FirebaseOrdersRepo", "Stock restoration completed for order $orderId")
    }
}

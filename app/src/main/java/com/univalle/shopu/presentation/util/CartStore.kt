package com.univalle.shopu.presentation.util

import androidx.compose.runtime.mutableStateListOf
import com.univalle.shopu.presentation.model.CartItem

object CartStore {
    val items = mutableStateListOf<CartItem>()

    fun add(item: CartItem) {
        val existing = items.find { it.id == item.id }
        if (existing != null) {
            existing.quantity += 1
            // Trigger recomposition by resetting list item
            val idx = items.indexOf(existing)
            if (idx >= 0) {
                items[idx] = existing.copy()
            }
        } else {
            items.add(item)
        }
    }

    fun inc(id: String) {
        val existing = items.find { it.id == id } ?: return
        existing.quantity += 1
        val idx = items.indexOf(existing)
        if (idx >= 0) items[idx] = existing.copy()
    }

    fun dec(id: String) {
        val existing = items.find { it.id == id } ?: return
        if (existing.quantity > 1) {
            existing.quantity -= 1
            val idx = items.indexOf(existing)
            if (idx >= 0) items[idx] = existing.copy()
        } else {
            items.remove(existing)
        }
    }

    fun remove(id: String) {
        items.removeAll { it.id == id }
    }

    fun clear() {
        items.clear()
    }

    fun total(): Double = items.sumOf { it.subtotal }
}

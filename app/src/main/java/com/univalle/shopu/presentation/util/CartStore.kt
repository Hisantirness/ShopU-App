package com.univalle.shopu.presentation.util

import androidx.compose.runtime.mutableStateListOf
import com.univalle.shopu.domain.model.CartItem

object CartStore {
    val items = mutableStateListOf<CartItem>()

    fun add(item: CartItem) {
        val existing = items.find { it.id == item.id }
        if (existing != null) {
            val idx = items.indexOf(existing)
            if (idx >= 0) {
                items[idx] = existing.copy(quantity = existing.quantity + 1)
            }
        } else {
            items.add(item)
        }
    }

    fun inc(id: String) {
        val existing = items.find { it.id == id } ?: return
        val idx = items.indexOf(existing)
        if (idx >= 0) items[idx] = existing.copy(quantity = existing.quantity + 1)
    }

    fun dec(id: String) {
        val existing = items.find { it.id == id } ?: return
        if (existing.quantity > 1) {
            val idx = items.indexOf(existing)
            if (idx >= 0) items[idx] = existing.copy(quantity = existing.quantity - 1)
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

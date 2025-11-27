package com.univalle.shopu.domain.model

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val imageUrl: String? = null
) {
    val subtotal: Double get() = price * quantity
}

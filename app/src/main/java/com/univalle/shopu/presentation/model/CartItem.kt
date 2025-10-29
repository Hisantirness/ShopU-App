package com.univalle.shopu.presentation.model

data class CartItem(
    val id: String,
    val name: String,
    val price: Double,
    var quantity: Int = 1,
    val imageUrl: String? = null
) {
    val subtotal: Double get() = price * quantity
}

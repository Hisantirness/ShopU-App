package com.univalle.shopu.domain.repository

import android.net.Uri
import com.google.firebase.firestore.ListenerRegistration
import com.univalle.shopu.domain.model.Product

interface ProductsRepository {
    fun observeProducts(onChange: (List<Product>) -> Unit): ListenerRegistration
    fun deleteProduct(productId: String, onFailure: () -> Unit)
    suspend fun createProduct(name: String, price: Double, quantity: Int, category: String, imageUrl: String?)
    suspend fun uploadImage(uri: Uri, mime: String): String
    suspend fun getProduct(productId: String): Product?
    suspend fun updateProduct(productId: String, name: String, price: Double, quantity: Int, category: String, imageUrl: String?)
}

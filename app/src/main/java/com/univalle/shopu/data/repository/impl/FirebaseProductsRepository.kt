package com.univalle.shopu.data.repository.impl

import android.net.Uri
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.univalle.shopu.domain.repository.ProductsRepository
import com.univalle.shopu.domain.model.Product
import kotlinx.coroutines.tasks.await

class FirebaseProductsRepository : ProductsRepository {
    private val db = Firebase.firestore
    private val storage = FirebaseStorage.getInstance("gs://shopu-prueba.appspot.com")

    override fun observeProducts(onChange: (List<Product>) -> Unit): ListenerRegistration {
        return db.collection("products").addSnapshotListener { snap, _ ->
            val products = snap?.documents?.mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) } ?: emptyList()
            onChange(products)
        }
    }

    override fun deleteProduct(productId: String, onFailure: () -> Unit) {
        db.collection("products").document(productId).delete().addOnFailureListener { onFailure() }
    }

    override suspend fun createProduct(name: String, price: Double, quantity: Int, category: String, imageUrl: String?) {
        val product = hashMapOf<String, Any?>(
            "name" to name.trim(),
            "price" to price.toDouble(),
            "quantity" to quantity.toInt(),
            "imageUrl" to (imageUrl ?: ""),
            "category" to category
        )
        
        if (imageUrl == null) product["imageUrl"] = null else product["imageUrl"] = imageUrl
        db.collection("products").add(product).await()
    }

    override suspend fun uploadImage(uri: Uri, mime: String): String {
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val ref = storage.reference.child("products/${'$'}{System.currentTimeMillis()}.$ext")
        val metadata = com.google.firebase.storage.StorageMetadata.Builder()
            .setContentType(mime)
            .build()
        ref.putFile(uri, metadata).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun getProduct(productId: String): Product? {
        val snap = db.collection("products").document(productId).get().await()
        return snap.toObject(Product::class.java)?.copy(id = snap.id)
    }

    override suspend fun updateProduct(
        productId: String,
        name: String,
        price: Double,
        quantity: Int,
        category: String,
        imageUrl: String?
    ) {
        val doc = hashMapOf(
            "name" to name.trim(),
            "price" to price,
            "quantity" to quantity,
            "imageUrl" to imageUrl,
            "category" to category
        )
        db.collection("products").document(productId).update(doc as Map<String, Any?>).await()
    }
}

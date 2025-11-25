package com.univalle.shopu.data.repository.impl

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.domain.repository.Worker
import com.univalle.shopu.domain.repository.WorkersRepository

class FirebaseWorkersRepository : WorkersRepository {
    private val db = Firebase.firestore

    override fun observeWorkers(onChange: (List<Worker>) -> Unit): ListenerRegistration {
        return db.collection("users")
            .whereEqualTo("role", "worker")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { d ->
                    val email = d.id
                    // Users might not have createdAt, so we default or use current time if needed.
                    // We try to read it if available, otherwise default.
                    val createdAt = d.getLong("createdAt") ?: System.currentTimeMillis()
                    Worker(email, createdAt)
                } ?: emptyList()
                onChange(list)
            }
    }

    override fun addWorker(email: String, createdAt: Long, onFailure: () -> Unit) {
        // Update user role to worker. We use SetOptions.merge() to be safe, 
        // ensuring we don't overwrite other fields if we were to set more data,
        // but here we just update the role.
        val data = mapOf(
            "role" to "worker",
            "workerSince" to createdAt
        )
        db.collection("users").document(email)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnFailureListener { onFailure() }
    }

    override fun removeWorker(email: String, onFailure: () -> Unit) {
        // Revert role to customer
        db.collection("users").document(email)
            .update("role", "customer")
            .addOnFailureListener { onFailure() }
    }
}

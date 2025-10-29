package com.univalle.shopu.data.repository.impl

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.domain.repository.Worker
import com.univalle.shopu.domain.repository.WorkersRepository

class FirebaseWorkersRepository : WorkersRepository {
    private val db = Firebase.firestore

    override fun observeWorkers(onChange: (List<Worker>) -> Unit): ListenerRegistration {
        return db.collection("workers").addSnapshotListener { snap, _ ->
            val list = snap?.documents?.map { d ->
                val email = d.id
                val createdAt = d.getLong("createdAt") ?: System.currentTimeMillis()
                Worker(email, createdAt)
            } ?: emptyList()
            onChange(list)
        }
    }

    override fun addWorker(email: String, createdAt: Long, onFailure: () -> Unit) {
        db.collection("workers").document(email)
            .set(mapOf("email" to email, "createdAt" to createdAt))
            .addOnFailureListener { onFailure() }
    }

    override fun removeWorker(email: String, onFailure: () -> Unit) {
        db.collection("workers").document(email)
            .delete()
            .addOnFailureListener { onFailure() }
    }
}

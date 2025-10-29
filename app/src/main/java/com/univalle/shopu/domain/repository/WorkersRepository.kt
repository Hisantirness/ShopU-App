package com.univalle.shopu.domain.repository

import com.google.firebase.firestore.ListenerRegistration

data class Worker(val email: String = "", val createdAt: Long = System.currentTimeMillis())

interface WorkersRepository {
    fun observeWorkers(onChange: (List<Worker>) -> Unit): ListenerRegistration
    fun addWorker(email: String, createdAt: Long, onFailure: () -> Unit)
    fun removeWorker(email: String, onFailure: () -> Unit)
}

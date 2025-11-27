package com.univalle.shopu.domain.model

/**
 * Constantes para los estados de pedidos en la aplicación
 */
object OrderStatus {
    const val PENDING = "pendiente"
    const val IN_PROGRESS = "en_proceso"
    const val READY = "listo"
    const val DELIVERED = "entregado"
    const val CANCELLED = "cancelado"
    
    val ALL_STATUSES = listOf(PENDING, IN_PROGRESS, READY, DELIVERED, CANCELLED)
    
    fun getDisplayName(status: String): String {
        return when (status) {
            PENDING -> "Pendiente"
            IN_PROGRESS -> "En progreso"
            READY -> "Listo"
            DELIVERED -> "Entregado"
            CANCELLED -> "Cancelado"
            else -> status
        }
    }
}

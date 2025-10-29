package com.univalle.shopu.presentation.util

fun isInstitutionalEmail(email: String): Boolean {
    val e = email.trim()
    if (e.isEmpty()) return false
    return e.endsWith("@correounivalle.edu.co", ignoreCase = true) ||
            e.endsWith("@univalle.edu.co", ignoreCase = true) ||
            e.contains("@estudiantes.univalle.edu.co", ignoreCase = true)
}

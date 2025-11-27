package com.univalle.shopu.presentation.util

import java.text.NumberFormat
import java.util.Locale

fun formatCurrencyCOP(value: Double): String {
    val nf = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    // Android añade símbolo COP, normalizamos al formato $ 1.000 -> $1.000
    return nf.format(value).replace("COP", "").trim()
}

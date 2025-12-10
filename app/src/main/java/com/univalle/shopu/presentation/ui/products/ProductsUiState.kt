package com.univalle.shopu.presentation.ui.products

import com.univalle.shopu.domain.model.Product

data class ProductsUiState(
    val search: String = "",
    val products: List<Product> = emptyList(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val toDelete: Set<String> = emptySet(),
    val confirmId: String? = null,
    val message: String? = null
)

package com.univalle.shopu.presentation.ui.products

import com.univalle.shopu.domain.model.Product

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val search: String = "",
    val toDelete: Set<String> = emptySet(),
    val confirmId: String? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null
)

sealed class ProductsEvent {
    data class OnSearchChange(val value: String) : ProductsEvent()
    data class OnToggleDelete(val productId: String) : ProductsEvent()
    data class OnConfirmId(val id: String?) : ProductsEvent()
    object SaveChanges : ProductsEvent()
    object ClearMessage : ProductsEvent()
}

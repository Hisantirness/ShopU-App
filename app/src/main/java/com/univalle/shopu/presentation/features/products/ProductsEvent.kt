package com.univalle.shopu.presentation.features.products

sealed interface ProductsEvent {
    data class OnSearchChange(val value: String): ProductsEvent
    data class OnToggleDelete(val productId: String): ProductsEvent
    data class OnConfirmId(val id: String?): ProductsEvent
    data object SaveChanges: ProductsEvent
    data object ClearMessage: ProductsEvent
}

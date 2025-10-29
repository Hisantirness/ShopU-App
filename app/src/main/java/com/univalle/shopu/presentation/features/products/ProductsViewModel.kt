package com.univalle.shopu.presentation.features.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.shopu.domain.repository.ProductsRepository
import com.univalle.shopu.data.repository.impl.FirebaseProductsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductsViewModel(
    private val repository: ProductsRepository = FirebaseProductsRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(ProductsUiState())
    val state: StateFlow<ProductsUiState> = _state

    init {
        observe()
    }

    fun onEvent(event: ProductsEvent) {
        when (event) {
            is ProductsEvent.OnSearchChange -> _state.update { it.copy(search = event.value) }
            is ProductsEvent.OnToggleDelete -> _state.update {
                val set = it.toDelete.toMutableSet()
                if (set.contains(event.productId)) set.remove(event.productId) else set.add(event.productId)
                it.copy(toDelete = set)
            }
            is ProductsEvent.OnConfirmId -> _state.update { it.copy(confirmId = event.id) }
            ProductsEvent.SaveChanges -> saveChanges()
            ProductsEvent.ClearMessage -> _state.update { it.copy(message = null) }
        }
    }

    private fun observe() {
        _state.update { it.copy(loading = true) }
        repository.observeProducts { products ->
            _state.update { it.copy(products = products, loading = false) }
        }
    }

    private fun saveChanges() {
        val current = _state.value
        val toDelete = current.toDelete.toList()
        if (toDelete.isEmpty()) {
            _state.update { it.copy(message = "No hay cambios") }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            var failures = 0
            toDelete.forEach { id ->
                repository.deleteProduct(id) { failures += 1 }
            }
            _state.update {
                it.copy(
                    saving = false,
                    message = if (failures == 0) "Cambios guardados" else "Algunos cambios fallaron",
                    toDelete = emptySet(),
                    confirmId = null
                )
            }
        }
    }
}

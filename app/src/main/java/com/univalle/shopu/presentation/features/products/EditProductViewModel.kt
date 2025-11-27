package com.univalle.shopu.presentation.features.products

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.shopu.data.repository.impl.FirebaseProductsRepository
import com.univalle.shopu.domain.repository.ProductsRepository
import com.univalle.shopu.domain.model.Product
import kotlinx.coroutines.launch

class EditProductViewModel(
    private val repository: ProductsRepository = FirebaseProductsRepository()
) : ViewModel() {

    data class UiState(
        val productId: String = "",
        val loaded: Boolean = false,
        val name: String = "",
        val price: String = "",
        val quantity: String = "",
        val category: String = "Café",
        val imageUrl: String? = null,
        val imageUri: Uri? = null,
        val loading: Boolean = false,
        val saving: Boolean = false,
        val error: String? = null,
        val success: Boolean = false
    )

    private val _state = androidx.compose.runtime.mutableStateOf(UiState())
    val state: androidx.compose.runtime.State<UiState> = _state

    fun init(productId: String) {
        if (_state.value.loaded && _state.value.productId == productId) return
        _state.value = _state.value.copy(productId = productId, loading = true)
        viewModelScope.launch {
            try {
                val p: Product? = repository.getProduct(productId)
                if (p != null) {
                    _state.value = _state.value.copy(
                        loaded = true,
                        loading = false,
                        name = p.name,
                        price = p.price.toString(),
                        quantity = p.quantity.toString(),
                        category = p.category,
                        imageUrl = p.imageUrl
                    )
                } else {
                    _state.value = _state.value.copy(loading = false, error = "Producto no encontrado")
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.localizedMessage ?: "Error cargando producto")
            }
        }
    }

    fun updateName(v: String) { _state.value = _state.value.copy(name = v) }
    fun updatePrice(v: String) { _state.value = _state.value.copy(price = v) }
    fun updateQuantity(v: String) { _state.value = _state.value.copy(quantity = v) }
    fun updateCategory(v: String) { _state.value = _state.value.copy(category = v) }
    fun updateImage(uri: Uri?) { _state.value = _state.value.copy(imageUri = uri) }
    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun save() {
        val s = _state.value
        var error: String? = null
        val priceVal = s.price.replace(Regex("[^0-9.,]"), "").replace(",", ".").toDoubleOrNull() ?: run { error = "Precio inválido"; null }
        val qtyVal = s.quantity.toIntOrNull() ?: run { error = "Cantidad inválida"; null }
        if (s.name.isBlank() && error == null) error = "Nombre requerido"
        if (error != null) { _state.value = s.copy(error = error); return }

        _state.value = s.copy(saving = true, error = null)
        viewModelScope.launch {
            try {
                var finalUrl: String? = s.imageUrl
                val uri = s.imageUri
                if (uri != null) {
                    val mime = "image/jpeg" // simplificado; la View puede resolverlo si se desea
                    finalUrl = repository.uploadImage(uri, mime)
                }
                repository.updateProduct(
                    productId = s.productId,
                    name = s.name.trim(),
                    price = priceVal!!,
                    quantity = qtyVal!!,
                    category = s.category,
                    imageUrl = finalUrl
                )
                _state.value = _state.value.copy(saving = false, success = true)
            } catch (e: Exception) {
                _state.value = _state.value.copy(saving = false, error = e.localizedMessage ?: "Error guardando producto")
            }
        }
    }
}

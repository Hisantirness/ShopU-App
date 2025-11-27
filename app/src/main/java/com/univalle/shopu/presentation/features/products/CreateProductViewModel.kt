package com.univalle.shopu.presentation.features.products

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.shopu.data.repository.impl.FirebaseProductsRepository
import com.univalle.shopu.domain.repository.ProductsRepository
import kotlinx.coroutines.launch

class CreateProductViewModel(
    private val repository: ProductsRepository = FirebaseProductsRepository()
) : ViewModel() {

    data class UiState(
        val name: String = "",
        val price: String = "",
        val quantity: String = "",
        val category: String = "Café",
        val imageUri: Uri? = null,
        val loading: Boolean = false,
        val error: String? = null,
        val success: Boolean = false,
        val createdImageUrl: String? = null
    )

    private val _state = androidx.compose.runtime.mutableStateOf(UiState())
    val state: androidx.compose.runtime.State<UiState> = _state

    fun updateName(v: String) { _state.value = _state.value.copy(name = v) }
    fun updatePrice(v: String) { _state.value = _state.value.copy(price = v) }
    fun updateQuantity(v: String) { _state.value = _state.value.copy(quantity = v) }
    fun updateCategory(v: String) { _state.value = _state.value.copy(category = v) }
    fun updateImage(uri: Uri?) { _state.value = _state.value.copy(imageUri = uri) }
    fun clearError() { _state.value = _state.value.copy(error = null) }
    fun clearSuccess() { _state.value = _state.value.copy(success = false, createdImageUrl = null) }

    fun createProduct() {
        val s = _state.value
        var error: String? = null
        val priceVal = s.price.replace("$", "").replace(".", "").replace(",", ".").toDoubleOrNull() ?: run { error = "Precio inválido"; null }
        val qtyVal = s.quantity.toIntOrNull() ?: run { error = "Cantidad inválida"; null }
        if (s.name.isBlank() && error == null) error = "Nombre requerido"
        if (error != null) { _state.value = s.copy(error = error); return }

        _state.value = s.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                var urlStr: String? = null
                val uri = s.imageUri
                if (uri != null) {
                    val mime = guessMime(uri) ?: "image/jpeg"
                    urlStr = repository.uploadImage(uri, mime)
                }
                repository.createProduct(
                    name = s.name.trim(),
                    price = priceVal!!,
                    quantity = qtyVal!!,
                    category = s.category,
                    imageUrl = urlStr
                )
                _state.value = _state.value.copy(loading = false, success = true, createdImageUrl = urlStr)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.localizedMessage ?: "Error creando producto")
            }
        }
    }

    private fun guessMime(uri: Uri): String? {
        // El caller (View) puede resolver mejor el mime; por simplicidad retornamos null aquí
        return null
    }
}

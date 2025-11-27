package com.univalle.shopu.presentation.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.shopu.domain.repository.OrdersRepository
import com.univalle.shopu.data.repository.impl.FirebaseOrdersRepository
import com.univalle.shopu.domain.model.OrderStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: OrdersRepository = FirebaseOrdersRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(OrdersUiState())
    val state: StateFlow<OrdersUiState> = _state

    init {
        observeOrders()
    }

    fun onEvent(event: OrdersEvent) {
        when (event) {
            is OrdersEvent.OnSearchChange -> _state.update { it.copy(search = event.value) }
            is OrdersEvent.OnTabChange -> _state.update { it.copy(selectedTab = event.index) }
            is OrdersEvent.OnOrderStatusChange -> _state.update {
                it.copy(localChanges = it.localChanges.toMutableMap().apply {
                    this[event.orderId] = event.newStatus
                })
            }
            OrdersEvent.SaveChanges -> saveChanges()
            OrdersEvent.ClearMessage -> _state.update { it.copy(message = null) }
        }
    }

    private fun observeOrders() {
        _state.update { it.copy(loading = true) }
        repository.observeOrders { orders ->
            _state.update { it.copy(orders = orders, loading = false) }
        }
    }

    private fun saveChanges() {
        val current = _state.value
        if (current.saving) return
        val toUpdate = current.orders.filter { ord ->
            val new = current.localChanges[ord.id]
            new != null && new != ord.status
        }
        if (toUpdate.isEmpty()) {
            _state.update { it.copy(message = "No hay cambios") }
            return
        }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            var failures = 0
            toUpdate.forEach { ord ->
                val newStatus = current.localChanges[ord.id]!!
                val result = repository.updateOrderStatus(ord.id, newStatus)
                if (result.isFailure) failures += 1
            }
            _state.update {
                it.copy(
                    saving = false,
                    message = if (failures == 0) "Cambios guardados" else "Algunos cambios fallaron",
                    localChanges = if (failures == 0) emptyMap() else it.localChanges // Keep changes if failed
                )
            }
        }
    }

    fun currentStatus(): String = OrderStatus.ALL_STATUSES.getOrElse(_state.value.selectedTab) { OrderStatus.PENDING }
    fun statuses(): List<String> = OrderStatus.ALL_STATUSES
}

package com.univalle.shopu.presentation.ui.workers

data class WorkersUiState(
    val workers: List<String> = emptyList(),
    val search: String = "",
    val emailInput: String = "",
    val toAdd: Set<String> = emptySet(),
    val toRemove: Set<String> = emptySet(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val message: String? = null
)

sealed class WorkersEvent {
    data class OnSearchChange(val value: String) : WorkersEvent()
    data class OnEmailInputChange(val value: String) : WorkersEvent()
    data class OnAddPending(val email: String) : WorkersEvent()
    data class OnRemovePending(val email: String) : WorkersEvent()
    data class OnToggleRemoveExisting(val email: String) : WorkersEvent()
    object SaveChanges : WorkersEvent()
    object ClearMessage : WorkersEvent()
}

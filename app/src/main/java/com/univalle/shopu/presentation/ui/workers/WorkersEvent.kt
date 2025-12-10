package com.univalle.shopu.presentation.ui.workers

sealed interface WorkersEvent {
    data class OnSearchChange(val value: String): WorkersEvent
    data class OnEmailInputChange(val value: String): WorkersEvent
    data class OnAddPending(val email: String): WorkersEvent
    data class OnRemovePending(val email: String): WorkersEvent
    data class OnToggleRemoveExisting(val email: String): WorkersEvent
    data object SaveChanges: WorkersEvent
    data object ClearMessage: WorkersEvent
}

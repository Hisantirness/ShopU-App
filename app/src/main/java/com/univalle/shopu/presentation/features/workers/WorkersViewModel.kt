package com.univalle.shopu.presentation.features.workers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.shopu.data.repository.impl.FirebaseWorkersRepository
import com.univalle.shopu.domain.repository.Worker
import com.univalle.shopu.domain.repository.WorkersRepository
import com.univalle.shopu.presentation.util.isInstitutionalEmail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WorkersViewModel(
    private val repository: WorkersRepository = FirebaseWorkersRepository()
) : ViewModel() {
    private val _state = MutableStateFlow(WorkersUiState())
    val state: StateFlow<WorkersUiState> = _state

    init { observe() }

    fun onEvent(event: WorkersEvent) {
        when (event) {
            is WorkersEvent.OnSearchChange -> _state.update { it.copy(search = event.value) }
            is WorkersEvent.OnEmailInputChange -> _state.update { it.copy(emailInput = event.value) }
            is WorkersEvent.OnAddPending -> addPending(event.email)
            is WorkersEvent.OnRemovePending -> _state.update { it.copy(toAdd = it.toAdd - event.email) }
            is WorkersEvent.OnToggleRemoveExisting -> toggleRemoveExisting(event.email)
            WorkersEvent.SaveChanges -> saveChanges()
            WorkersEvent.ClearMessage -> _state.update { it.copy(message = null) }
        }
    }

    private fun observe() {
        _state.update { it.copy(loading = true) }
        repository.observeWorkers { list: List<Worker> ->
            _state.update { it.copy(workers = list.map { w -> w.email }, loading = false) }
        }
    }

    private fun addPending(rawEmail: String) {
        val mail = rawEmail.trim()
        if (mail.isBlank()) { _state.update { it.copy(message = "Ingresa un correo") }; return }
        if (!isInstitutionalEmail(mail)) { _state.update { it.copy(message = "Correo no institucional") }; return }
        val st = _state.value
        if (st.workers.any { it.equals(mail, true) } || st.toAdd.contains(mail)) {
            _state.update { it.copy(message = "Ya es trabajador") }
            return
        }
        _state.update { it.copy(toAdd = it.toAdd + mail, emailInput = "", message = "Añadido a pendientes") }
    }

    private fun toggleRemoveExisting(email: String) {
        val set = _state.value.toRemove.toMutableSet()
        if (set.contains(email)) set.remove(email) else set.add(email)
        _state.update { it.copy(toRemove = set) }
    }

    private fun saveChanges() {
        val st = _state.value
        if (st.toAdd.isEmpty() && st.toRemove.isEmpty()) { _state.update { it.copy(message = "No hay cambios") }; return }
        _state.update { it.copy(saving = true) }
        viewModelScope.launch {
            var failures = 0
            val now = System.currentTimeMillis()
            st.toAdd.forEach { mail -> repository.addWorker(mail, now) { failures += 1 } }
            st.toRemove.forEach { mail -> repository.removeWorker(mail) { failures += 1 } }
            _state.update {
                it.copy(
                    saving = false,
                    toAdd = emptySet(),
                    toRemove = emptySet(),
                    message = if (failures == 0) "Cambios guardados" else "Algunos cambios fallaron"
                )
            }
        }
    }
}

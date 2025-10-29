package com.univalle.shopu.presentation.features.workers

data class WorkersUiState(
    val search: String = "",
    val emailInput: String = "",
    val workers: List<String> = emptyList(),
    val toAdd: Set<String> = emptySet(),
    val toRemove: Set<String> = emptySet(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val message: String? = null
)

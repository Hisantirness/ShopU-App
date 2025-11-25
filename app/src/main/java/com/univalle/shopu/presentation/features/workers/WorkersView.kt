package com.univalle.shopu.presentation.features.workers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.univalle.shopu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkersView(onBack: () -> Unit, vm: WorkersViewModel = viewModel()) {
    val cs = MaterialTheme.colorScheme
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(stringResource(R.string.manage_workers_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = cs.surface)
            )
        },
        containerColor = cs.background
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            // Search
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.search,
                    onValueChange = { vm.onEvent(WorkersEvent.OnSearchChange(it)) },
                    placeholder = { Text(stringResource(R.string.search_worker_placeholder)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                        cursorColor = cs.primary,
                        focusedLabelColor = cs.primary
                    )
                )
            }
            // Add input
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = state.emailInput,
                    onValueChange = { vm.onEvent(WorkersEvent.OnEmailInputChange(it)) },
                    placeholder = { Text(stringResource(R.string.institutional_email_placeholder)) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cs.primary,
                        unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                        cursorColor = cs.primary,
                        focusedLabelColor = cs.primary
                    )
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = { vm.onEvent(WorkersEvent.OnAddPending(state.emailInput)) }, colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)) {
                    Text(stringResource(R.string.add_action))
                }
            }

            Spacer(Modifier.height(8.dp))

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cs.primary) }
            } else {
                val filtered = state.workers.filter { w ->
                    state.search.isBlank() || w.contains(state.search, true)
                }
                LazyColumn(Modifier.weight(1f).padding(16.dp)) {
                    // Pendientes por añadir
                    items(state.toAdd.toList()) { mail ->
                        WorkerRowVM(email = mail, pendingAdd = true, onRemovePending = { vm.onEvent(WorkersEvent.OnRemovePending(mail)) }, onToggleRemove = null, markedForRemoval = false)
                        Spacer(Modifier.height(8.dp))
                    }
                    // Existentes
                    items(filtered, key = { it }) { email ->
                        WorkerRowVM(
                            email = email,
                            pendingAdd = false,
                            onRemovePending = null,
                            onToggleRemove = { vm.onEvent(WorkersEvent.OnToggleRemoveExisting(email)) },
                            markedForRemoval = state.toRemove.contains(email)
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                state.message?.let {
                    Text(it, color = cs.onBackground.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = { vm.onEvent(WorkersEvent.SaveChanges) },
                    enabled = !state.saving,
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                ) { if (state.saving) CircularProgressIndicator(color = cs.onPrimary, strokeWidth = 2.dp) else Text(stringResource(R.string.save_changes)) }
            }
        }
    }
}

@Composable
private fun WorkerRowVM(
    email: String,
    pendingAdd: Boolean,
    onRemovePending: (() -> Unit)?,
    onToggleRemove: (() -> Unit)?,
    markedForRemoval: Boolean,
) {
    val cs = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(email, color = cs.onSurface, modifier = Modifier.weight(1f))
            if (pendingAdd) {
                OutlinedButton(onClick = { onRemovePending?.invoke() }) { Text(stringResource(R.string.remove_action)) }
            } else {
                val tint = if (markedForRemoval) cs.primary else cs.onSurface
                IconButton(onClick = { onToggleRemove?.invoke() }) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = tint) }
            }
        }
    }
}

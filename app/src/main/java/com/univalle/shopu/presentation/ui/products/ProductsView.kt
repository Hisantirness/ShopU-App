package com.univalle.shopu.presentation.ui.products

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.univalle.shopu.R
import com.univalle.shopu.domain.model.Product
import com.univalle.shopu.presentation.viewmodel.ProductsViewModel
import com.univalle.shopu.presentation.util.formatCurrencyCOP
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsView(
    onBack: () -> Unit,
    onCreateNew: () -> Unit,
    onEditProduct: (String) -> Unit,
    vm: ProductsViewModel = viewModel()
) {
    val cs = MaterialTheme.colorScheme
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(stringResource(R.string.manage_products_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = cs.surface)
            )
        },
        containerColor = cs.background
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            // Search
            OutlinedTextField(
                value = state.search,
                onValueChange = { vm.onEvent(ProductsEvent.OnSearchChange(it)) },
                placeholder = { Text(stringResource(R.string.search_product_placeholder)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                    cursorColor = cs.primary,
                    focusedLabelColor = cs.primary
                )
            )

            // Crear nuevo producto
            OutlinedButton(
                onClick = onCreateNew,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.create_new_product))
            }

            Spacer(Modifier.height(8.dp))

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = cs.primary) }
            } else {
                val filtered = state.products.filter { p ->
                    state.search.isBlank() || p.name.contains(state.search, true)
                }
                LazyColumn(Modifier.weight(1f).padding(16.dp)) {
                    items(filtered, key = { it.id }) { p ->
                        ProductRowVM(
                            p = p,
                            selectedForDelete = state.toDelete.contains(p.id),
                            onEdit = { onEditProduct(p.id) },
                            onToggleDelete = { vm.onEvent(ProductsEvent.OnToggleDelete(p.id)) }
                        )
                        Spacer(Modifier.height(8.dp))
                    }
                }

                state.message?.let {
                    Text(it, color = cs.onBackground.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (state.toDelete.isEmpty()) vm.onEvent(ProductsEvent.ClearMessage) else vm.onEvent(ProductsEvent.OnConfirmId("__bulk__"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                ) { Text(stringResource(R.string.save_changes)) }
            }
        }
    }

    // Confirmación (bulk delete)
    if (state.confirmId != null) {
        val bulk = state.confirmId == "__bulk__"
        AlertDialog(
            onDismissRequest = { vm.onEvent(ProductsEvent.OnConfirmId(null)) },
            title = { Text(if (bulk) stringResource(R.string.confirm_changes_title) else stringResource(R.string.delete_product_title)) },
            text = { Text(if (bulk) stringResource(R.string.bulk_delete_message, state.toDelete.size) else stringResource(R.string.delete_product_message)) },
            confirmButton = {
                TextButton(onClick = {
                    if (bulk) vm.onEvent(ProductsEvent.SaveChanges)
                    vm.onEvent(ProductsEvent.OnConfirmId(null))
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = { TextButton(onClick = { vm.onEvent(ProductsEvent.OnConfirmId(null)) }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ProductRowVM(
    p: Product,
    selectedForDelete: Boolean,
    onEdit: () -> Unit,
    onToggleDelete: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val model = if (!p.imageUrl.isNullOrBlank() && (p.imageUrl.startsWith("http://") || p.imageUrl.startsWith("https://"))) p.imageUrl else null
            AsyncImage(
                model = model,
                contentDescription = p.name,
                modifier = Modifier.size(56.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.shopulogofinal),
                error = painterResource(id = R.drawable.shopulogofinal)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, fontWeight = FontWeight.Bold, color = cs.onSurface)
                Text(stringResource(R.string.price_label), color = cs.onSurface.copy(alpha = 0.7f))
                Text(formatCurrencyCOP(p.price), color = cs.onSurface)
                Text(stringResource(R.string.quantity_label, p.quantity), color = cs.onSurface)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit), tint = cs.primary) }
                IconButton(onClick = onToggleDelete) { Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = if (selectedForDelete) cs.primary else cs.onSurface) }
            }
        }
    }
}

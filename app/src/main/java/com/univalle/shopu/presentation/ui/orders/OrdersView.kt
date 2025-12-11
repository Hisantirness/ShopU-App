package com.univalle.shopu.presentation.ui.orders

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.univalle.shopu.R
import com.univalle.shopu.domain.model.Order
import com.univalle.shopu.presentation.viewmodel.OrdersViewModel
import com.univalle.shopu.presentation.util.formatCurrencyCOP
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersView(onBack: () -> Unit, onOrderClick: (String) -> Unit, vm: OrdersViewModel = viewModel()) {
    val cs = MaterialTheme.colorScheme
    val state by vm.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(stringResource(R.string.manage_orders_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_button)) } },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = cs.surface)
            )
        },
        containerColor = cs.background
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            // Search
            OutlinedTextField(
                value = state.search,
                onValueChange = { vm.onEvent(OrdersEvent.OnSearchChange(it)) },
                placeholder = { Text(stringResource(R.string.search_order_placeholder)) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                    cursorColor = cs.primary,
                    focusedLabelColor = cs.primary
                ),
                shape = MaterialTheme.shapes.large
            )

            // Tabs estados - Horizontally scrollable
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                containerColor = cs.surface,
                edgePadding = 16.dp
            ) {
                vm.statuses().forEachIndexed { idx, status ->
                    val title = getStatusTitle(status)
                    Tab(
                        selected = state.selectedTab == idx,
                        onClick = { vm.onEvent(OrdersEvent.OnTabChange(idx)) },
                        selectedContentColor = cs.primary,
                        unselectedContentColor = cs.onSurface.copy(alpha = 0.7f),
                        text = { 
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (state.selectedTab == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (state.loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = cs.primary)
                }
            } else {
                val currentStatus = vm.currentStatus()
                val filtered = state.orders.filter { order ->
                    val effectiveStatus = state.localChanges[order.id] ?: order.status
                    effectiveStatus == currentStatus
                }
                    .filter { o ->
                        state.search.isBlank() || o.customer.contains(state.search, true) || o.id.contains(state.search, true)
                    }

                LazyColumn(Modifier.weight(1f).padding(16.dp)) {
                    if (filtered.isEmpty()) {
                        item { Text(stringResource(R.string.no_orders), color = cs.onBackground.copy(alpha = 0.6f)) }
                    } else {
                        items(filtered, key = { it.id }) { order ->
                            OrderRowVM(
                                order = order,
                                current = state.localChanges[order.id] ?: order.status,
                                statuses = vm.statuses(),
                                onChange = { new -> vm.onEvent(OrdersEvent.OnOrderStatusChange(order.id, new)) },
                                onClick = { onOrderClick(order.id) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }

                state.message?.let {
                    Text(it, color = cs.onBackground.copy(alpha = 0.7f), modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = { vm.onEvent(OrdersEvent.SaveChanges) },
                    enabled = !state.saving,
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(52.dp)
                ) {
                    if (state.saving) CircularProgressIndicator(color = cs.onPrimary, strokeWidth = 2.dp) else Text(stringResource(R.string.save_changes))
                }
            }
        }
    }
}

@Composable
private fun OrderRowVM(order: Order, current: String, statuses: List<String>, onChange: (String) -> Unit, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        onClick = onClick
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header with ID and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Image
                Image(
                    painter = painterResource(id = R.drawable.shopulogofinal), // Placeholder for product image
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(MaterialTheme.shapes.small)
                        .border(1.dp, Color.LightGray, MaterialTheme.shapes.small),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(Modifier.width(12.dp))
                
                // Details
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "#" + order.id,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                    )
                    Spacer(Modifier.height(4.dp))
                    // Show first item name or "Pedido"
                    val firstItemName = (order.items.firstOrNull()?.get("name") as? String) ?: "Pedido"
                    Text(
                        text = firstItemName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatTime(order.createdAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                
                // Price
                Text(
                    text = formatCurrencyCOP(order.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Button and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                
                Box {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F), // Red color
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Actualizar estado", style = MaterialTheme.typography.labelLarge)
                    }
                    
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        statuses.forEach { st ->
                            val title = getStatusTitle(st)
                            DropdownMenuItem(
                                text = { Text(title) },
                                onClick = {
                                    onChange(st)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun formatTime(ts: Long): String {
    val date = java.util.Date(ts)
    val fmt = java.text.SimpleDateFormat("h:mm a", java.util.Locale("es", "CO"))
    return fmt.format(date)
}

@Composable
private fun getStatusTitle(status: String): String {
    return when (status) {
        "pendiente" -> stringResource(R.string.status_pending)
        "en_proceso" -> stringResource(R.string.status_in_progress)
        "listo" -> stringResource(R.string.status_ready)
        "entregado" -> stringResource(R.string.status_delivered)
        "cancelado" -> "Cancelado"
        else -> status
    }
}

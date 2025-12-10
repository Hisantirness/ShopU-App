package com.univalle.shopu.presentation.ui.shop

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.R
import com.univalle.shopu.presentation.util.formatCurrencyCOP
import java.text.SimpleDateFormat
import java.util.*

data class Order(
    val id: String = "",
    val customer: String = "",
    val items: List<Map<String, Any>> = emptyList(),
    val total: Double = 0.0,
    val status: String = "pendiente",
    val createdAt: Long = 0L
)

@Composable
fun OrderHistoryScreen(
    auth: FirebaseAuth,
    onBack: () -> Unit,
    onOrderClick: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = Firebase.firestore
    val userEmail = auth.currentUser?.email ?: ""

    var search by remember { mutableStateOf("") }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    // Cargar pedidos del usuario
    LaunchedEffect(Unit) {
        db.collection("orders")
            .whereEqualTo("customer", userEmail)
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    loading = false
                    return@addSnapshotListener
                }
                loading = false
                orders = snap?.documents?.mapNotNull { doc ->
                    try {
                        Order(
                            id = doc.getString("id") ?: "",
                            customer = doc.getString("customer") ?: "",
                            items = doc.get("items") as? List<Map<String, Any>> ?: emptyList(),
                            total = doc.getDouble("total") ?: 0.0,
                            status = doc.getString("status") ?: "pendiente",
                            createdAt = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) {
                        null
                    }
                }?.sortedByDescending { it.createdAt } ?: emptyList()
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back_button),
                    tint = cs.onBackground
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.order_history),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground
            )
        }

        Spacer(Modifier.height(16.dp))

        // Búsqueda
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text(stringResource(R.string.search_order_hint)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f)
            )
        )

        Spacer(Modifier.height(12.dp))

        Spacer(Modifier.height(16.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = cs.primary)
            }
        } else {
            val filtered = orders.filter { order ->
                search.isBlank() ||
                        order.id.contains(search, ignoreCase = true) ||
                        order.items.any { item ->
                            (item["name"] as? String)?.contains(search, ignoreCase = true) == true
                        }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = stringResource(R.string.no_orders_found),
                        color = cs.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered) { order ->
                        OrderCard(
                            order = order,
                            onClick = { onOrderClick(order.id) },
                            onCancel = {
                                db.collection("orders").document(order.id)
                                    .update("status", "cancelado")
                                    .addOnSuccessListener {
                                        android.widget.Toast.makeText(context, "Pedido cancelado", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        android.util.Log.e("OrderHistory", "Error cancelling order", e)
                                        android.widget.Toast.makeText(context, "Error al cancelar: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, onClick: () -> Unit, onCancel: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val canCancel = order.status.lowercase() == "pendiente" || order.status.lowercase() == "en progreso"
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirmar cancelación") },
            text = { Text("¿Estás seguro de que deseas cancelar este pedido?") },
            confirmButton = {
                Button(
                    onClick = {
                        onCancel()
                        showDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cs.error)
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.order_id, order.id),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = cs.onSurface
                )
                StatusChip(status = order.status)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.order_date, dateFormat.format(Date(order.createdAt))),
                style = MaterialTheme.typography.bodySmall,
                color = cs.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(12.dp))

            // Mostrar items
            order.items.take(2).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${item["quantity"]} x ${item["name"]}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = formatCurrencyCOP((item["price"] as? Number)?.toDouble() ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface
                    )
                }
            }

            if (order.items.size > 2) {
                Text(
                    text = "+ ${order.items.size - 2} más",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(12.dp))

            Divider()

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.cart_total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    )
                    Text(
                        text = formatCurrencyCOP(order.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = cs.primary
                    )
                }
                
                if (canCancel) {
                    OutlinedButton(
                        onClick = { showDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.error)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val cs = MaterialTheme.colorScheme
    val (text, color) = when (status.lowercase()) {
        "pendiente" -> stringResource(R.string.status_pending) to cs.error
        "en progreso" -> stringResource(R.string.status_in_progress) to cs.tertiary
        "listo" -> stringResource(R.string.status_ready) to cs.primary
        "entregado" -> stringResource(R.string.status_delivered) to Color(0xFF4CAF50)
        "cancelado" -> "Cancelado" to cs.error
        else -> status to cs.onSurface
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

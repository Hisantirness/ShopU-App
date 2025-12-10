package com.univalle.shopu.presentation.ui.orders

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.R
import com.univalle.shopu.presentation.util.formatCurrencyCOP
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(
    orderId: String,
    isAdmin: Boolean,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val db = Firebase.firestore
    var order by remember { mutableStateOf<Order?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showStatusDialog by remember { mutableStateOf(false) }

    LaunchedEffect(orderId) {
        db.collection("orders").document(orderId).addSnapshotListener { snap, _ ->
            if (snap != null && snap.exists()) {
                order = try {
                    Order(
                        id = snap.getString("id") ?: "",
                        customer = snap.getString("customer") ?: "",
                        items = snap.get("items") as? List<Map<String, Any>> ?: emptyList(),
                        total = snap.getDouble("total") ?: 0.0,
                        status = snap.getString("status") ?: "pendiente",
                        createdAt = snap.getLong("createdAt") ?: 0L,
                        paymentInfo = snap.get("paymentInfo") as? Map<String, String> ?: emptyMap()
                    )
                } catch (e: Exception) { null }
            }
            loading = false
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Informacion de pedido", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Volver") } },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = cs.surface)
            )
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            if (loading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                val o = order
                if (o == null) {
                    Text("Pedido no encontrado", Modifier.align(Alignment.Center))
                } else {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Info
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Informacion de pedido", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("#${o.id}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            }
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                                color = Color.White
                            ) {
                                Text(
                                    text = getStatusText(o.status),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Big Icon
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .border(2.dp, Color(0xFFD32F2F), CircleShape)
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFD32F2F), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.shopulogofinal), // Placeholder icon
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        Divider(color = Color.Black)
                        
                        // Totals
                        val subtotal = o.total * 0.9 // Example tax calculation
                        val tax = o.total * 0.1

                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
                            Text(formatCurrencyCOP(subtotal), style = MaterialTheme.typography.bodyLarge)
                        }
                        Divider(color = Color.Black)
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Impuestos", style = MaterialTheme.typography.bodyLarge)
                            Text(formatCurrencyCOP(tax), style = MaterialTheme.typography.bodyLarge)
                        }
                        Divider(color = Color.Black, thickness = 2.dp)
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(formatCurrencyCOP(o.total), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Color.Black)

                        Spacer(Modifier.height(16.dp))

                        // Payment Method
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Metodo de pago", style = MaterialTheme.typography.bodyLarge)
                            Text("Nequi", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color(0xFF2C0E37))
                        }

                        Spacer(Modifier.height(24.dp))

                        // Contact Info
                        Text("Contacto:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        
                        ContactRow("Nombre", o.paymentInfo["nombre"] ?: "")
                        Spacer(Modifier.height(8.dp))
                        ContactRow("Telefono", o.paymentInfo["telefono"] ?: "")
                        Spacer(Modifier.height(8.dp))
                        ContactRow("Referencia", o.paymentInfo["referencia"] ?: "")

                        Spacer(Modifier.height(32.dp))

                        // Buttons
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (isAdmin) {
                                Button(
                                    onClick = { showStatusDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Actualizar estado")
                                }
                                Spacer(Modifier.width(16.dp))
                            }
                            
                            OutlinedButton(
                                onClick = onBack,
                                shape = MaterialTheme.shapes.small,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Black),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Volver", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showStatusDialog) {
        val statuses = listOf("pendiente", "en_proceso", "listo", "entregado", "cancelado")
        val repository = remember { com.univalle.shopu.data.repository.impl.FirebaseOrdersRepository() }
        val context = androidx.compose.ui.platform.LocalContext.current
        
        AlertDialog(
            onDismissRequest = { showStatusDialog = false },
            title = { Text("Actualizar estado") },
            text = {
                Column {
                    statuses.forEach { st ->
                        val scope = rememberCoroutineScope()
                        TextButton(
                            onClick = {
                                scope.launch {
                                    val result = repository.updateOrderStatus(orderId, st)
                                    if (result.isFailure) {
                                        android.widget.Toast.makeText(context, "Error al actualizar estado", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                                showStatusDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(getStatusText(st))
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun ContactRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.width(100.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Black)
                .padding(8.dp)
        ) {
            Text(value)
        }
    }
}

@Composable
fun getStatusText(status: String): String {
    return when (status) {
        "pendiente" -> "Pendiente"
        "en_proceso" -> "En progreso"
        "listo" -> "Listo"
        "entregado" -> "Entregado"
        "cancelado" -> "Cancelado"
        else -> status
    }
}

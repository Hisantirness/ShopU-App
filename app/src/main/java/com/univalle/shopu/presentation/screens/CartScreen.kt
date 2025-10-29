package com.univalle.shopu.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.presentation.util.CartStore
import com.univalle.shopu.presentation.util.formatCurrencyCOP

@Composable
fun CartScreen(onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var placing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Carrito de Compras", style = MaterialTheme.typography.titleLarge, color = cs.onBackground)
        Spacer(Modifier.height(12.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(CartStore.items) { item ->
                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp), elevation = CardDefaults.cardElevation(2.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, color = cs.onSurface)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OutlinedButton(onClick = { CartStore.dec(item.id) }) { Text("−") }
                                Spacer(Modifier.width(8.dp))
                                Text("${item.quantity}")
                                Spacer(Modifier.width(8.dp))
                                OutlinedButton(onClick = { CartStore.inc(item.id) }) { Text("+") }
                            }
                        }
                        Text(formatCurrencyCOP(item.subtotal), color = cs.onSurface)
                    }
                }
            }
        }
        Text("Total: " + formatCurrencyCOP(CartStore.total()), color = cs.onBackground, fontWeight = FontWeight.Bold)
        if (message != null) {
            Spacer(Modifier.height(6.dp))
            Text(message!!, color = cs.onBackground.copy(alpha = 0.7f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = cs.surface, contentColor = cs.onSurface)) { Text("Volver") }
            Button(
                onClick = {
                    if (CartStore.items.isEmpty()) { message = "El carrito está vacío"; return@Button }
                    placing = true
                    val auth = Firebase.auth
                    val db = Firebase.firestore
                    val email = auth.currentUser?.email ?: "anon@local"
                    val order = hashMapOf(
                        "customer" to email,
                        "items" to CartStore.items.map { mapOf("id" to it.id, "name" to it.name, "price" to it.price, "quantity" to it.quantity) },
                        "total" to CartStore.total(),
                        "status" to "pendiente",
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("orders").add(order)
                        .addOnSuccessListener {
                            placing = false
                            CartStore.clear()
                            message = "Pedido creado correctamente"
                        }
                        .addOnFailureListener { e ->
                            placing = false
                            message = e.localizedMessage ?: "Error creando pedido"
                        }
                },
                enabled = !placing,
                colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
            ) {
                if (placing) CircularProgressIndicator(color = cs.onPrimary, strokeWidth = 2.dp) else Text("Confirmar pedido")
            }
        }
    }
}

package com.univalle.shopu.presentation.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.R
import com.univalle.shopu.presentation.util.CartStore
import com.univalle.shopu.presentation.util.formatCurrencyCOP

@Composable
fun PaymentScreen(
    auth: FirebaseAuth,
    onPaymentConfirmed: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = Firebase.firestore
    val userEmail = auth.currentUser?.email ?: ""

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var referencia by remember { mutableStateOf("") }
    var processing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Cargar datos del usuario
    LaunchedEffect(Unit) {
        db.collection("users").document(userEmail).get()
            .addOnSuccessListener { doc ->
                val nombres = doc.getString("nombres") ?: ""
                val apellidos = doc.getString("apellidos") ?: ""
                nombre = "$nombres $apellidos"
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
                text = stringResource(R.string.payment_screen),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground
            )
        }

        Spacer(Modifier.height(24.dp))

        // QR Code (placeholder - en producción sería un QR real de Nequi)
        Card(
            modifier = Modifier
                .size(250.dp)
                .align(Alignment.CenterHorizontally),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Aquí iría el QR real de Nequi
                // Por ahora usamos el logo como placeholder
                Image(
                    painter = painterResource(id = R.drawable.shopulogofinal),
                    contentDescription = stringResource(R.string.pay_with_nequi),
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.contact_label),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = cs.onBackground
        )

        Spacer(Modifier.height(12.dp))

        // Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text(stringResource(R.string.names_label)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f)
            )
        )

        Spacer(Modifier.height(12.dp))

        // A pagar
        OutlinedTextField(
            value = formatCurrencyCOP(CartStore.total()),
            onValueChange = {},
            label = { Text(stringResource(R.string.amount_to_pay)) },
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                disabledBorderColor = cs.onSurface.copy(alpha = 0.1f),
                disabledTextColor = cs.onSurface,
                disabledLabelColor = cs.onSurface.copy(alpha = 0.6f)
            )
        )

        Spacer(Modifier.height(12.dp))

        // Referencia
        OutlinedTextField(
            value = referencia,
            onValueChange = { referencia = it },
            label = { Text(stringResource(R.string.reference_label)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f)
            )
        )

        Spacer(Modifier.height(12.dp))

        // Teléfono
        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text(stringResource(R.string.phone_label)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f)
            )
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = error!!,
                color = cs.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.weight(1f))

        // Botón Confirmar y Pagar
        Button(
            onClick = {
                if (CartStore.items.isEmpty()) {
                    error = context.getString(R.string.cart_empty)
                    return@Button
                }
                if (nombre.isBlank()) {
                    error = context.getString(R.string.error_enter_names)
                    return@Button
                }

                processing = true
                error = null

                // Validar stock antes de crear el pedido
                db.runTransaction { transaction ->
                    // 1. Verificar stock de todos los productos
                    CartStore.items.forEach { item ->
                        val productRef = db.collection("products").document(item.id)
                        val snapshot = transaction.get(productRef)
                        val currentStock = (snapshot.get("quantity") as? Number)?.toInt() ?: 0
                        if (currentStock < item.quantity) {
                            throw com.google.firebase.firestore.FirebaseFirestoreException(
                                "Stock insuficiente para ${item.name}. Disponible: $currentStock",
                                com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                            )
                        }
                    }

                    // 2. Obtener nuevo ID de pedido
                    val counterRef = db.collection("counters").document("orders")
                    val snapshot = transaction.get(counterRef)
                    val newId = if (snapshot.exists()) {
                        (snapshot.getLong("lastId") ?: 1000) + 1
                    } else {
                        1001
                    }
                    transaction.set(counterRef, mapOf("lastId" to newId))

                    // 3. Crear el pedido
                    val order = hashMapOf(
                        "id" to newId.toString(),
                        "customer" to userEmail,
                        "items" to CartStore.items.map {
                            mapOf(
                                "id" to it.id,
                                "name" to it.name,
                                "price" to it.price,
                                "quantity" to it.quantity
                            )
                        },
                        "total" to CartStore.total(),
                        "status" to "pendiente",
                        "createdAt" to System.currentTimeMillis(),
                        "paymentInfo" to mapOf(
                            "nombre" to nombre,
                            "telefono" to telefono,
                            "referencia" to referencia
                        )
                    )
                    val orderRef = db.collection("orders").document(newId.toString())
                    transaction.set(orderRef, order)
                }
                    .addOnSuccessListener {
                        processing = false
                        CartStore.clear()
                        android.widget.Toast.makeText(context, "Pedido realizado con éxito", android.widget.Toast.LENGTH_SHORT).show()
                        onPaymentConfirmed()
                    }
                    .addOnFailureListener { e ->
                        processing = false
                        error = e.localizedMessage ?: context.getString(R.string.order_error)
                    }
            },
            enabled = !processing,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = cs.primary,
                contentColor = cs.onPrimary
            )
        ) {
            if (processing) {
                CircularProgressIndicator(
                    color = cs.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(stringResource(R.string.confirm_payment))
            }
        }
    }
}

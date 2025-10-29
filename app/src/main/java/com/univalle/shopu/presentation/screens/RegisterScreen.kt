package com.univalle.shopu.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.R
import com.univalle.shopu.presentation.util.isInstitutionalEmail

@Composable
fun RegisterScreen(
    auth: FirebaseAuth,
    onRegistered: () -> Unit,
    onBack: () -> Unit,
) {
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showVerifyDialog by remember { mutableStateOf(false) }
    var verifyDialogMessage by remember { mutableStateOf("") }

    val db = Firebase.firestore

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.shopulogofinal),
                contentDescription = "Logo ShopU",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Crear cuenta", style = MaterialTheme.typography.titleLarge, color = Color(0xFF060000))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = { Text("Nombres") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text("Apellidos") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text("Código estudiantil") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo institucional") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña (6+)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error ?: "", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    error = null
                    if (nombres.isBlank()) { error = "Ingresa tus nombres"; return@Button }
                    if (apellidos.isBlank()) { error = "Ingresa tus apellidos"; return@Button }
                    if (codigo.isBlank()) { error = "Ingresa tu código"; return@Button }
                    if (email.isBlank()) { error = "Ingresa tu correo"; return@Button }
                    if (!isInstitutionalEmail(email)) { error = "Debe ser correo institucional"; return@Button }
                    if (password.length < 6) { error = "La contraseña debe tener 6+ caracteres"; return@Button }

                    loading = true
                    val trimmedEmail = email.trim()
                    auth.createUserWithEmailAndPassword(trimmedEmail, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val data = hashMapOf(
                                    "nombres" to nombres.trim(),
                                    "apellidos" to apellidos.trim(),
                                    "codigo" to codigo.trim(),
                                    "correo" to trimmedEmail
                                )
                                db.collection("users").document(trimmedEmail)
                                    .set(data)
                                    .addOnSuccessListener {
                                        // Enviar correo de verificación
                                        auth.currentUser?.sendEmailVerification()
                                            ?.addOnCompleteListener { verifyTask ->
                                                loading = false
                                                verifyDialogMessage = if (verifyTask.isSuccessful) {
                                                    "Te enviamos un correo de verificación a $trimmedEmail. Revisa tu bandeja y confirma para poder iniciar sesión."
                                                } else {
                                                    val msg = verifyTask.exception?.localizedMessage
                                                        ?: "Cuenta creada, pero no se pudo enviar el correo de verificación. Intenta reenviar desde Inicio de sesión."
                                                    msg
                                                }
                                                showVerifyDialog = true
                                            }
                                            ?: run {
                                                loading = false
                                                verifyDialogMessage = "No se pudo iniciar el envío de verificación. Intenta luego desde Inicio de sesión."
                                                showVerifyDialog = true
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        loading = false
                                        error = e.localizedMessage ?: "Error guardando datos"
                                    }
                            } else {
                                loading = false
                                error = task.exception?.localizedMessage ?: "Error al crear cuenta"
                            }
                        }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp) else Text("Registrarme")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("Volver")
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (showVerifyDialog) {
                AlertDialog(
                    onDismissRequest = { /* bloquear dismiss fuera del botón */ },
                    title = { Text("Verificación enviada") },
                    text = { Text(verifyDialogMessage) },
                    confirmButton = {
                        Button(onClick = {
                            showVerifyDialog = false
                            onRegistered() // Regresar a Login
                        }) {
                            Text("Ir a Inicio de sesión")
                        }
                    }
                )
            }
        }
    }
}

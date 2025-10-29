package com.univalle.shopu.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.univalle.shopu.R
import com.univalle.shopu.presentation.util.isInstitutionalEmail

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onAdminLogin: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showVerifyDialog by remember { mutableStateOf(false) }
    var verifyDialogMessage by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme

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
                modifier = Modifier
                    .size(120.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Bienvenido a ShopU", color = cs.onBackground, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo institucional") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                    cursorColor = cs.primary,
                    focusedLabelColor = cs.primary
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                    cursorColor = cs.primary,
                    focusedLabelColor = cs.primary
                )
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error ?: "", color = Color.Red)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    error = null
                    if (email.isBlank()) {
                        error = "Ingresa tu correo"
                        return@Button
                    }
                    if (!isInstitutionalEmail(email)) {
                        error = "Debe ser correo institucional"
                        return@Button
                    }
                    if (password.isBlank()) {
                        error = "Ingresa tu contraseña"
                        return@Button
                    }

                    // Admin override
                    if (email.trim().equals("santiago.villa@correounivalle.edu.co", ignoreCase = true)
                        && password == "123456") {
                        onAdminLogin()
                        return@Button
                    }
                    loading = true
                    val trimmedEmail = email.trim()
                    auth.signInWithEmailAndPassword(trimmedEmail, password)
                        .addOnCompleteListener { task ->
                            loading = false
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user?.isEmailVerified == true) {
                                    onLoginSuccess()
                                } else {
                                    verifyDialogMessage = "Tu correo no está verificado. Te enviamos un correo a $trimmedEmail. Revisa tu bandeja y confirma para continuar."
                                    showVerifyDialog = true
                                    // Intentar reenviar automáticamente una vez
                                    user?.sendEmailVerification()
                                }
                            } else {
                                error = task.exception?.localizedMessage ?: "Error al iniciar sesión"
                            }
                        }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp) else Text("Iniciar sesión")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth().height(52.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)) {
                Text("Crear cuenta")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "Acceso solo con correo institucional", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

            if (showVerifyDialog) {
                AlertDialog(
                    onDismissRequest = { /* do nothing */ },
                    title = { Text("Verifica tu correo") },
                    text = { Text(verifyDialogMessage) },
                    confirmButton = {
                        Button(onClick = {
                            showVerifyDialog = false
                            auth.signOut()
                        }) { Text("Entendido") }
                    },
                    dismissButton = {
                        Button(onClick = {
                            auth.currentUser?.sendEmailVerification()
                        }) { Text("Reenviar correo") }
                    }
                )
            }
        }
    }
}

package com.univalle.shopu.presentation.ui.auth

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current

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
                contentDescription = stringResource(R.string.app_logo_desc),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(96.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(R.string.register_title), style = MaterialTheme.typography.titleLarge, color = Color(0xFF060000))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = { Text(stringResource(R.string.names_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text(stringResource(R.string.last_names_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text(stringResource(R.string.student_code_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.password_min_label)) },
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
                    if (nombres.isBlank()) { error = context.getString(R.string.error_enter_names); return@Button }
                    if (apellidos.isBlank()) { error = context.getString(R.string.error_enter_last_names); return@Button }
                    if (codigo.isBlank()) { error = context.getString(R.string.error_enter_code); return@Button }
                    if (email.isBlank()) { error = context.getString(R.string.error_enter_email); return@Button }
                    if (!isInstitutionalEmail(email)) { error = context.getString(R.string.error_institutional_email); return@Button }
                    if (password.length < 6) { error = context.getString(R.string.error_password_min); return@Button }

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
                                                    context.getString(R.string.verify_sent_body, trimmedEmail)
                                                } else {
                                                    val msg = verifyTask.exception?.localizedMessage
                                                        ?: context.getString(R.string.verify_sent_error)
                                                    msg
                                                }
                                                showVerifyDialog = true
                                            }
                                            ?: run {
                                                loading = false
                                                verifyDialogMessage = context.getString(R.string.verify_sent_fail)
                                                showVerifyDialog = true
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        loading = false
                                        error = e.localizedMessage ?: context.getString(R.string.error_save_data)
                                    }
                            } else {
                                loading = false
                                error = task.exception?.localizedMessage ?: context.getString(R.string.error_create_account)
                            }
                        }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp) else Text(stringResource(R.string.register_button))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(stringResource(R.string.back_button))
            }
            Spacer(modifier = Modifier.height(24.dp))

            if (showVerifyDialog) {
                AlertDialog(
                    onDismissRequest = { /* bloquear dismiss fuera del botón */ },
                    title = { Text(stringResource(R.string.verify_sent_title)) },
                    text = { Text(verifyDialogMessage) },
                    confirmButton = {
                        Button(onClick = {
                            showVerifyDialog = false
                            onRegistered() // Regresar a Login
                        }) {
                            Text(stringResource(R.string.go_to_login))
                        }
                    }
                )
            }
        }
    }
}

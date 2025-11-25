package com.univalle.shopu.presentation.features.auth

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
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.univalle.shopu.R
import androidx.compose.ui.res.stringResource
import com.univalle.shopu.presentation.util.isInstitutionalEmail
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onAdminLogin: (Boolean) -> Unit, // Boolean indicates isSuperAdmin
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showVerifyDialog by remember { mutableStateOf(false) }
    var verifyDialogMessage by remember { mutableStateOf("") }
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
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
                contentDescription = stringResource(R.string.app_logo_desc),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(120.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.welcome_message), color = cs.onBackground, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.email_hint)) },
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
                label = { Text(stringResource(R.string.password_hint)) },
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
                        error = context.getString(R.string.error_enter_email)
                        return@Button
                    }
                    if (!isInstitutionalEmail(email)) {
                        error = context.getString(R.string.error_institutional_email)
                        return@Button
                    }
                    if (password.isBlank()) {
                        error = context.getString(R.string.error_enter_password)
                        return@Button
                    }

                    loading = true
                    val trimmedEmail = email.trim()
                    auth.signInWithEmailAndPassword(trimmedEmail, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                if (user?.isEmailVerified == true) {
                                    // Check role from users collection
                                    val emailKey = user.email ?: ""
                                    db.collection("users").document(emailKey).get()
                                        .addOnSuccessListener { userDoc ->
                                            loading = false
                                            val role = userDoc.getString("role") ?: "customer"
                                            when (role) {
                                                "admin" -> onAdminLogin(true) // Super Admin
                                                "worker" -> onAdminLogin(false) // Worker
                                                else -> onLoginSuccess() // Customer
                                            }
                                        }
                                        .addOnFailureListener {
                                            loading = false
                                            onLoginSuccess() // Default to customer on error
                                        }
                                } else {
                                    loading = false
                                    verifyDialogMessage = context.getString(R.string.verify_email_body, trimmedEmail)
                                    showVerifyDialog = true
                                    // Intentar reenviar automáticamente una vez
                                    user?.sendEmailVerification()
                                }
                            } else {
                                loading = false
                                error = task.exception?.localizedMessage ?: context.getString(R.string.error_login_generic)
                            }
                        }
                },
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
            ) {
                if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp) else Text(stringResource(R.string.login_button))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onNavigateToRegister, modifier = Modifier.fillMaxWidth().height(52.dp), colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)) {
                Text(stringResource(R.string.create_account))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = stringResource(R.string.access_note), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))

            if (showVerifyDialog) {
                AlertDialog(
                    onDismissRequest = { /* do nothing */ },
                    title = { Text(stringResource(R.string.verify_email_title)) },
                    text = { Text(verifyDialogMessage) },
                    confirmButton = {
                        Button(onClick = {
                            showVerifyDialog = false
                            auth.signOut()
                        }) { Text(stringResource(R.string.understood)) }
                    },
                    dismissButton = {
                        Button(onClick = {
                            auth.currentUser?.sendEmailVerification()
                        }) { Text(stringResource(R.string.resend_email)) }
                    }
                )
            }
        }
    }
}

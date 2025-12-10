package com.univalle.shopu.presentation.features.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.R
import com.univalle.shopu.presentation.util.isInstitutionalEmail
import com.univalle.shopu.ui.components.ShopUButton
import com.univalle.shopu.ui.components.ShopUTextField

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
    var showRecoverySuccessDialog by remember { mutableStateOf(false) }
    var recoverySuccessMessage by remember { mutableStateOf("") }
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
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                painter = painterResource(id = R.drawable.shopulogofinal),
                contentDescription = stringResource(R.string.app_logo_desc),
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(140.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground
            )
            Spacer(modifier = Modifier.height(32.dp))
            ShopUTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email_hint),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShopUTextField(
                value = password,
                onValueChange = { password = it },
                label = stringResource(R.string.password_hint),
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            ShopUButton(
                text = stringResource(R.string.login_button),
                onClick = {
                    error = null
                    if (email.isBlank()) {
                        error = context.getString(R.string.error_enter_email)
                        return@ShopUButton
                    }
                    if (!isInstitutionalEmail(email)) {
                        error = context.getString(R.string.error_institutional_email)
                        return@ShopUButton
                    }
                    if (password.isBlank()) {
                        error = context.getString(R.string.error_enter_password)
                        return@ShopUButton
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
                modifier = Modifier.fillMaxWidth(),
                content = if (loading) {
                    {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else null
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShopUButton(
                text = stringResource(R.string.create_account),
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = {
                if (email.isBlank()) {
                    error = context.getString(R.string.recover_password_enter_email)
                    return@TextButton
                }
                loading = true
                auth.sendPasswordResetEmail(email.trim())
                    .addOnCompleteListener { task ->
                        loading = false
                        if (task.isSuccessful) {
                            recoverySuccessMessage = context.getString(R.string.recover_password_success, email)
                            showRecoverySuccessDialog = true
                            error = null
                        } else {
                            error = task.exception?.localizedMessage ?: context.getString(R.string.recover_password_error)
                        }
                    }
            }) {
                Text(stringResource(R.string.recover_password))
            }

            if (showRecoverySuccessDialog) {
                AlertDialog(
                    onDismissRequest = { showRecoverySuccessDialog = false },
                    title = { Text(stringResource(R.string.app_name)) },
                    text = { Text(recoverySuccessMessage) },
                    confirmButton = {
                        Button(onClick = { showRecoverySuccessDialog = false }) {
                            Text(stringResource(R.string.understood))
                        }
                    }
                )
            }

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

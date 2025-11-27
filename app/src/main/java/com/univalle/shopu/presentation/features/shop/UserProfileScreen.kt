package com.univalle.shopu.presentation.features.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.R

@Composable
fun UserProfileScreen(
    auth: FirebaseAuth,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val db = Firebase.firestore
    val userEmail = auth.currentUser?.email ?: ""

    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var editing by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Cargar datos del usuario
    LaunchedEffect(Unit) {
        db.collection("users").document(userEmail).get()
            .addOnSuccessListener { doc ->
                loading = false
                nombres = doc.getString("nombres") ?: ""
                apellidos = doc.getString("apellidos") ?: ""
                codigo = doc.getString("codigo") ?: ""
            }
            .addOnFailureListener {
                loading = false
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
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
                text = stringResource(R.string.user_profile),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground
            )
        }

        Spacer(Modifier.height(24.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = cs.primary)
            }
        } else {
            // Profile Picture Placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(cs.primary.copy(alpha = 0.2f))
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = cs.primary
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.user_data),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = cs.onBackground
            )

            Spacer(Modifier.height(16.dp))

            // Nombres
            OutlinedTextField(
                value = nombres,
                onValueChange = { nombres = it },
                label = { Text(stringResource(R.string.names_label)) },
                enabled = editing,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                    disabledBorderColor = cs.onSurface.copy(alpha = 0.1f),
                    disabledTextColor = cs.onSurface,
                    disabledLabelColor = cs.onSurface.copy(alpha = 0.6f)
                )
            )

            Spacer(Modifier.height(12.dp))

            // Apellidos
            OutlinedTextField(
                value = apellidos,
                onValueChange = { apellidos = it },
                label = { Text(stringResource(R.string.last_names_label)) },
                enabled = editing,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                    disabledBorderColor = cs.onSurface.copy(alpha = 0.1f),
                    disabledTextColor = cs.onSurface,
                    disabledLabelColor = cs.onSurface.copy(alpha = 0.6f)
                )
            )

            Spacer(Modifier.height(12.dp))

            // Código
            OutlinedTextField(
                value = codigo,
                onValueChange = { codigo = it },
                label = { Text(stringResource(R.string.student_code_label)) },
                enabled = editing,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = cs.primary,
                    unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                    disabledBorderColor = cs.onSurface.copy(alpha = 0.1f),
                    disabledTextColor = cs.onSurface,
                    disabledLabelColor = cs.onSurface.copy(alpha = 0.6f)
                )
            )

            Spacer(Modifier.height(12.dp))

            // Correo (no editable)
            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                label = { Text(stringResource(R.string.email_hint)) },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = cs.onSurface.copy(alpha = 0.1f),
                    disabledTextColor = cs.onSurface,
                    disabledLabelColor = cs.onSurface.copy(alpha = 0.6f)
                )
            )

            if (message != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message!!,
                    color = cs.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))

            // Botón Editar/Guardar
            Button(
                onClick = {
                    if (editing) {
                        // Guardar cambios
                        saving = true
                        val updates = mapOf(
                            "nombres" to nombres,
                            "apellidos" to apellidos,
                            "codigo" to codigo
                        )
                        db.collection("users").document(userEmail)
                            .update(updates)
                            .addOnSuccessListener {
                                saving = false
                                editing = false
                                message = context.getString(R.string.profile_updated)
                            }
                            .addOnFailureListener {
                                saving = false
                                message = context.getString(R.string.profile_update_error)
                            }
                    } else {
                        editing = true
                        message = null
                    }
                },
                enabled = !saving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = cs.primary,
                    contentColor = cs.onPrimary
                )
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        color = cs.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        if (editing) stringResource(R.string.save_profile)
                        else stringResource(R.string.edit_profile)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Botón Cerrar Sesión
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = cs.error
                )
            ) {
                Text(stringResource(R.string.logout))
            }
        }
    }

    // Diálogo de confirmación de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.logout_confirm_title)) },
            text = { Text(stringResource(R.string.logout_confirm_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = cs.error
                    )
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

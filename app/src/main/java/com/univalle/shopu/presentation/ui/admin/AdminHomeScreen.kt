package com.univalle.shopu.presentation.ui.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.univalle.shopu.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    isSuperAdmin: Boolean,
    onGoOrders: () -> Unit,
    onGoManageProducts: () -> Unit,
    onGoManageWorkers: () -> Unit,
    onLogout: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text(stringResource(R.string.admin_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = cs.surface)
            )
        },
        containerColor = cs.background
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.shopulogofinal),
                contentDescription = null,
                modifier = Modifier.size(96.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.height(24.dp))

            AdminActionButton(text = stringResource(R.string.manage_orders), onClick = onGoOrders)
            Spacer(Modifier.height(12.dp))
            AdminActionButton(text = stringResource(R.string.manage_products), onClick = onGoManageProducts)
            if (isSuperAdmin) {
                Spacer(Modifier.height(12.dp))
                AdminActionButton(text = stringResource(R.string.manage_workers), onClick = onGoManageWorkers)
            }

            Spacer(Modifier.weight(1f))
            Button(
                onClick = {
                    // Cerrar sesión real antes de navegar
                    Firebase.auth.signOut()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(R.string.logout)) }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AdminActionButton(text: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = cs.primary)
    ) {
        Text(text)
    }
}

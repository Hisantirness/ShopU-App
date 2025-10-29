package com.univalle.shopu.presentation.features.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.univalle.shopu.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProductView(
    productId: String,
    onDone: () -> Unit,
    onBack: () -> Unit,
    vm: EditProductViewModel = viewModel()
) {
    val cs = MaterialTheme.colorScheme
    val state by vm.state

    LaunchedEffect(productId) {
        vm.init(productId)
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> vm.updateImage(uri) }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Editar producto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = null) } },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = cs.surface)
            )
        },
        containerColor = cs.background
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            // Imagen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentAlignment = Alignment.Center
            ) {
                val showUri = state.imageUri
                val showUrl = state.imageUrl
                when {
                    showUri != null -> AsyncImage(model = showUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    !showUrl.isNullOrBlank() -> AsyncImage(model = showUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(painter = painterResource(id = R.drawable.shopulogofinal), contentDescription = null, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Sin imagen", color = Color.Gray)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                Button(onClick = {
                    pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) { Text("Elegir de galería") }
                OutlinedButton(onClick = { vm.updateImage(null) }) { Text("Mantener actual") }
            }

            // Campos
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = state.name, onValueChange = vm::updateName, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.price, onValueChange = vm::updatePrice, label = { Text("Precio") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = state.quantity, onValueChange = vm::updateQuantity, label = { Text("Cantidad disponible") }, singleLine = true, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            val categories = listOf("Café","Snack","Bebidas frías","Postres")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                categories.forEach { cat ->
                    val selected = state.category == cat
                    FilterChip(
                        selected = selected,
                        onClick = { vm.updateCategory(cat) },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = cs.surface,
                            selectedContainerColor = cs.primary,
                            labelColor = cs.onSurface,
                            selectedLabelColor = cs.onPrimary
                        )
                    )
                }
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = Color.Red, modifier = Modifier.padding(horizontal = 16.dp))
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { vm.save() },
                enabled = !state.saving,
                colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp)
            ) {
                if (state.saving) CircularProgressIndicator(color = cs.onPrimary, strokeWidth = 2.dp) else Text("Guardar cambios")
            }

            if (state.success) {
                Spacer(Modifier.height(8.dp))
                Text("Cambios guardados", color = cs.onBackground.copy(alpha = 0.8f), modifier = Modifier.padding(horizontal = 16.dp))
                LaunchedEffect("done") { onDone() }
            }
        }
    }
}

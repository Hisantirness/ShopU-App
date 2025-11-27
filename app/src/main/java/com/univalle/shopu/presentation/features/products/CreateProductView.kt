package com.univalle.shopu.presentation.features.products

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.univalle.shopu.R
import androidx.compose.ui.res.stringResource

@Composable
fun CreateProductView(onDone: () -> Unit, onBack: () -> Unit, vm: CreateProductViewModel = viewModel()) {
    val cs = MaterialTheme.colorScheme
    val state by vm.state
    val context = LocalContext.current

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? -> vm.updateImage(uri) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.create_product_title), style = MaterialTheme.typography.titleLarge, color = cs.onBackground, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBack) { Text(stringResource(R.string.back_button)) }
        }
        Spacer(Modifier.height(12.dp))

        // Image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.imageUri != null) {
                AsyncImage(
                    model = state.imageUri,
                    contentDescription = stringResource(R.string.product_image_desc),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.shopulogofinal)
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.shopulogofinal),
                        contentDescription = null,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.no_image), color = Color.Gray)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text(stringResource(R.string.pick_from_gallery)) }
            OutlinedButton(onClick = { vm.updateImage(null) }) { Text(stringResource(R.string.no_photo)) }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = state.name, onValueChange = vm::updateName, label = { Text(stringResource(R.string.name_label)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = state.price, onValueChange = vm::updatePrice, label = { Text(stringResource(R.string.price_label)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = state.quantity, onValueChange = vm::updateQuantity, label = { Text(stringResource(R.string.quantity_available_label)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        val categories = listOf("Café","Snack","Bebidas frías","Postres")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
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
            Text(it, color = Color.Red)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { vm.createProduct() },
            enabled = !state.loading,
            colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (state.loading) CircularProgressIndicator(color = cs.onPrimary, strokeWidth = 2.dp) else Text(stringResource(R.string.create_product_button))
        }

        if (state.success) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text(stringResource(R.string.product_created_title)) },
                text = { Text(stringResource(R.string.image_url_label, state.createdImageUrl ?: stringResource(R.string.no_image))) },
                confirmButton = {
                    Button(onClick = { vm.clearSuccess(); onDone() }) { Text(stringResource(R.string.go_to_admin)) }
                }
            )
        }
    }
}

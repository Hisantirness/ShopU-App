package com.univalle.shopu.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext

import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.univalle.shopu.R
import com.univalle.shopu.presentation.model.Product

@Composable
fun AdminScreen(onDone: () -> Unit, onBack: () -> Unit) {
    val red = colorResource(id = R.color.red_primary)
    val black = colorResource(id = R.color.black)
    val bg = colorResource(id = R.color.app_background)

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Café") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var successDialog by remember { mutableStateOf(false) }
    var createdUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val auth = Firebase.auth
    val context = LocalContext.current

    // Asegurar sesión del ADMIN real para reglas de producción (admin-only)
    LaunchedEffect(Unit) {
        try {
            val current = auth.currentUser
            val adminEmail = "santiago.villa@correounivalle.edu.co"
            val adminPass = "123456"
            if (current == null || current.email != adminEmail) {
                auth.signInWithEmailAndPassword(adminEmail, adminPass).await()
            }
        } catch (_: Exception) {
            // Si falla, el flujo de crear producto mostrará el error de reglas
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> imageUri = uri }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Administrador", style = MaterialTheme.typography.titleLarge, color = black, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("Volver") }
        }
        Spacer(Modifier.height(12.dp))

        // Image preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagen del producto",
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
                    Text("Sin imagen", color = Color.Gray)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                pickImageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) { Text("Elegir de galería") }
            OutlinedButton(onClick = { imageUri = null }) { Text("Sin foto") }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Cantidad disponible") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        val categories = listOf("Café","Snack","Bebidas frías","Postres")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            categories.forEach { cat ->
                val selected = category == cat
                FilterChip(
                    selected = selected,
                    onClick = { category = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color.White,
                        selectedContainerColor = red,
                        labelColor = black,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error!!, color = Color.Red)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                error = null
                val priceVal = price.replace("$", "").replace(".", "").replace(",", ".").toDoubleOrNull()?.let { it } ?: run {
                    error = "Precio inválido"; return@Button
                }
                val qtyVal = quantity.toIntOrNull() ?: run { error = "Cantidad inválida"; return@Button }
                if (name.isBlank()) { error = "Nombre requerido"; return@Button }

                loading = true
                scope.launch {
                    try {
                        val db = Firebase.firestore
                        val storage = FirebaseStorage.getInstance()
                        val auth = Firebase.auth

                        // Reafirmar autenticación ADMIN (por si expiró)
                        val adminEmail = "santiago.villa@correounivalle.edu.co"
                        val adminPass = "123456"
                        if (auth.currentUser?.email != adminEmail) {
                            auth.signInWithEmailAndPassword(adminEmail, adminPass).await()
                        }

                        var urlStr: String? = null
                        val uri = imageUri
                        if (uri != null) {
                            // Detectar MIME y extensión reales
                            val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                            val ext = when (mime) {
                                "image/png" -> "png"
                                "image/webp" -> "webp"
                                else -> "jpg"
                            }
                            val ref = storage.reference.child("products/${System.currentTimeMillis()}.$ext")
                            val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                                .setContentType(mime)
                                .build()
                            ref.putFile(uri, metadata).await()
                            // A veces Storage tarda unos ms en propagar el objeto; reintentar downloadUrl
                            var lastError: Exception? = null
                            repeat(3) { attempt ->
                                try {
                                    val url = ref.downloadUrl.await()
                                    urlStr = url.toString()
                                    lastError = null
                                    return@repeat
                                } catch (e: Exception) {
                                    lastError = e
                                    if (attempt < 2) delay(400) // reintento
                                }
                            }
                            if (urlStr == null) throw lastError ?: Exception("No se pudo obtener la URL de descarga")
                        }

                        val product = hashMapOf(
                            "name" to name.trim(),
                            "price" to priceVal,
                            "quantity" to qtyVal,
                            "imageUrl" to urlStr,
                            "category" to category
                        )
                        val doc = db.collection("products").add(product).await()

                        createdUrl = urlStr ?: "(sin imagen)"
                        successDialog = true
                    } catch (e: Exception) {
                        error = e.localizedMessage ?: "Error creando producto"
                    } finally {
                        loading = false
                    }
                }
            },
            enabled = !loading,
            colors = ButtonDefaults.buttonColors(containerColor = red, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (loading) CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp) else Text("Crear producto")
        }

        if (successDialog) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("Producto creado") },
                text = { Text("Imagen: $createdUrl") },
                confirmButton = {
                    Button(onClick = { successDialog = false; onDone() }) { Text("Ir al Menú") }
                }
            )
        }
    }
}

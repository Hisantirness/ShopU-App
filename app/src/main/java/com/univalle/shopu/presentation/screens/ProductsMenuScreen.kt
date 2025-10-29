package com.univalle.shopu.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.univalle.shopu.R
import com.univalle.shopu.presentation.model.Product
import com.univalle.shopu.presentation.util.CartStore
import com.univalle.shopu.presentation.util.formatCurrencyCOP

@Composable
fun ProductsMenuScreen(onCartClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme

    var search by remember { mutableStateOf("") }
    val categories = listOf("Café", "Snack", "Bebidas frías", "Postres")
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    val db = Firebase.firestore
    var products by remember { mutableStateOf(listOf<Product>()) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("products").addSnapshotListener { snap, _ ->
            loading = false
            products = snap?.documents?.mapNotNull { it.toObject(Product::class.java)?.copy(id = it.id) } ?: emptyList()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(cs.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cs.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.shopulogofinal),
                contentDescription = "logo",
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
            Text("Menú de Compras", color = cs.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrito",
                tint = cs.primary,
                modifier = Modifier.size(28.dp).clickable { onCartClick() }
            )
        }

        // Search
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Buscar productos...") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = cs.primary,
                unfocusedBorderColor = cs.onSurface.copy(alpha = 0.2f),
                cursorColor = cs.primary,
                focusedLabelColor = cs.primary
            )
        )

        // Categories
        Row(Modifier.padding(horizontal = 16.dp)) {
            categories.forEach { cat ->
                val selected = selectedCategory == cat
                FilterChip(
                    selected = selected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = cs.surface,
                        selectedContainerColor = cs.primary,
                        labelColor = cs.onSurface,
                        selectedLabelColor = cs.onPrimary
                    )
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        if (loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = cs.primary)
            }
        } else {
            val filtered = products.filter { p ->
                (selectedCategory.isBlank() || p.category.equals(selectedCategory, ignoreCase = true)) &&
                        (search.isBlank() || p.name.contains(search, ignoreCase = true))
            }
            LazyColumn(Modifier.fillMaxSize()) {
                items(filtered) { p ->
                    ProductCard(p, onAdd = {
                        CartStore.add(
                            com.univalle.shopu.presentation.model.CartItem(
                                id = p.id, name = p.name, price = p.price, imageUrl = p.imageUrl
                            )
                        )
                    })
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ProductCard(p: Product, onAdd: () -> Unit) {
    val cs = MaterialTheme.colorScheme

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val model = if (!p.imageUrl.isNullOrBlank() && (p.imageUrl.startsWith("http://") || p.imageUrl.startsWith("https://"))) p.imageUrl else null
            AsyncImage(
                model = model,
                contentDescription = p.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.shopulogofinal),
                error = painterResource(id = R.drawable.shopulogofinal)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, color = cs.onSurface, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text("Precio", color = cs.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrencyCOP(p.price), color = cs.onSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onAdd, colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)) {
                    Text("Agregar")
                }
            }
        }
    }
}

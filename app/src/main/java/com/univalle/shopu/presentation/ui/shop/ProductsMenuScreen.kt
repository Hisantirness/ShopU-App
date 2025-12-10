package com.univalle.shopu.presentation.ui.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
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
import com.univalle.shopu.domain.model.Product
import com.univalle.shopu.presentation.util.CartStore
import com.univalle.shopu.presentation.util.formatCurrencyCOP
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun ProductsMenuScreen(
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
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

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
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
                contentDescription = stringResource(R.string.app_logo_desc),
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.menu_title), color = cs.onSurface, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onHistoryClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.List,
                    contentDescription = stringResource(R.string.order_history),
                    tint = cs.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Person,
                    contentDescription = stringResource(R.string.user_profile),
                    tint = cs.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = stringResource(R.string.cart_desc),
                tint = cs.primary,
                modifier = Modifier.size(28.dp).clickable { onCartClick() }
            )
        }

        // Search
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
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
                    ProductCard(
                        product = p,
                        snackbarHostState = snackbarHostState,
                        context = context
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun ProductCard(
    product: Product,
    snackbarHostState: SnackbarHostState,
    context: android.content.Context
) {
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val model = if (!product.imageUrl.isNullOrBlank() && (product.imageUrl.startsWith("http://") || product.imageUrl.startsWith("https://"))) product.imageUrl else null
            AsyncImage(
                model = model,
                contentDescription = product.name,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.shopulogofinal),
                error = painterResource(id = R.drawable.shopulogofinal)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(product.name, color = cs.onSurface, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.price_label), color = cs.onSurface.copy(alpha = 0.6f))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatCurrencyCOP(product.price), color = cs.onSurface, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        CartStore.add(
                            com.univalle.shopu.domain.model.CartItem(
                                id = product.id,
                                name = product.name,
                                price = product.price,
                                imageUrl = product.imageUrl
                            )
                        )
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = context.getString(R.string.item_added_to_cart),
                                duration = SnackbarDuration.Short
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = cs.primary, contentColor = cs.onPrimary)
                ) {
                    Text(stringResource(R.string.add_button))
                }
            }
        }
    }
}

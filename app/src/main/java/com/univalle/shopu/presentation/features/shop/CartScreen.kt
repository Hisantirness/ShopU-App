package com.univalle.shopu.presentation.features.shop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.univalle.shopu.R
import com.univalle.shopu.presentation.util.CartStore
import com.univalle.shopu.presentation.util.formatCurrencyCOP
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.univalle.shopu.ui.components.ShopUButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onProceedToPayment: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.cart_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.back_button)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    titleContentColor = cs.onSurface
                )
            )
        },
        containerColor = cs.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Cart Items
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = CartStore.items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = cs.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Product Image
                            if (!item.imageUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.shopulogofinal),
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(cs.surfaceVariant),
                                    contentScale = ContentScale.Fit
                                )
                            }

                            // Product Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = cs.onSurface
                                )
                                Text(
                                    text = stringResource(R.string.price_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = cs.onSurface.copy(alpha = 0.6f)
                                )
                                
                                // Quantity Controls
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { CartStore.dec(item.id) },
                                        modifier = Modifier.size(32.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = cs.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = "−",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cs.onSurfaceVariant
                                        )
                                    }
                                    
                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.widthIn(min = 24.dp)
                                    )
                                    
                                    IconButton(
                                        onClick = { CartStore.inc(item.id) },
                                        modifier = Modifier.size(32.dp),
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = cs.surfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text = "+",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = cs.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            // Price and Delete Button
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = formatCurrencyCOP(item.subtotal),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = cs.onSurface
                                )
                                
                                Spacer(Modifier.height(8.dp))
                                
                                // Delete Button - Icon only
                                IconButton(
                                    onClick = { CartStore.remove(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.remove_item),
                                        tint = cs.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Section - Totals and Confirm
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cs.surface)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subtotal
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.subtotal_label),
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurface
                    )
                    Text(
                        text = formatCurrencyCOP(CartStore.total()),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = cs.onSurface
                    )
                }

                // Taxes (0 for now)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.taxes_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formatCurrencyCOP(0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurface.copy(alpha = 0.7f)
                    )
                }

                Divider(color = cs.onSurface.copy(alpha = 0.2f))

                // Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.total_label),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cs.onSurface
                    )
                    Text(
                        text = formatCurrencyCOP(CartStore.total()),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = cs.primary
                    )
                }

                // Error Message
                if (message != null) {
                    Text(
                        text = message!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.error
                    )
                }

                // Confirm Button
                ShopUButton(
                    text = stringResource(R.string.confirm_order),
                    onClick = {
                        if (CartStore.items.isEmpty()) {
                            message = context.getString(R.string.cart_empty)
                            return@ShopUButton
                        }
                        onProceedToPayment()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

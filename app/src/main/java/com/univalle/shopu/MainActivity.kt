package com.univalle.shopu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.univalle.shopu.presentation.navigation.NavigationWrapper
import com.univalle.shopu.ui.theme.ShopUTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShopUTheme {
                AppRoot()
            }
        }
    }
}

@Composable
fun AppRoot() {
    val navController = rememberNavController()
    val auth = Firebase.auth
    Surface(color = MaterialTheme.colorScheme.background) {
        NavigationWrapper(
            auth = auth,
            navController = navController
        )
    }
}

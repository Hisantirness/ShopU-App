package com.univalle.shopu.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.univalle.shopu.presentation.screens.LoginScreen
import com.univalle.shopu.presentation.screens.RegisterScreen
import com.univalle.shopu.presentation.screens.WelcomeScreen
import com.univalle.shopu.presentation.screens.ProductsMenuScreen
import com.univalle.shopu.presentation.screens.CartScreen
import com.univalle.shopu.presentation.screens.AdminHomeScreen
import com.univalle.shopu.presentation.screens.ManageAdminsScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val WELCOME = "welcome"
    const val MENU = "menu"
    const val CART = "cart"
    const val ADMIN_HOME = "admin_home"
    const val ADMIN_CREATE_PRODUCT = "admin_create_product"
    const val ADMIN_EDIT_PRODUCT = "admin_edit_product/{productId}"
    const val ADMIN_ORDERS = "admin_orders"
    const val ADMIN_MANAGE_PRODUCTS = "admin_manage_products"
    const val ADMIN_MANAGE_ADMINS = "admin_manage_admins"
    const val ADMIN_MANAGE_WORKERS = "admin_manage_workers"
}

@Composable
fun NavigationWrapper(
    auth: FirebaseAuth,
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                auth = auth,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onLoginSuccess = {
                    navController.navigate(Routes.MENU) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onAdminLogin = {
                    navController.navigate(Routes.ADMIN_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                auth = auth,
                onRegistered = {
                    // Volver a LOGIN para que el usuario confirme su email antes de entrar
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.WELCOME) {
            WelcomeScreen()
        }

        composable(Routes.MENU) {
            ProductsMenuScreen(
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }

        composable(Routes.CART) {
            CartScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onGoOrders = { navController.navigate(Routes.ADMIN_ORDERS) },
                onGoManageProducts = { navController.navigate(Routes.ADMIN_MANAGE_PRODUCTS) },
                onGoManageWorkers = { navController.navigate(Routes.ADMIN_MANAGE_WORKERS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADMIN_CREATE_PRODUCT) {
            com.univalle.shopu.presentation.features.products.CreateProductView(
                onDone = {
                    navController.navigate(Routes.ADMIN_HOME) {
                        popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_EDIT_PRODUCT) { backStackEntry ->
            val pid = backStackEntry.arguments?.getString("productId") ?: ""
            com.univalle.shopu.presentation.features.products.EditProductView(
                productId = pid,
                onDone = {
                    navController.navigate(Routes.ADMIN_MANAGE_PRODUCTS) {
                        popUpTo(Routes.ADMIN_MANAGE_PRODUCTS) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_ORDERS) {
            com.univalle.shopu.presentation.features.orders.OrdersView(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_PRODUCTS) {
            com.univalle.shopu.presentation.features.products.ProductsView(
                onBack = { navController.popBackStack() },
                onCreateNew = { navController.navigate(Routes.ADMIN_CREATE_PRODUCT) },
                onEditProduct = { pid -> navController.navigate("admin_edit_product/${'$'}pid") }
            )
        }

        composable(Routes.ADMIN_MANAGE_ADMINS) {
            ManageAdminsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_WORKERS) {
            com.univalle.shopu.presentation.features.workers.WorkersView(onBack = { navController.popBackStack() })
        }
    }
}

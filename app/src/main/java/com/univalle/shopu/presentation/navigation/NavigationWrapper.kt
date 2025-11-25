package com.univalle.shopu.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.univalle.shopu.presentation.features.auth.LoginScreen
import com.univalle.shopu.presentation.features.auth.RegisterScreen

import com.univalle.shopu.presentation.features.shop.ProductsMenuScreen
import com.univalle.shopu.presentation.features.shop.CartScreen
import com.univalle.shopu.presentation.features.admin.AdminHomeScreen
import com.univalle.shopu.presentation.features.admin.ManageAdminsScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"

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
                onAdminLogin = { isSuper ->
                    navController.navigate("admin_home/$isSuper") {
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


        composable(Routes.MENU) {
            ProductsMenuScreen(
                onCartClick = { navController.navigate(Routes.CART) }
            )
        }

        composable(Routes.CART) {
            CartScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = "admin_home/{isSuper}",
            arguments = listOf(androidx.navigation.navArgument("isSuper") { type = androidx.navigation.NavType.BoolType })
        ) { backStackEntry ->
            val isSuper = backStackEntry.arguments?.getBoolean("isSuper") ?: false
            AdminHomeScreen(
                isSuperAdmin = isSuper,
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
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "admin_edit_product/{productId}",
            arguments = listOf(androidx.navigation.navArgument("productId") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val pid = (backStackEntry.arguments?.getString("productId") ?: "").trim()
            com.univalle.shopu.presentation.features.products.EditProductView(
                productId = pid,
                onDone = {
                    navController.popBackStack()
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
                onEditProduct = { pid -> navController.navigate("admin_edit_product/$pid") }
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

package com.univalle.shopu.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.firebase.auth.FirebaseAuth
import com.univalle.shopu.presentation.ui.auth.LoginScreen
import com.univalle.shopu.presentation.ui.auth.RegisterScreen
import com.univalle.shopu.presentation.ui.shop.ProductsMenuScreen
import com.univalle.shopu.presentation.ui.shop.CartScreen
import com.univalle.shopu.presentation.ui.shop.UserProfileScreen
import com.univalle.shopu.presentation.ui.shop.OrderHistoryScreen
import com.univalle.shopu.presentation.ui.shop.PaymentScreen
import com.univalle.shopu.presentation.ui.admin.AdminHomeScreen
import com.univalle.shopu.presentation.ui.admin.ManageAdminsScreen
import com.univalle.shopu.presentation.ui.products.CreateProductView
import com.univalle.shopu.presentation.ui.products.EditProductView
import com.univalle.shopu.presentation.ui.products.ProductsView
import com.univalle.shopu.presentation.ui.orders.OrdersView
import com.univalle.shopu.presentation.ui.orders.OrderDetailsScreen
import com.univalle.shopu.presentation.ui.workers.WorkersView

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"

    const val MENU = "menu"
    const val CART = "cart"
    const val USER_PROFILE = "user_profile"
    const val ORDER_HISTORY = "order_history"
    const val PAYMENT = "payment"
    
    const val ADMIN_HOME = "admin_home"
    const val ADMIN_CREATE_PRODUCT = "admin_create_product"
    const val ADMIN_EDIT_PRODUCT = "admin_edit_product/{productId}"
    const val ADMIN_ORDERS = "admin_orders"
    const val ADMIN_MANAGE_PRODUCTS = "admin_manage_products"
    const val ADMIN_MANAGE_ADMINS = "admin_manage_admins"
    const val ADMIN_MANAGE_WORKERS = "admin_manage_workers"
    const val ORDER_DETAILS = "order_details/{orderId}/{isAdmin}"
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
                onCartClick = { navController.navigate(Routes.CART) },
                onProfileClick = { navController.navigate(Routes.USER_PROFILE) },
                onHistoryClick = { navController.navigate(Routes.ORDER_HISTORY) }
            )
        }

        composable(Routes.CART) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onProceedToPayment = { navController.navigate(Routes.PAYMENT) }
            )
        }

        composable(Routes.USER_PROFILE) {
            UserProfileScreen(
                auth = auth,
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ORDER_HISTORY) {
            OrderHistoryScreen(
                auth = auth,
                onBack = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate("order_details/$orderId/false")
                }
            )
        }

        composable(Routes.PAYMENT) {
            PaymentScreen(
                auth = auth,
                onPaymentConfirmed = {
                    navController.navigate(Routes.ORDER_HISTORY) {
                        popUpTo(Routes.MENU) { inclusive = false }
                    }
                },
                onBack = { navController.popBackStack() }
            )
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
            CreateProductView(
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
            EditProductView(
                productId = pid,
                onDone = {
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_ORDERS) {
            OrdersView(
                onBack = { navController.popBackStack() },
                onOrderClick = { orderId ->
                    navController.navigate("order_details/$orderId/true")
                }
            )
        }

        composable(Routes.ADMIN_MANAGE_PRODUCTS) {
            ProductsView(
                onBack = { navController.popBackStack() },
                onCreateNew = { navController.navigate(Routes.ADMIN_CREATE_PRODUCT) },
                onEditProduct = { pid -> navController.navigate("admin_edit_product/$pid") }
            )
        }

        composable(Routes.ADMIN_MANAGE_ADMINS) {
            ManageAdminsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ADMIN_MANAGE_WORKERS) {
            WorkersView(onBack = { navController.popBackStack() })
        }

        composable(
            route = "order_details/{orderId}/{isAdmin}",
            arguments = listOf(
                androidx.navigation.navArgument("orderId") { type = androidx.navigation.NavType.StringType },
                androidx.navigation.navArgument("isAdmin") { type = androidx.navigation.NavType.BoolType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val isAdmin = backStackEntry.arguments?.getBoolean("isAdmin") ?: false
            OrderDetailsScreen(
                orderId = orderId,
                isAdmin = isAdmin,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

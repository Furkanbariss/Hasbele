package com.virata.hamdele.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.virata.hamdele.feature.about.AboutRoute
import com.virata.hamdele.feature.about.WelcomeRoute
import com.virata.hamdele.feature.counter.CounterRoute
import com.virata.hamdele.feature.history.HistoryRoute
import com.virata.hamdele.feature.muezzin.MuezzinRoute
import com.virata.hamdele.feature.settings.SettingsRoute
import com.virata.hamdele.feature.zikirs.ZikirListRoute

private object Routes {
    const val WELCOME = "welcome"
    const val COUNTER = "counter"
    const val ZIKIRS = "zikirs"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val MUEZZIN = "muezzin"
    const val ABOUT = "about"
}

/**
 * [showWelcome] yalnız ilk kurulumda true'dur; bilgilendirme okununca Tesbih ekranı yığının dibine
 * geçer. Tesbih ekranı her zaman yığının dibinde durur.
 */
@Composable
fun AppNavHost(showWelcome: Boolean, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    // Başlangıç ekranı açılışta bir kez seçilir; bilgilendirme okununca değişmez.
    val start = remember { if (showWelcome) Routes.WELCOME else Routes.COUNTER }
    NavHost(
        navController = navController,
        startDestination = start,
        modifier = modifier,
    ) {
        composable(Routes.WELCOME) {
            WelcomeRoute(
                onContinue = {
                    navController.navigate(Routes.COUNTER) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.COUNTER) {
            CounterRoute(
                onOpenZikirs = { navController.open(Routes.ZIKIRS) },
                onOpenHistory = { navController.open(Routes.HISTORY) },
                onOpenSettings = { navController.open(Routes.SETTINGS) },
                onOpenMuezzin = { navController.open(Routes.MUEZZIN) },
                onOpenAbout = { navController.open(Routes.ABOUT) },
            )
        }
        composable(Routes.MUEZZIN) {
            MuezzinRoute(
                onBack = { navController.navigateUp() },
                // Tesbih ekranı her zaman yığının dibindedir; tesbihat orada sayılır.
                onOpenCounter = { navController.popBackStack(Routes.COUNTER, inclusive = false) },
            )
        }
        composable(Routes.ZIKIRS) {
            ZikirListRoute(onBack = { navController.navigateUp() })
        }
        composable(Routes.HISTORY) {
            HistoryRoute(onBack = { navController.navigateUp() })
        }
        composable(Routes.SETTINGS) {
            SettingsRoute(onBack = { navController.navigateUp() })
        }
        composable(Routes.ABOUT) {
            AboutRoute(onBack = { navController.navigateUp() })
        }
    }
}

/** Çift dokunuşta aynı ekranın yığına iki kez girmesini önler. */
private fun NavController.open(route: String) {
    navigate(route) { launchSingleTop = true }
}

package com.journeyticket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.journeyticket.ui.capture.CaptureScreen
import com.journeyticket.ui.confirm.ConfirmScreen
import com.journeyticket.ui.home.HomeScreen
import com.journeyticket.ui.preview.TicketPreviewScreen
import com.journeyticket.ui.settings.SettingsScreen
import com.journeyticket.ui.timeline.TimelineScreen

/** 单 Activity 导航骨架：六大路由占位，参数化路由随功能落地补充 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinations.HOME.route,
        modifier = modifier,
    ) {
        composable(Destinations.HOME.route) {
            HomeScreen(
                onGoCapture = { navController.navigate(Destinations.CAPTURE.route) },
                onGoTimeline = { navController.navigate(Destinations.TIMELINE.route) },
                onGoSettings = { navController.navigate(Destinations.SETTINGS.route) },
            )
        }
        composable(Destinations.TIMELINE.route) {
            TimelineScreen(
                onOpenTicket = { navController.navigate(Destinations.PREVIEW.route) },
            )
        }
        composable(Destinations.CAPTURE.route) {
            CaptureScreen(
                onNavigateToConfirm = { navController.navigate(Destinations.CONFIRM.route) },
            )
        }
        composable(Destinations.CONFIRM.route) {
            ConfirmScreen(
                onGenerateOnly = { navController.navigate(Destinations.PREVIEW.route) },
            )
        }
        composable(Destinations.PREVIEW.route) {
            TicketPreviewScreen()
        }
        composable(Destinations.SETTINGS.route) {
            SettingsScreen()
        }
    }
}

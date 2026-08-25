package com.zioanacleto.feedtracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zioanacleto.feedtracker.features.home.HomeScreen
import com.zioanacleto.feedtracker.features.home.navigation.HomeRoute
import com.zioanacleto.feedtracker.features.newtracking.NewTrackingScreen
import com.zioanacleto.feedtracker.features.newtracking.navigation.NewTrackingRoute

@Composable
fun FeedTrackerNavHost(
    modifier: Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        modifier = Modifier,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(modifier) { navController.navigate(NewTrackingRoute) }
        }
        composable<NewTrackingRoute> {
            NewTrackingScreen { navController.popBackStack() }
        }
    }
}
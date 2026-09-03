package com.zioanacleto.feedtracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.zioanacleto.feedtracker.features.home.HomeScreen
import com.zioanacleto.feedtracker.features.home.navigation.HomeRoute
import com.zioanacleto.feedtracker.features.newtracking.NewTrackingScreen
import com.zioanacleto.feedtracker.features.newtracking.navigation.NewTrackingRoute
import com.zioanacleto.feedtracker.features.pasttracking.PastTrackingScreen
import com.zioanacleto.feedtracker.features.pasttracking.navigation.PastTrackingRoute

@Composable
fun FeedTrackerNavHost(modifier: Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        modifier = Modifier,
        startDestination = HomeRoute,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                modifier = modifier,
                onNewTrackingClick = { name, surname, birthDate ->
                    navController.navigate(
                        NewTrackingRoute(
                            name = name,
                            surname = surname,
                            birthDate = birthDate,
                        ),
                    )
                },
                onPastTrackingClick = { name, surname, birthDate ->
                    navController.navigate(
                        PastTrackingRoute(
                            name = name,
                            surname = surname,
                            birthDate = birthDate,
                        ),
                    )
                },
            )
        }
        composable<NewTrackingRoute> { entry ->
            val route = entry.toRoute<NewTrackingRoute>()
            NewTrackingScreen(
                initialName = route.name,
                initialSurname = route.surname,
                initialBirthDate = route.birthDate,
                onBackButtonClick = { navController.popBackStack() },
            )
        }
        composable<PastTrackingRoute> { entry ->
            val route = entry.toRoute<PastTrackingRoute>()
            PastTrackingScreen(
                initialName = route.name,
                initialSurname = route.surname,
                initialBirthDate = route.birthDate,
                onBackButtonClick = { navController.popBackStack() },
            )
        }
    }
}

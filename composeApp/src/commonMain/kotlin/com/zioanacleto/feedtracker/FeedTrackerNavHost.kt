package com.zioanacleto.feedtracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.zioanacleto.feedtracker.features.home.HomeScreen
import com.zioanacleto.feedtracker.features.home.navigation.HomeRoute
import com.zioanacleto.feedtracker.features.login.EmailLoginScreen
import com.zioanacleto.feedtracker.features.login.EmailSignUpScreen
import com.zioanacleto.feedtracker.features.login.LoginMethodsScreen
import com.zioanacleto.feedtracker.features.login.navigation.EmailLoginRoute
import com.zioanacleto.feedtracker.features.login.navigation.EmailSignUpRoute
import com.zioanacleto.feedtracker.features.login.navigation.LoginRoute
import com.zioanacleto.feedtracker.features.newtracking.NewTrackingScreen
import com.zioanacleto.feedtracker.features.newtracking.navigation.NewTrackingRoute
import com.zioanacleto.feedtracker.features.pasttracking.PastTrackingScreen
import com.zioanacleto.feedtracker.features.pasttracking.navigation.PastTrackingRoute
import com.zioanacleto.feedtracker.features.settings.PersonalSettingsScreen
import com.zioanacleto.feedtracker.features.settings.navigation.PersonalSettingsRoute

@Composable
fun FeedTrackerNavHost(isLoggedIn: Boolean, modifier: Modifier = Modifier) {
    key(if (isLoggedIn) "logged-in" else "logged-out") {
        if (isLoggedIn) {
            LoggedInNavHost(modifier)
        } else {
            LoggedOutNavHost(modifier)
        }
    }
}

@Composable
private fun LoggedOutNavHost(modifier: Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        modifier = modifier.fillMaxSize(),
        startDestination = LoginRoute,
    ) {
        composable<LoginRoute> {
            LoginMethodsScreen(
                modifier = Modifier.fillMaxSize(),
                onSignUpClick = { navController.navigate(EmailSignUpRoute) },
                onEmailMethodSelected = { navController.navigate(EmailLoginRoute) },
            )
        }
        composable<EmailSignUpRoute> {
            EmailSignUpScreen(
                modifier = Modifier.fillMaxSize(),
                onBackButtonClick = { navController.popBackStack() },
            )
        }
        composable<EmailLoginRoute> {
            EmailLoginScreen(
                modifier = Modifier.fillMaxSize(),
                onBackButtonClick = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun LoggedInNavHost(modifier: Modifier, navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        modifier = modifier.fillMaxSize(),
        startDestination = HomeRoute,
    ) {
        composable<HomeRoute> {
            HomeScreen(
                modifier = Modifier.fillMaxSize(),
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
                onPersonalSettingsClick = { navController.navigate(PersonalSettingsRoute) },
            )
        }
        composable<PersonalSettingsRoute> {
            PersonalSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onBackButtonClick = { navController.popBackStack() },
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

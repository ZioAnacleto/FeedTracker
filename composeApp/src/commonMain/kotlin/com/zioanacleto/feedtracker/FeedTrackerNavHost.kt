package com.zioanacleto.feedtracker

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.zioanacleto.feedtracker.features.home.HomeScreen
import com.zioanacleto.feedtracker.features.home.navigation.HomeRoute
import com.zioanacleto.feedtracker.features.login.EmailLoginScreen
import com.zioanacleto.feedtracker.features.login.EmailSignUpScreen
import com.zioanacleto.feedtracker.features.login.ForgotPasswordScreen
import com.zioanacleto.feedtracker.features.login.LoginMethodsScreen
import com.zioanacleto.feedtracker.features.login.navigation.EmailLoginRoute
import com.zioanacleto.feedtracker.features.login.navigation.EmailSignUpRoute
import com.zioanacleto.feedtracker.features.login.navigation.ForgotPasswordRoute
import com.zioanacleto.feedtracker.features.login.navigation.LoginRoute
import com.zioanacleto.feedtracker.features.newtracking.NewTrackingScreen
import com.zioanacleto.feedtracker.features.newtracking.navigation.NewTrackingRoute
import com.zioanacleto.feedtracker.features.pasttracking.PastTrackingScreen
import com.zioanacleto.feedtracker.features.pasttracking.navigation.PastTrackingRoute
import com.zioanacleto.feedtracker.features.settings.personal.PersonalSettingsScreen
import com.zioanacleto.feedtracker.features.settings.personal.navigation.PersonalSettingsRoute
import com.zioanacleto.feedtracker.features.settings.profile.ProfileSettingsScreen
import com.zioanacleto.feedtracker.features.settings.profile.navigation.PROFILE_SAVED_RESULT
import com.zioanacleto.feedtracker.features.settings.profile.navigation.ProfileSettingsRoute
import com.zioanacleto.feedtracker.widget.NewTrackingNavigator

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
                onForgotPasswordClick = { navController.navigate(ForgotPasswordRoute) },
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                modifier = Modifier.fillMaxSize(),
                onBackButtonClick = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun LoggedInNavHost(modifier: Modifier, navController: NavHostController = rememberNavController()) {
    val openNewTracking by NewTrackingNavigator.openRequested.collectAsState()
    LaunchedEffect(openNewTracking) {
        if (!openNewTracking) return@LaunchedEffect
        navController.openNewTrackingSession()
        NewTrackingNavigator.consume()
    }
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
        composable<PersonalSettingsRoute> { entry ->
            val profileSaved by entry.savedStateHandle
                .getStateFlow(PROFILE_SAVED_RESULT, false)
                .collectAsState()
            PersonalSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onBackButtonClick = { navController.popBackStack() },
                onProfileClick = { navController.navigate(ProfileSettingsRoute) },
                showProfileSavedMessage = profileSaved,
                onProfileSavedMessageShown = { entry.savedStateHandle[PROFILE_SAVED_RESULT] = false },
            )
        }
        composable<ProfileSettingsRoute> {
            ProfileSettingsScreen(
                modifier = Modifier.fillMaxSize(),
                onBackButtonClick = { navController.popBackStack() },
                onSaved = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(PROFILE_SAVED_RESULT, true)
                    navController.popBackStack()
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

private fun NavHostController.openNewTrackingSession() {
    if (currentDestination?.hasRoute<NewTrackingRoute>() == true) return
    navigate(NewTrackingRoute())
}

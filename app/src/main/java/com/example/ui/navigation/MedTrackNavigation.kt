package com.example.ui.navigation

import android.net.Uri
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.auth.FirebaseAuthService
import com.example.ui.MedTrackLandingScreen
import com.example.ui.emailverification.EmailVerificationScreen
import com.example.ui.forgotpassword.ForgotPasswordScreen
import com.example.ui.getstarted.GetStartedScreen
import com.example.ui.hub.create.CreateFamilyHubScreen
import com.example.ui.hub.dashboard.FamilyHiveDashboardScreen
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.hub.join.JoinFamilyHubScreen
import com.example.ui.hub.selection.HubSelectionScreen
import com.example.ui.hub.waitingroom.JoinRequestWaitingRoomScreen
import com.example.ui.login.LoginScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.profilesetup.ProfileSetupScreen
import com.example.ui.profilesetup.data.UserProfileRepository
import com.example.ui.splash.MedTrackSplashScreen
import kotlinx.coroutines.launch

/**
 * Screen Route identifiers for MedTrack.
 * Each screen is isolated in its own independent module.
 */
object MedTrackDestinations {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val LOGIN = "login"
    const val FORGOT_PASSWORD = "forgot_password"
    const val GET_STARTED = "get_started"
    const val EMAIL_VERIFICATION = "email_verification"
    const val PROFILE_SETUP = "profile_setup"
    const val PROFILE = "profile"
    const val HUB_SELECTION = "hub_selection"
    const val CREATE_FAMILY_HUB = "create_family_hub"
    const val JOIN_FAMILY_HUB = "join_family_hub"
    const val WAITING_ROOM = "waiting_room"
    const val FAMILY_HIVE = "family_hive"
    const val MEDICATION_HISTORY = "medication_history"
}

/**
 * Central Navigation Host for MedTrack.
 * Provides clean, decoupled navigation between modular screens with
 * smooth, accessible transitions.
 */
@Composable
fun MedTrackNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val coroutineScope = rememberCoroutineScope()

    fun performLogout() {
        FirebaseAuthService.Instance.signOut()
        UserProfileRepository.clearProfile()
        FamilyHubRepository.clearHub()
        com.example.ui.hub.dashboard.data.HubDashboardRepository.clearHubState()
        navController.navigate(MedTrackDestinations.HOME) {
            popUpTo(0) { inclusive = true }
            launchSingleTop = true
        }
    }

    NavHost(
        navController = navController,
        startDestination = MedTrackDestinations.SPLASH,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 80 },
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(durationMillis = 350))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -80 },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -80 },
                animationSpec = tween(durationMillis = 350)
            ) + fadeIn(animationSpec = tween(durationMillis = 350))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 80 },
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300))
        }
    ) {
        composable(
            route = "${MedTrackDestinations.SPLASH}?message={message}",
            arguments = listOf(
                navArgument("message") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val customMessage = backStackEntry.arguments?.getString("message")?.takeIf { it.isNotBlank() }
            MedTrackSplashScreen(
                customMessage = customMessage,
                onDestinationResolved = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(MedTrackDestinations.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = MedTrackDestinations.HOME) {
            MedTrackLandingScreen(
                onNavigateToLogin = {
                    navController.navigate(MedTrackDestinations.LOGIN)
                },
                onNavigateToGetStarted = {
                    navController.navigate(MedTrackDestinations.GET_STARTED)
                }
            )
        }

        composable(route = MedTrackDestinations.LOGIN) {
            LoginScreen(
                onNavigateBack = {
                    navController.navigate(MedTrackDestinations.HOME) {
                        popUpTo(MedTrackDestinations.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToGetStarted = {
                    navController.navigate(MedTrackDestinations.GET_STARTED)
                },
                onNavigateToForgotPassword = { enteredEmail ->
                    val encoded = if (enteredEmail.isNotBlank()) Uri.encode(enteredEmail) else ""
                    val route = if (encoded.isNotEmpty()) {
                        "${MedTrackDestinations.FORGOT_PASSWORD}?email=$encoded"
                    } else {
                        MedTrackDestinations.FORGOT_PASSWORD
                    }
                    navController.navigate(route)
                },
                onLoginSuccess = { targetRoute ->
                    navController.navigate(targetRoute) {
                        popUpTo(MedTrackDestinations.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = "${MedTrackDestinations.FORGOT_PASSWORD}?email={email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val prefilledEmail = backStackEntry.arguments?.getString("email") ?: ""
            ForgotPasswordScreen(
                initialEmail = prefilledEmail,
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(MedTrackDestinations.LOGIN) {
                            popUpTo(MedTrackDestinations.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(MedTrackDestinations.LOGIN) {
                        popUpTo(MedTrackDestinations.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = MedTrackDestinations.GET_STARTED) {
            GetStartedScreen(
                onNavigateHome = {
                    navController.navigate(MedTrackDestinations.HOME) {
                        popUpTo(MedTrackDestinations.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(MedTrackDestinations.LOGIN) {
                        popUpTo(MedTrackDestinations.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onAccountCreated = { emailAddress ->
                    val encodedEmail = if (emailAddress.isNotBlank()) Uri.encode(emailAddress) else ""
                    val route = if (encodedEmail.isNotEmpty()) {
                        "${MedTrackDestinations.EMAIL_VERIFICATION}?email=$encodedEmail"
                    } else {
                        MedTrackDestinations.EMAIL_VERIFICATION
                    }
                    navController.navigate(route) {
                        popUpTo(MedTrackDestinations.GET_STARTED) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${MedTrackDestinations.EMAIL_VERIFICATION}?email={email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val emailArg = backStackEntry.arguments?.getString("email")
            val decodedEmail = emailArg?.takeIf { it.isNotBlank() }?.let { Uri.decode(it) }
            EmailVerificationScreen(
                email = decodedEmail,
                onVerified = {
                    coroutineScope.launch {
                        val destination = UserSessionRouter.resolveCurrentDestination()
                        val route = UserSessionRouter.getRoute(destination)
                        navController.navigate(route) {
                            popUpTo(MedTrackDestinations.EMAIL_VERIFICATION) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                onResendVerification = {
                    // Verification email resend triggered
                }
            )
        }

        composable(route = MedTrackDestinations.PROFILE_SETUP) {
            ProfileSetupScreen(
                onProfileSaved = { _ ->
                    coroutineScope.launch {
                        val destination = UserSessionRouter.resolveCurrentDestination()
                        val route = UserSessionRouter.getRoute(destination)
                        navController.navigate(route) {
                            popUpTo(MedTrackDestinations.PROFILE_SETUP) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(route = MedTrackDestinations.HUB_SELECTION) {
            HubSelectionScreen(
                onNavigateToCreateHub = {
                    navController.navigate(MedTrackDestinations.CREATE_FAMILY_HUB)
                },
                onNavigateToJoinHub = {
                    navController.navigate(MedTrackDestinations.JOIN_FAMILY_HUB)
                },
                onNavigateToProfile = {
                    navController.navigate(MedTrackDestinations.PROFILE)
                },
                onLogout = {
                    performLogout()
                }
            )
        }

        composable(route = MedTrackDestinations.PROFILE) {
            ProfileScreen(
                onNavigateBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(MedTrackDestinations.HUB_SELECTION) {
                            launchSingleTop = true
                        }
                    }
                },
                onNavigateToCreateHub = {
                    navController.navigate(MedTrackDestinations.CREATE_FAMILY_HUB)
                },
                onNavigateToJoinHub = {
                    navController.navigate(MedTrackDestinations.JOIN_FAMILY_HUB)
                },
                onNavigateToHubDashboard = { hubId ->
                    navController.navigate(MedTrackDestinations.FAMILY_HIVE) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    performLogout()
                }
            )
        }

        composable(route = MedTrackDestinations.CREATE_FAMILY_HUB) {
            CreateFamilyHubScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = {
                    navController.navigate(MedTrackDestinations.PROFILE)
                },
                onLogout = {
                    performLogout()
                },
                onProceedToHub = {
                    navController.navigate(MedTrackDestinations.FAMILY_HIVE) {
                        popUpTo(MedTrackDestinations.HUB_SELECTION) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = MedTrackDestinations.JOIN_FAMILY_HUB) {
            JoinFamilyHubScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = {
                    navController.navigate(MedTrackDestinations.PROFILE)
                },
                onLogout = {
                    performLogout()
                },
                onJoinSuccess = {
                    navController.navigate(MedTrackDestinations.FAMILY_HIVE) {
                        popUpTo(MedTrackDestinations.HUB_SELECTION) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToWaitingRoom = { hubId, hubName, requestId ->
                    val encodedName = Uri.encode(hubName)
                    navController.navigate("${MedTrackDestinations.WAITING_ROOM}?hubId=$hubId&hubName=$encodedName&requestId=$requestId")
                }
            )
        }

        composable(
            route = "${MedTrackDestinations.WAITING_ROOM}?hubId={hubId}&hubName={hubName}&requestId={requestId}",
            arguments = listOf(
                navArgument("hubId") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("hubName") {
                    type = NavType.StringType
                    defaultValue = "Family Hub"
                },
                navArgument("requestId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val hubId = backStackEntry.arguments?.getString("hubId").orEmpty()
            val rawHubName = backStackEntry.arguments?.getString("hubName").orEmpty()
            val hubName = try { Uri.decode(rawHubName).ifBlank { "Family Hub" } } catch (_: Exception) { "Family Hub" }
            val requestId = backStackEntry.arguments?.getString("requestId").orEmpty()

            JoinRequestWaitingRoomScreen(
                hubId = hubId,
                hubName = hubName,
                requestId = requestId,
                onNavigateToHubSelection = {
                    navController.navigate(MedTrackDestinations.HUB_SELECTION) {
                        popUpTo(MedTrackDestinations.HUB_SELECTION) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToHubDashboard = {
                    navController.navigate(MedTrackDestinations.FAMILY_HIVE) {
                        popUpTo(MedTrackDestinations.HUB_SELECTION) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(route = MedTrackDestinations.FAMILY_HIVE) {
            FamilyHiveDashboardScreen(
                onNavigateToProfile = {
                    navController.navigate(MedTrackDestinations.PROFILE)
                },
                onNavigateToMedicationHistory = {
                    navController.navigate(MedTrackDestinations.MEDICATION_HISTORY)
                },
                onSignOut = {
                    performLogout()
                }
            )
        }

        composable(route = MedTrackDestinations.MEDICATION_HISTORY) {
            val currentHub = com.example.ui.hub.data.FamilyHubRepository.currentHub.collectAsState().value
            val hubId = currentHub?.hubId.orEmpty()
            com.example.ui.hub.activity.MedicationHistoryScreen(
                hubId = hubId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

package com.example.ui.navigation

import android.net.Uri
import com.example.data.auth.FirebaseAuthService
import com.example.ui.hub.data.FamilyHubRepository
import com.example.ui.profilesetup.data.UserProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Target destination based on user registration, verification, profile, and hub state.
 */
sealed class UserTargetDestination {
    data class EmailVerification(val email: String?) : UserTargetDestination()
    object ProfileSetup : UserTargetDestination()
    object HubSelection : UserTargetDestination()
    object FamilyHive : UserTargetDestination()
    object Home : UserTargetDestination()
}

/**
 * Centralized router responsible for enforcing user onboarding and authentication flows:
 * 1. If not logged in -> Home / Login
 * 2. If registered but email not verified -> Email Verification Screen
 * 3. If registered and email verified, but profile not created -> Profile Setup Screen
 * 4. If registered, email verified, profile created, but not in a hub -> Hub Selection Screen
 * 5. If registered, email verified, profile created, and in a hub -> Respective Family Hive
 */
object UserSessionRouter {

    suspend fun resolveCurrentDestination(): UserTargetDestination = withContext(Dispatchers.IO) {
        val currentUser = FirebaseAuthService.Instance.currentUser ?: return@withContext UserTargetDestination.Home

        // 1. Refresh user to check latest email verification status
        try {
            currentUser.reload().await()
        } catch (_: Exception) {
            // Fallback if reload is throttled or offline
        }

        // Rule 1: Email not verified
        if (!currentUser.isEmailVerified) {
            return@withContext UserTargetDestination.EmailVerification(currentUser.email)
        }

        // Rule 2: Profile not created or incomplete
        val profile = UserProfileRepository.loadProfile()
        if (profile == null || !profile.isCompleted || profile.name.isBlank()) {
            return@withContext UserTargetDestination.ProfileSetup
        }

        // Rule 3: Not in any hub
        val hub = FamilyHubRepository.loadUserHub()
        if (hub == null || hub.hubId.isBlank()) {
            return@withContext UserTargetDestination.HubSelection
        }

        // Rule 4: Profile complete and in a hub -> Respective Hub
        return@withContext UserTargetDestination.FamilyHive
    }

    /**
     * Converts a UserTargetDestination into a Navigation route string.
     */
    fun getRoute(destination: UserTargetDestination): String {
        return when (destination) {
            is UserTargetDestination.EmailVerification -> {
                val encoded = if (!destination.email.isNullOrBlank()) Uri.encode(destination.email) else ""
                if (encoded.isNotBlank()) {
                    "${MedTrackDestinations.EMAIL_VERIFICATION}?email=$encoded"
                } else {
                    MedTrackDestinations.EMAIL_VERIFICATION
                }
            }
            is UserTargetDestination.ProfileSetup -> MedTrackDestinations.PROFILE_SETUP
            is UserTargetDestination.HubSelection -> MedTrackDestinations.HUB_SELECTION
            is UserTargetDestination.FamilyHive -> MedTrackDestinations.FAMILY_HIVE
            is UserTargetDestination.Home -> MedTrackDestinations.HOME
        }
    }
}

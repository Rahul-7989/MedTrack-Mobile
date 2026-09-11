package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.auth.FirebaseAuthService
import com.example.ui.components.MedTrackLogo
import com.example.ui.navigation.MedTrackDestinations
import com.example.ui.navigation.UserSessionRouter
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.MedTrackTextPrimary
import com.example.ui.theme.MedTrackTextSecondary
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory

import kotlinx.coroutines.delay

/**
 * Startup Splash Screen for MedTrack.
 * Checks active session on launch / login:
 * - If logged out: immediately transitions to Home/Landing page.
 * - If logged in: resolves user state and navigates directly to their Hub Dashboard
 *   (or active onboarding step) without ever displaying the landing page or a white screen delay.
 */
@Composable
fun MedTrackSplashScreen(
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    onDestinationResolved: (String) -> Unit
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350)
        )
    }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuthService.Instance.currentUser
        if (currentUser == null) {
            // User is logged out -> route to Home page
            onDestinationResolved(MedTrackDestinations.HOME)
        } else {
            // User is logged in -> resolve destination with smooth loading
            val destination = UserSessionRouter.resolveCurrentDestination()
            val route = UserSessionRouter.getRoute(destination)
            // Ensure minimum display time for a smooth transition experience
            delay(500)
            onDestinationResolved(route)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WarmIvory,
                        Color(0xFFF7F2EC),
                        WarmCream
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            MedTrackLogo(
                sizeDp = 100.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "MedTrack",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = MedTrackTextPrimary,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = customMessage ?: "The family medication companion",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MedTrackTextSecondary,
                letterSpacing = 0.2.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = DustyTeal,
                strokeWidth = 2.5.dp
            )
        }
    }
}

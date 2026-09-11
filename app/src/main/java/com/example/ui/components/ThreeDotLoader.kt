package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Scoped palette token for loader
private val ColorWarmIvory = Color(0xFFFAF4EC)

/**
 * Minimal Three-Dot Button Loader.
 *
 * Replaces circular spinners inside action buttons with 3 sequentially pulsing dots:
 * 1. Dot 1 pulses to bright opacity (1.0)
 * 2. Dot 2 pulses to bright opacity (1.0)
 * 3. Dot 3 pulses to bright opacity (1.0)
 * Loop restarts seamlessly every 720ms.
 *
 * Strict visual rules:
 * - No vertical translation / bouncing
 * - No position shift / rotation
 * - Retains exact parent button dimensions & background
 * - Inactive opacity: 0.35f, Active opacity: 1.0f
 */
@Composable
fun ThreeDotButtonLoader(
    modifier: Modifier = Modifier,
    dotColor: Color = ColorWarmIvory,
    dotSize: Dp = 5.5.dp,
    dotSpacing: Dp = 6.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ThreeDotPulseTransition")

    // Dot 1 pulse: peak at 180ms
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 720
                0.35f at 0
                1.0f at 180
                0.35f at 360
                0.35f at 720
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot1Alpha"
    )

    // Dot 2 pulse: peak at 360ms (180ms delay)
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 720
                0.35f at 0
                0.35f at 180
                1.0f at 360
                0.35f at 540
                0.35f at 720
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot2Alpha"
    )

    // Dot 3 pulse: peak at 540ms (360ms delay)
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 720
                0.35f at 0
                0.35f at 360
                1.0f at 540
                0.35f at 720
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot3Alpha"
    )

    Row(
        modifier = modifier
            .height(20.dp)
            .semantics {
                contentDescription = "Loading, please wait"
            }
            .testTag("three_dot_button_loader"),
        horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot 1
        Box(
            modifier = Modifier
                .size(dotSize)
                .alpha(dot1Alpha)
                .clip(CircleShape)
                .background(dotColor)
        )

        // Dot 2
        Box(
            modifier = Modifier
                .size(dotSize)
                .alpha(dot2Alpha)
                .clip(CircleShape)
                .background(dotColor)
        )

        // Dot 3
        Box(
            modifier = Modifier
                .size(dotSize)
                .alpha(dot3Alpha)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

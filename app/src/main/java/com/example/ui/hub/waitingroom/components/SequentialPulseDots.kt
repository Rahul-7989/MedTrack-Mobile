package com.example.ui.hub.waitingroom.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val ColorDustyTeal = Color(0xFF72B5BA)

/**
 * Subtle, restrained sequential pulse indicator representing a pending request state.
 * Smoothly pulses three small dots with gentle opacity animation.
 */
@Composable
fun SequentialPulseDots(
    modifier: Modifier = Modifier,
    dotColor: Color = ColorDustyTeal,
    dotSize: Dp = 8.dp,
    spacing: Dp = 10.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "DotsPulseTransition")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                0.35f at 0
                1f at 300
                0.35f at 700
                0.35f at 1500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot1Alpha"
    )

    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                0.35f at 250
                1f at 550
                0.35f at 950
                0.35f at 1500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot2Alpha"
    )

    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                0.35f at 500
                1f at 800
                0.35f at 1200
                0.35f at 1500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "Dot3Alpha"
    )

    Row(
        modifier = modifier.testTag("waiting_room_sequential_pulse_dots"),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .alpha(dot1Alpha)
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .alpha(dot2Alpha)
                .clip(CircleShape)
                .background(dotColor)
        )
        Box(
            modifier = Modifier
                .size(dotSize)
                .alpha(dot3Alpha)
                .clip(CircleShape)
                .background(dotColor)
        )
    }
}

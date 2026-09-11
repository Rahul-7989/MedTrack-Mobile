package com.example.ui.hub.join.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

// Scoped palette tokens for background geometry
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorWarmCream = Color(0xFFF3E6D5)

/**
 * Ambient background geometry for Join Family Hub screen.
 */
@Composable
fun JoinHubBackgroundShapes(
    alpha: Float,
    isReducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "JoinHubShapesTransition")

    val breathe by infiniteTransition.animateFloat(
        initialValue = if (isReducedMotion) 1f else 0.96f,
        targetValue = if (isReducedMotion) 1f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(6500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShapeScale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Top-left Dusty Teal ambient cloud
        drawCircle(
            color = ColorDustyTeal.copy(alpha = 0.16f * alpha),
            radius = w * 0.44f * breathe,
            center = Offset(w * 0.12f, h * 0.10f)
        )

        // Top-right Warm Cream organic circle
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.60f * alpha),
            radius = w * 0.35f,
            center = Offset(w * 0.95f, h * 0.18f)
        )

        // Bottom-right Pale Teal glow
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.22f * alpha),
            radius = w * 0.38f * breathe,
            center = Offset(w * 0.88f, h * 0.86f)
        )

        // Bottom-left Warm Amber subtle warmth
        drawCircle(
            color = ColorWarmAmber.copy(alpha = 0.12f * alpha),
            radius = w * 0.30f,
            center = Offset(w * 0.10f, h * 0.84f)
        )
    }
}

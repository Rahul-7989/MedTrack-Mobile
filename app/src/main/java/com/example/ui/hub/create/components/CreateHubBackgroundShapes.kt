package com.example.ui.hub.create.components

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
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmCream = Color(0xFFF3E6D5)

/**
 * Ambient background geometry for Create Family Hub screen.
 */
@Composable
fun CreateHubBackgroundShapes(
    alpha: Float,
    isReducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CreateHubShapesTransition")

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

        // Top-right Warm Amber ambient cloud
        drawCircle(
            color = ColorWarmAmber.copy(alpha = 0.16f * alpha),
            radius = w * 0.44f * breathe,
            center = Offset(w * 0.90f, h * 0.10f)
        )

        // Top-left Warm Cream organic circle
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.60f * alpha),
            radius = w * 0.35f,
            center = Offset(w * 0.05f, h * 0.18f)
        )

        // Bottom-left Burnt Apricot glow
        drawCircle(
            color = ColorBurntApricot.copy(alpha = 0.12f * alpha),
            radius = w * 0.38f * breathe,
            center = Offset(w * 0.12f, h * 0.86f)
        )

        // Bottom-right Pale Teal gentle balance
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.18f * alpha),
            radius = w * 0.32f,
            center = Offset(w * 0.88f, h * 0.84f)
        )
    }
}

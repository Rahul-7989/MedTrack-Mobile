package com.example.ui.hub.selection.components

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
import androidx.compose.ui.graphics.drawscope.DrawScope

// Scoped palette tokens for background geometry
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorWarmCream = Color(0xFFF3E6D5)

/**
 * Ambient, warm geometric background shapes for the Hub Selection page.
 */
@Composable
fun HubSelectionBackgroundShapes(
    alpha: Float,
    isReducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HubShapesTransition")

    val breathe1 by infiniteTransition.animateFloat(
        initialValue = if (isReducedMotion) 1f else 0.96f,
        targetValue = if (isReducedMotion) 1f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Shape1Scale"
    )

    val breathe2 by infiniteTransition.animateFloat(
        initialValue = if (isReducedMotion) 1f else 1.03f,
        targetValue = if (isReducedMotion) 1f else 0.97f,
        animationSpec = infiniteRepeatable(
            animation = tween(7500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Shape2Scale"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Top-right soft Dusty Teal ambient cloud
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.22f * alpha),
            radius = w * 0.45f * breathe1,
            center = Offset(w * 0.92f, h * 0.08f)
        )

        // Top-left Warm Cream organic circle
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.55f * alpha),
            radius = w * 0.32f * breathe2,
            center = Offset(w * 0.08f, h * 0.16f)
        )

        // Bottom-left Warm Amber subtle glow
        drawCircle(
            color = ColorWarmAmber.copy(alpha = 0.14f * alpha),
            radius = w * 0.40f * breathe2,
            center = Offset(w * 0.10f, h * 0.88f)
        )

        // Bottom-right Burnt Apricot / Dusty Teal gentle blend
        drawCircle(
            color = ColorBurntApricot.copy(alpha = 0.10f * alpha),
            radius = w * 0.35f * breathe1,
            center = Offset(w * 0.85f, h * 0.85f)
        )
    }
}

package com.example.ui.login.components

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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Isolated geometric background for the MedTrack Login Page.
 *
 * Implements an atmospheric, editorial composition using oversized fragments of
 * MedTrack's geometric brand language (rounded hexagons, overlapping soft shapes,
 * connection arcs) rendered in low-opacity Warm Cream, Pale Teal, Warm Amber,
 * Burnt Apricot, and Dusty Teal on a Warm Ivory canvas.
 *
 * Designed to subtly suggest "connection and entering the family hub" without
 * cluttering the screen or reproducing the actual logo.
 */
@Composable
fun LoginBackgroundShapes(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    isReducedMotion: Boolean = false
) {
    // Ultra-subtle, slow floating motions (8-12s cycles)
    val infiniteTransition = rememberInfiniteTransition(label = "LoginBgFloating")

    val floatOffset1 by if (isReducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 9500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "FloatShape1"
        )
    }

    val floatOffset2 by if (isReducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 10f,
            targetValue = -10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 11500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "FloatShape2"
        )
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        if (alpha <= 0.01f) return@Canvas

        val w = size.width
        val h = size.height

        // Palette definitions with low opacities to keep login form as the distinct hero
        val colorWarmCream = Color(0xFFF3E6D5).copy(alpha = 0.50f * alpha)
        val colorPaleTeal = Color(0xFFA9CED0).copy(alpha = 0.28f * alpha)
        val colorDustyTeal = Color(0xFF72B5BA).copy(alpha = 0.20f * alpha)
        val colorWarmAmber = Color(0xFFE5A23C).copy(alpha = 0.18f * alpha)
        val colorBurntApricot = Color(0xFFD88B3D).copy(alpha = 0.22f * alpha)

        // 1. Large Pale Teal & Warm Cream overlapping background foundation on the left/top
        // Suggests family connection and hub gathering
        drawCircle(
            color = colorWarmCream,
            radius = w.coerceAtLeast(h) * 0.42f,
            center = Offset(w * 0.10f + floatOffset1 * 0.5f, h * 0.20f + floatOffset2 * 0.5f)
        )

        drawCircle(
            color = colorPaleTeal,
            radius = w.coerceAtLeast(h) * 0.28f,
            center = Offset(w * 0.05f + floatOffset2, h * 0.45f + floatOffset1)
        )

        // 2. Oversized Dusty Teal rounded geometric hexagon fragment (partially cropped on left/middle)
        val tealHexCenter = Offset(w * 0.22f + floatOffset1, h * 0.38f + floatOffset2)
        val tealHexWidth = (w * 0.40f).coerceIn(200f, 480f)
        val tealHexHeight = tealHexWidth * 1.15f
        drawRoundedHexagon(
            cx = tealHexCenter.x,
            cy = tealHexCenter.y,
            width = tealHexWidth,
            height = tealHexHeight,
            radius = tealHexWidth * 0.12f,
            color = colorDustyTeal
        )

        // 3. Warm Amber geometric hexagon form (floating gracefully above Dusty Teal)
        val amberHexCenter = Offset(w * 0.14f - floatOffset2 * 0.8f, h * 0.18f + floatOffset1 * 0.8f)
        val amberHexWidth = (w * 0.26f).coerceIn(130f, 300f)
        val amberHexHeight = amberHexWidth * 1.15f
        drawRoundedHexagon(
            cx = amberHexCenter.x,
            cy = amberHexCenter.y,
            width = amberHexWidth,
            height = amberHexHeight,
            radius = amberHexWidth * 0.12f,
            color = colorWarmAmber
        )

        // 4. Smaller Burnt Apricot geometric form anchoring lower-left
        val apricotCenter = Offset(w * 0.18f + floatOffset2, h * 0.72f - floatOffset1)
        val apricotWidth = (w * 0.18f).coerceIn(90f, 210f)
        val apricotHeight = apricotWidth * 1.15f
        drawRoundedHexagon(
            cx = apricotCenter.x,
            cy = apricotCenter.y,
            width = apricotWidth,
            height = apricotHeight,
            radius = apricotWidth * 0.12f,
            color = colorBurntApricot
        )

        // 5. Delicate Pale Teal and Warm Cream ambient glow on bottom-right behind/beside the login panel
        drawCircle(
            color = colorPaleTeal.copy(alpha = 0.15f * alpha),
            radius = w.coerceAtLeast(h) * 0.32f,
            center = Offset(w * 0.90f - floatOffset1, h * 0.88f - floatOffset2)
        )

        drawCircle(
            color = colorWarmCream.copy(alpha = 0.40f * alpha),
            radius = w.coerceAtLeast(h) * 0.22f,
            center = Offset(w * 0.82f + floatOffset2 * 0.6f, h * 0.15f - floatOffset1 * 0.6f)
        )
    }
}

/**
 * Draws a rounded hexagon path with fillet arcs at each corner.
 */
private fun DrawScope.drawRoundedHexagon(
    cx: Float,
    cy: Float,
    width: Float,
    height: Float,
    radius: Float,
    color: Color
) {
    val pts = listOf(
        Offset(cx, cy - height / 2f),
        Offset(cx + width / 2f, cy - height / 4f),
        Offset(cx + width / 2f, cy + height / 4f),
        Offset(cx, cy + height / 2f),
        Offset(cx - width / 2f, cy + height / 4f),
        Offset(cx - width / 2f, cy - height / 4f)
    )

    val n = pts.size
    val path = Path()
    val r = radius.coerceAtMost(width * 0.15f)

    for (i in 0 until n) {
        val pPrev = pts[(i - 1 + n) % n]
        val pCurr = pts[i]
        val pNext = pts[(i + 1) % n]

        val v1x = pPrev.x - pCurr.x
        val v1y = pPrev.y - pCurr.y
        val len1 = hypot(v1x, v1y).coerceAtLeast(0.001f)
        val u1x = v1x / len1
        val u1y = v1y / len1

        val v2x = pNext.x - pCurr.x
        val v2y = pNext.y - pCurr.y
        val len2 = hypot(v2x, v2y).coerceAtLeast(0.001f)
        val u2x = v2x / len2
        val u2y = v2y / len2

        val startX = pCurr.x + u1x * r
        val startY = pCurr.y + u1y * r
        val endX = pCurr.x + u2x * r
        val endY = pCurr.y + u2y * r

        if (i == 0) {
            path.moveTo(startX, startY)
        } else {
            path.lineTo(startX, startY)
        }
        path.quadraticTo(pCurr.x, pCurr.y, endX, endY)
    }
    path.close()
    drawPath(path = path, color = color)
}

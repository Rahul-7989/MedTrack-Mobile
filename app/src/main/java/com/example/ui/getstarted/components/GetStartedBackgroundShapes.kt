package com.example.ui.getstarted.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.hypot

// Scoped MedTrack artistic palette for Get Started page
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorWarmCream = Color(0xFFF3E6D5)

/**
 * Subtle abstract geometric background elements inspired by MedTrack's visual identity.
 * Features large soft partial circles extending beyond the viewport, rounded hexagons,
 * and gentle overlapping forms in low opacity on a Warm Ivory canvas.
 * Zero medical clichés.
 */
@Composable
fun GetStartedBackgroundShapes(
    alpha: Float = 1f,
    isReducedMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "GetStartedBgMotion")

    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShapeOffset1"
    )

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ShapeOffset2"
    )

    val currentOffset1 = if (isReducedMotion) 0f else floatOffset1
    val currentOffset2 = if (isReducedMotion) 0f else floatOffset2

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Large Top-right Warm Cream & Warm Amber forms extending beyond viewport
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.45f * alpha),
            radius = w.coerceAtLeast(h) * 0.38f,
            center = Offset(w * 0.95f, h * 0.08f + currentOffset1)
        )
        drawCircle(
            color = ColorWarmAmber.copy(alpha = 0.12f * alpha),
            radius = w.coerceAtLeast(h) * 0.22f,
            center = Offset(w * 0.90f, h * 0.16f + currentOffset2)
        )

        // 2. Large Bottom-left gentle teal presence extending beyond viewport
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.20f * alpha),
            radius = w.coerceAtLeast(h) * 0.35f,
            center = Offset(w * 0.05f, h * 0.92f + currentOffset2)
        )
        drawCircle(
            color = ColorDustyTeal.copy(alpha = 0.12f * alpha),
            radius = w.coerceAtLeast(h) * 0.20f,
            center = Offset(w * 0.14f, h * 0.84f + currentOffset1)
        )

        // 3. Rounded hexagon fragment floating on top-left
        val hexWidth = (w * 0.28f).coerceIn(120f, 260f)
        drawRoundedHexagon(
            cx = w * 0.12f + currentOffset1,
            cy = h * 0.22f + currentOffset2,
            width = hexWidth,
            height = hexWidth * 1.15f,
            radius = hexWidth * 0.12f,
            color = ColorBurntApricot.copy(alpha = 0.12f * alpha)
        )

        // 4. Subtle center-right soft hexagonal pill
        val pillWidth = (w * 0.20f).coerceIn(90f, 180f)
        drawRoundedHexagon(
            cx = w * 0.88f - currentOffset2,
            cy = h * 0.65f + currentOffset1,
            width = pillWidth,
            height = pillWidth * 1.15f,
            radius = pillWidth * 0.12f,
            color = ColorPaleTeal.copy(alpha = 0.15f * alpha)
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

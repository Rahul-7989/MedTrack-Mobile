package com.example.ui.emailverification.components

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.hypot

// Scoped MedTrack palette for Email Verification background
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorWarmCream = Color(0xFFF3E6D5)

/**
 * Subtle abstract geometric background elements for the Email Verification page.
 * Uses low-opacity oversized geometric shapes extending beyond the viewport:
 * - Large partial circles
 * - Soft organic curves
 * - Rounded hexagons
 * Creates depth behind the card while remaining quiet and non-distracting.
 */
@Composable
fun EmailVerificationBackgroundShapes(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    isReducedMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "VerifyBgMotion")

    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VerifyShapeOffset1"
    )

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 7f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "VerifyShapeOffset2"
    )

    val currentOffset1 = if (isReducedMotion) 0f else floatOffset1
    val currentOffset2 = if (isReducedMotion) 0f else floatOffset2

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val maxDim = w.coerceAtLeast(h)

        // 1. Large Top-right Warm Cream & Warm Amber forms extending beyond viewport
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.45f * alpha),
            radius = maxDim * 0.38f,
            center = Offset(w * 0.94f, h * 0.08f + currentOffset1)
        )
        drawCircle(
            color = ColorWarmAmber.copy(alpha = 0.10f * alpha),
            radius = maxDim * 0.22f,
            center = Offset(w * 0.88f, h * 0.15f + currentOffset2)
        )

        // 2. Large Bottom-left gentle teal presence extending beyond viewport
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.18f * alpha),
            radius = maxDim * 0.35f,
            center = Offset(w * 0.06f, h * 0.90f + currentOffset2)
        )
        drawCircle(
            color = ColorDustyTeal.copy(alpha = 0.10f * alpha),
            radius = maxDim * 0.20f,
            center = Offset(w * 0.14f, h * 0.82f + currentOffset1)
        )

        // 3. Rounded hexagon fragment floating on top-left
        val hexWidth = (w * 0.26f).coerceIn(110f, 240f)
        drawRoundedHexagon(
            cx = w * 0.14f + currentOffset1,
            cy = h * 0.20f + currentOffset2,
            width = hexWidth,
            height = hexWidth * 1.15f,
            radius = hexWidth * 0.12f,
            color = ColorBurntApricot.copy(alpha = 0.10f * alpha)
        )

        // 4. Subtle mid-right soft hexagonal pill
        val pillWidth = (w * 0.18f).coerceIn(80f, 160f)
        drawRoundedHexagon(
            cx = w * 0.90f - currentOffset2,
            cy = h * 0.65f + currentOffset1,
            width = pillWidth,
            height = pillWidth * 1.15f,
            radius = pillWidth * 0.12f,
            color = ColorPaleTeal.copy(alpha = 0.14f * alpha)
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

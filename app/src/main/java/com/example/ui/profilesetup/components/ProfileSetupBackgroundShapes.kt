package com.example.ui.profilesetup.components

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

// Scoped MedTrack palette for Profile Setup background
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorWarmCream = Color(0xFFF3E6D5)

/**
 * Subtle abstract geometric background elements for the Profile Setup page.
 * Uses low-opacity oversized geometric shapes extending beyond the viewport:
 * - Large partial circles
 * - Soft organic curves
 * - Rounded hexagons
 * Creates tactile depth behind the card while remaining serene and unobtrusive.
 */
@Composable
fun ProfileSetupBackgroundShapes(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    isReducedMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ProfileBgMotion")

    val floatOffset1 by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ProfileShapeOffset1"
    )

    val floatOffset2 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ProfileShapeOffset2"
    )

    val currentOffset1 = if (isReducedMotion) 0f else floatOffset1
    val currentOffset2 = if (isReducedMotion) 0f else floatOffset2

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val maxDim = w.coerceAtLeast(h)

        // 1. Large Top-left Warm Cream & Dusty Teal forms
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.50f * alpha),
            radius = maxDim * 0.36f,
            center = Offset(w * 0.08f, h * 0.05f + currentOffset1)
        )
        drawCircle(
            color = ColorDustyTeal.copy(alpha = 0.10f * alpha),
            radius = maxDim * 0.20f,
            center = Offset(w * 0.16f, h * 0.12f + currentOffset2)
        )

        // 2. Large Bottom-right Warm Amber & Pale Teal presence
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.20f * alpha),
            radius = maxDim * 0.38f,
            center = Offset(w * 0.92f, h * 0.92f + currentOffset2)
        )
        drawCircle(
            color = ColorWarmAmber.copy(alpha = 0.09f * alpha),
            radius = maxDim * 0.22f,
            center = Offset(w * 0.84f, h * 0.82f + currentOffset1)
        )

        // 3. Rounded hexagon fragment floating on mid-right
        val hexWidth = (w * 0.24f).coerceIn(100f, 220f)
        drawRoundedHexagon(
            cx = w * 0.88f + currentOffset1,
            cy = h * 0.30f + currentOffset2,
            width = hexWidth,
            height = hexWidth * 1.15f,
            radius = hexWidth * 0.12f,
            color = ColorBurntApricot.copy(alpha = 0.09f * alpha)
        )

        // 4. Subtle lower-left soft hexagonal pill
        val pillWidth = (w * 0.18f).coerceIn(80f, 160f)
        drawRoundedHexagon(
            cx = w * 0.10f - currentOffset2,
            cy = h * 0.72f + currentOffset1,
            width = pillWidth,
            height = pillWidth * 1.15f,
            radius = pillWidth * 0.12f,
            color = ColorPaleTeal.copy(alpha = 0.12f * alpha)
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

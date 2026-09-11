package com.example.ui.login.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.hypot

/**
 * Editorial abstract geometric composition for the left side of desktop/wide screens.
 * Visually communicates "entering the family hub" through pure geometry, color, and spacing:
 * - Layers of Warm Amber, Dusty Teal, Burnt Apricot, Pale Teal, and Warm Cream.
 * - Subtly interlocking curved forms suggesting connection and family togetherness.
 * - Contains zero text, zero cliches, and zero logos.
 */
@Composable
fun LoginEditorialArtwork(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    isReducedMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArtworkFloat")

    val floatOffset by if (isReducedMotion) {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = -6f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 8500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "ArtFloat"
        )
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            if (alpha <= 0.01f) return@Canvas

            val w = size.width
            val h = size.height
            val cx = w * 0.52f
            val cy = h * 0.50f + floatOffset

            // Palette
            val colorDustyTeal = Color(0xFF72B5BA).copy(alpha = 0.55f * alpha)
            val colorPaleTeal = Color(0xFFA9CED0).copy(alpha = 0.45f * alpha)
            val colorWarmAmber = Color(0xFFE5A23C).copy(alpha = 0.55f * alpha)
            val colorBurntApricot = Color(0xFFD88B3D).copy(alpha = 0.50f * alpha)
            val colorWarmCream = Color(0xFFF3E6D5).copy(alpha = 0.85f * alpha)

            val baseSize = w.coerceAtMost(h) * 0.75f

            // 1. Soft Warm Cream ambient back-disk
            drawCircle(
                color = colorWarmCream,
                radius = baseSize * 0.48f,
                center = Offset(cx - baseSize * 0.05f, cy + baseSize * 0.05f)
            )

            // 2. Large Dusty Teal rounded hexagon (center-right of the decorative artwork)
            drawRoundedHex(
                cx = cx + baseSize * 0.12f,
                cy = cy + baseSize * 0.08f,
                width = baseSize * 0.62f,
                height = baseSize * 0.72f,
                radius = baseSize * 0.09f,
                color = colorDustyTeal
            )

            // 3. Warm Amber rounded hexagon (top-left of decorative piece)
            drawRoundedHex(
                cx = cx - baseSize * 0.16f,
                cy = cy - baseSize * 0.16f,
                width = baseSize * 0.48f,
                height = baseSize * 0.56f,
                radius = baseSize * 0.08f,
                color = colorWarmAmber
            )

            // 4. Burnt Apricot rounded hexagon (bottom-left anchor)
            drawRoundedHex(
                cx = cx - baseSize * 0.20f,
                cy = cy + baseSize * 0.22f,
                width = baseSize * 0.32f,
                height = baseSize * 0.38f,
                radius = baseSize * 0.06f,
                color = colorBurntApricot
            )

            // 5. Pale Teal connecting orbital loop / arc (representing family gathering)
            val arcPath = Path().apply {
                moveTo(cx - baseSize * 0.30f, cy + baseSize * 0.05f)
                cubicTo(
                    cx - baseSize * 0.20f, cy - baseSize * 0.35f,
                    cx + baseSize * 0.20f, cy - baseSize * 0.35f,
                    cx + baseSize * 0.32f, cy - baseSize * 0.05f
                )
            }
            drawPath(
                path = arcPath,
                color = colorPaleTeal,
                style = Stroke(width = 6f, cap = StrokeCap.Round)
            )

            // 6. Inner gentle Pale Teal disc in the overlapping junction
            drawCircle(
                color = colorPaleTeal.copy(alpha = 0.35f * alpha),
                radius = baseSize * 0.16f,
                center = Offset(cx, cy)
            )
        }
    }
}

private fun DrawScope.drawRoundedHex(
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

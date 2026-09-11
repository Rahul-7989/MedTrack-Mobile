package com.example.ui.components

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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BurntApricot
import com.example.ui.theme.DustyTeal
import com.example.ui.theme.PaleTeal
import com.example.ui.theme.WarmAmber
import com.example.ui.theme.WarmCream
import com.example.ui.theme.WarmIvory

/**
 * Custom artistic background for MedTrack.
 * Features large, soft, low-opacity organic geometric shapes, subtle hexagonal forms,
 * gentle curves, and small warm amber/burnt apricot accents that sit quietly behind the hero.
 */
@Composable
fun BackgroundOrganicShapes(
    modifier: Modifier = Modifier,
    isReducedMotion: Boolean = false,
    revealProgress: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "BackgroundFloatTransition")

    val floatAnimA by infiniteTransition.animateFloat(
        initialValue = if (isReducedMotion) 0f else -6f,
        targetValue = if (isReducedMotion) 0f else 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatA"
    )

    val floatAnimB by infiniteTransition.animateFloat(
        initialValue = if (isReducedMotion) 0f else 8f,
        targetValue = if (isReducedMotion) 0f else -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatB"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        // 1. Base canvas fill: Warm Ivory
        drawRect(color = WarmIvory)

        val alphaMultiplier = revealProgress.coerceIn(0f, 1f)

        // 2. Large top-right organic Warm Cream shape with soft gradient
        val topBlobPath = Path().apply {
            moveTo(w * 0.42f, 0f)
            cubicTo(
                w * 0.62f, h * 0.04f + floatAnimA,
                w * 0.78f, h * 0.12f,
                w * 0.88f, h * 0.22f + floatAnimB
            )
            cubicTo(
                w * 0.98f, h * 0.32f,
                w * 1.05f, h * 0.22f,
                w, h * 0.40f
            )
            lineTo(w, 0f)
            close()
        }
        drawPath(
            path = topBlobPath,
            color = WarmCream.copy(alpha = 0.65f * alphaMultiplier),
            style = Fill
        )

        // 3. Subtle Pale Teal organic shape overlapping top-right
        val paleTealTopPath = Path().apply {
            moveTo(w * 0.68f, 0f)
            cubicTo(
                w * 0.75f, h * 0.08f + floatAnimB,
                w * 0.85f, h * 0.14f,
                w, h * 0.16f
            )
            lineTo(w, 0f)
            close()
        }
        drawPath(
            path = paleTealTopPath,
            color = PaleTeal.copy(alpha = 0.32f * alphaMultiplier),
            style = Fill
        )

        // 4. Large bottom-left soft Warm Cream & Pale Teal foundation
        val bottomBlobPath = Path().apply {
            moveTo(0f, h * 0.58f)
            cubicTo(
                w * 0.15f, h * 0.62f + floatAnimB,
                w * 0.32f, h * 0.74f,
                w * 0.36f, h * 0.88f + floatAnimA
            )
            cubicTo(
                w * 0.40f, h * 0.96f,
                w * 0.25f, h * 1.05f,
                w * 0.18f, h
            )
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = bottomBlobPath,
            color = WarmCream.copy(alpha = 0.55f * alphaMultiplier),
            style = Fill
        )

        val paleTealBottomPath = Path().apply {
            moveTo(0f, h * 0.76f)
            cubicTo(
                w * 0.12f, h * 0.80f + floatAnimA,
                w * 0.22f, h * 0.90f,
                w * 0.14f, h
            )
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = paleTealBottomPath,
            color = PaleTeal.copy(alpha = 0.28f * alphaMultiplier),
            style = Fill
        )

        // 5. Soft organic rounded hexagon / pebble shape in upper-mid left (Warm Amber accent)
        val amberCenterX = w * 0.14f
        val amberCenterY = h * 0.26f + floatAnimA
        val amberRadius = (w * 0.065f).coerceIn(24.dp.toPx(), 48.dp.toPx())
        val softHexPath = Path().apply {
            val r = amberRadius
            moveTo(amberCenterX, amberCenterY - r)
            cubicTo(amberCenterX + r * 0.7f, amberCenterY - r, amberCenterX + r, amberCenterY - r * 0.4f, amberCenterX + r, amberCenterY)
            cubicTo(amberCenterX + r, amberCenterY + r * 0.4f, amberCenterX + r * 0.5f, amberCenterY + r, amberCenterX, amberCenterY + r)
            cubicTo(amberCenterX - r * 0.6f, amberCenterY + r, amberCenterX - r, amberCenterY + r * 0.6f, amberCenterX - r, amberCenterY)
            cubicTo(amberCenterX - r, amberCenterY - r * 0.5f, amberCenterX - r * 0.6f, amberCenterY - r, amberCenterX, amberCenterY - r)
            close()
        }
        drawPath(
            path = softHexPath,
            color = WarmAmber.copy(alpha = 0.18f * alphaMultiplier),
            style = Fill
        )

        // 6. Delicate Burnt Apricot accent ring/pebble in lower-mid right
        val apricotCenterX = w * 0.86f
        val apricotCenterY = h * 0.72f + floatAnimB
        val apricotRadius = (w * 0.05f).coerceIn(18.dp.toPx(), 36.dp.toPx())
        drawCircle(
            color = BurntApricot.copy(alpha = 0.14f * alphaMultiplier),
            radius = apricotRadius,
            center = Offset(apricotCenterX, apricotCenterY)
        )
        drawCircle(
            color = BurntApricot.copy(alpha = 0.22f * alphaMultiplier),
            radius = apricotRadius * 0.75f,
            center = Offset(apricotCenterX, apricotCenterY),
            style = Stroke(width = 1.5.dp.toPx())
        )

        // 7. Subtle Dusty Teal floating pebble accent in mid-right
        val tealCenterX = w * 0.88f
        val tealCenterY = h * 0.38f + floatAnimA * 0.7f
        val tealRadius = (w * 0.038f).coerceIn(14.dp.toPx(), 28.dp.toPx())
        drawCircle(
            color = DustyTeal.copy(alpha = 0.12f * alphaMultiplier),
            radius = tealRadius,
            center = Offset(tealCenterX, tealCenterY)
        )

        // 8. Soft Warm Cream floating pebble near center bottom
        val centerBottomBlob = Path().apply {
            val cx = w * 0.52f
            val cy = h * 0.88f + floatAnimB * 0.5f
            val r = (w * 0.10f).coerceIn(36.dp.toPx(), 72.dp.toPx())
            addOval(androidx.compose.ui.geometry.Rect(cx - r, cy - r * 0.5f, cx + r, cy + r * 0.5f))
        }
        drawPath(
            path = centerBottomBlob,
            color = WarmCream.copy(alpha = 0.40f * alphaMultiplier),
            style = Fill
        )
    }
}

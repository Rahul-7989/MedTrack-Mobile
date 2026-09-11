package com.example.ui.hub.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path

// Background geometric palette
private val ShapeDustyTeal = Color(0xFF72B5BA).copy(alpha = 0.07f)
private val ShapePaleTeal = Color(0xFFA9CED0).copy(alpha = 0.08f)
private val ShapeWarmAmber = Color(0xFFE5A23C).copy(alpha = 0.06f)
private val ShapeWarmCream = Color(0xFFF3E6D5).copy(alpha = 0.25f)

/**
 * Layered organic geometric background shapes adhering strictly to MedTrack's visual style.
 */
@Composable
fun HubDashboardBackgroundShapes(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top right gentle teal circle
        drawCircle(
            color = ShapePaleTeal,
            radius = width * 0.45f,
            center = Offset(width * 0.92f, height * 0.08f)
        )

        // Top left warm amber arc
        drawCircle(
            color = ShapeWarmAmber,
            radius = width * 0.38f,
            center = Offset(width * 0.05f, height * 0.22f)
        )

        // Mid-right organic soft cream curve
        val path1 = Path().apply {
            moveTo(width, height * 0.35f)
            cubicTo(
                width * 0.65f, height * 0.40f,
                width * 0.70f, height * 0.58f,
                width, height * 0.65f
            )
            close()
        }
        drawPath(path1, color = ShapeWarmCream)

        // Bottom left dusty teal ambient circle
        drawCircle(
            color = ShapeDustyTeal,
            radius = width * 0.55f,
            center = Offset(width * 0.15f, height * 0.88f)
        )
    }
}

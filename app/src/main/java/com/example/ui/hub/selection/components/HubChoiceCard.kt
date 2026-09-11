package com.example.ui.hub.selection.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SoraFontFamily

// Scoped Card Palette
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorTextMuted = Color(0xFF665F58)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorBorderHover = Color(0xFFD8C4AD)
private val ColorAmbientShadow = Color(0x149C876E)
private val ColorSpotShadow = Color(0x1E786550)

enum class HubChoiceType {
    CREATE,
    JOIN
}

/**
 * Large tactile choice card for Hub Selection.
 *
 * Supports hover (rise 2-3px, enhanced shadow) and press (subtle 0.98 scale compression).
 */
@Composable
fun HubChoiceCard(
    type: HubChoiceType,
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardShape = RoundedCornerShape(20.dp)

    // Hover & Press physics
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 2.dp
            isHovered -> 8.dp
            else -> 4.dp
        },
        animationSpec = tween(220),
        label = "CardElevation"
    )

    val offsetY by animateDpAsState(
        targetValue = when {
            isPressed -> 0.dp
            isHovered -> (-3).dp
            else -> 0.dp
        },
        animationSpec = tween(220),
        label = "CardOffsetY"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = tween(180),
        label = "CardScale"
    )

    val borderColor = if (isHovered) ColorBorderHover else ColorBorderWarm
    val testTag = if (type == HubChoiceType.CREATE) "create_hub_card" else "join_hub_card"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .offset(y = offsetY)
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = cardShape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(cardShape)
            .background(ColorWarmCream)
            .border(BorderStroke(1.5.dp, borderColor), cardShape)
            .hoverable(interactionSource = interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = "$title. $description"
            }
            .testTag(testTag)
            .padding(horizontal = if (isCompact) 18.dp else 22.dp, vertical = if (isCompact) 18.dp else 22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 16.dp else 20.dp)
        ) {
            // Left: Minimal geometric illustration
            Box(
                modifier = Modifier
                    .size(if (isCompact) 56.dp else 64.dp)
                    .clip(CircleShape)
                    .background(ColorWarmIvory)
                    .border(BorderStroke(1.dp, ColorBorderWarm.copy(alpha = 0.6f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (type == HubChoiceType.CREATE) {
                    CreateHubIllustrationCanvas(
                        modifier = Modifier.size(if (isCompact) 48.dp else 54.dp)
                    )
                } else {
                    JoinHubIllustrationCanvas(
                        modifier = Modifier.size(if (isCompact) 48.dp else 54.dp)
                    )
                }
            }

            // Right: Text content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isCompact) 15.5.sp else 16.5.sp,
                    color = ColorDarkWarmText,
                    letterSpacing = (-0.2).sp
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = description,
                    fontFamily = SoraFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = if (isCompact) 12.sp else 13.sp,
                    color = ColorTextMuted,
                    lineHeight = if (isCompact) 17.sp else 18.5.sp,
                    letterSpacing = 0.1.sp
                )
            }
        }
    }
}

/**
 * Minimal geometric illustration for "Create your family hub".
 * Concept: Simple home with connected dots in Warm Amber & Burnt Apricot.
 */
@Composable
private fun CreateHubIllustrationCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // House Body
        val housePath = Path().apply {
            // Roof peak
            moveTo(w * 0.50f, h * 0.22f)
            // Right roof edge
            lineTo(w * 0.76f, h * 0.44f)
            // Right wall
            lineTo(w * 0.72f, h * 0.74f)
            // Floor
            lineTo(w * 0.28f, h * 0.74f)
            // Left wall
            lineTo(w * 0.24f, h * 0.44f)
            close()
        }
        drawPath(
            path = housePath,
            color = ColorWarmAmber.copy(alpha = 0.18f)
        )
        drawPath(
            path = housePath,
            color = ColorWarmAmber,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Doorway / Heart of the home
        drawRoundRect(
            color = ColorBurntApricot,
            topLeft = Offset(w * 0.43f, h * 0.54f),
            size = Size(w * 0.14f, h * 0.20f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Chimney / Hearth accent
        val chimneyPath = Path().apply {
            moveTo(w * 0.62f, h * 0.32f)
            lineTo(w * 0.62f, h * 0.24f)
            lineTo(w * 0.68f, h * 0.24f)
            lineTo(w * 0.68f, h * 0.37f)
        }
        drawPath(
            path = chimneyPath,
            color = ColorBurntApricot,
            style = Stroke(width = 1.8f, cap = StrokeCap.Round)
        )

        // Connected family dots around the house
        // Top dot
        drawCircle(
            color = ColorBurntApricot,
            radius = 3f,
            center = Offset(w * 0.50f, h * 0.12f)
        )
        drawLine(
            color = ColorBurntApricot.copy(alpha = 0.5f),
            start = Offset(w * 0.50f, h * 0.15f),
            end = Offset(w * 0.50f, h * 0.22f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        // Left dot
        drawCircle(
            color = ColorWarmAmber,
            radius = 2.6f,
            center = Offset(w * 0.15f, h * 0.50f)
        )
        drawLine(
            color = ColorWarmAmber.copy(alpha = 0.5f),
            start = Offset(w * 0.18f, h * 0.50f),
            end = Offset(w * 0.24f, h * 0.50f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )

        // Right dot
        drawCircle(
            color = ColorWarmAmber,
            radius = 2.6f,
            center = Offset(w * 0.85f, h * 0.50f)
        )
        drawLine(
            color = ColorWarmAmber.copy(alpha = 0.5f),
            start = Offset(w * 0.82f, h * 0.50f),
            end = Offset(w * 0.76f, h * 0.50f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Minimal geometric illustration for "Join a family hub".
 * Concept: Connected group/people nodes in Dusty Teal & Pale Teal.
 */
@Composable
private fun JoinHubIllustrationCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Connecting constellation lines
        val linePath = Path().apply {
            moveTo(w * 0.32f, h * 0.60f)
            lineTo(w * 0.50f, h * 0.36f)
            lineTo(w * 0.68f, h * 0.60f)
            lineTo(w * 0.32f, h * 0.60f)
        }
        drawPath(
            path = linePath,
            color = ColorPaleTeal.copy(alpha = 0.40f)
        )
        drawPath(
            path = linePath,
            color = ColorDustyTeal,
            style = Stroke(width = 1.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Central connection pulse ring
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.30f),
            radius = w * 0.16f,
            center = Offset(w * 0.50f, h * 0.36f)
        )

        // Center primary person node
        drawCircle(
            color = ColorDustyTeal,
            radius = 5.5f,
            center = Offset(w * 0.50f, h * 0.36f)
        )
        drawCircle(
            color = ColorWarmIvory,
            radius = 2.2f,
            center = Offset(w * 0.50f, h * 0.36f)
        )

        // Left family member node
        drawCircle(
            color = ColorDustyTeal,
            radius = 4.5f,
            center = Offset(w * 0.32f, h * 0.60f)
        )

        // Right family member node
        drawCircle(
            color = ColorDustyTeal,
            radius = 4.5f,
            center = Offset(w * 0.68f, h * 0.60f)
        )

        // Incoming join arrow / spark node (top right)
        drawCircle(
            color = ColorPaleTeal,
            radius = 3.2f,
            center = Offset(w * 0.72f, h * 0.24f)
        )
        drawLine(
            color = ColorDustyTeal.copy(alpha = 0.60f),
            start = Offset(w * 0.69f, h * 0.27f),
            end = Offset(w * 0.55f, h * 0.33f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }
}

package com.example.ui.profilesetup.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.profilesetup.model.ProfileAvatarType

import androidx.compose.ui.unit.Dp

// Scoped Palette for Avatars
private val ColorDustyTeal = Color(0xFF72B5BA)
private val ColorPaleTeal = Color(0xFFA9CED0)
private val ColorWarmAmber = Color(0xFFE5A23C)
private val ColorBurntApricot = Color(0xFFD88B3D)
private val ColorWarmIvory = Color(0xFFFAF4EC)
private val ColorWarmCream = Color(0xFFF3E6D5)
private val ColorDarkWarmText = Color(0xFF514A44)
private val ColorBorderWarm = Color(0xFFE4D5C2)
private val ColorAmbientShadow = Color(0x189C876E)
private val ColorSpotShadow = Color(0x22786550)

/**
 * Dynamic Profile Avatar View.
 *
 * Prominently displayed near the top of the Profile Setup card and Hub Selection header.
 * Automatically updates its illustration based on gender selection with a smooth,
 * subtle fade & scale transition (200-300ms ease-out).
 *
 * Exactly three states:
 * 1. Male - Minimalist, friendly, modern head-and-shoulders with male styling
 * 2. Female - Same family aesthetic, matching proportions with female styling
 * 3. Anonymous - Neutral, friendly geometric silhouette for "Prefer not to say"
 */
@Composable
fun ProfileAvatarView(
    avatarType: ProfileAvatarType,
    modifier: Modifier = Modifier,
    size: Dp = 96.dp
) {
    val easeOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = if (size > 64.dp) 6.dp else 4.dp,
                shape = CircleShape,
                ambientColor = ColorAmbientShadow,
                spotColor = ColorSpotShadow
            )
            .clip(CircleShape)
            .background(ColorWarmIvory)
            .border(BorderStroke(1.5.dp, ColorBorderWarm), CircleShape)
            .semantics {
                contentDescription = when (avatarType) {
                    ProfileAvatarType.MALE -> "Male profile avatar"
                    ProfileAvatarType.FEMALE -> "Female profile avatar"
                    ProfileAvatarType.ANONYMOUS -> "Anonymous profile avatar"
                    ProfileAvatarType.CHILD_MALE -> "Boy child profile avatar"
                    ProfileAvatarType.CHILD_FEMALE -> "Girl child profile avatar"
                    ProfileAvatarType.CHILD_ANONYMOUS -> "Child profile avatar"
                }
            }
            .testTag("profile_avatar_container"),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = avatarType,
            transitionSpec = {
                (fadeIn(animationSpec = tween(240, easing = easeOut)) +
                        scaleIn(initialScale = 0.95f, animationSpec = tween(240, easing = easeOut)))
                    .togetherWith(
                        fadeOut(animationSpec = tween(180, easing = easeOut)) +
                                scaleOut(targetScale = 0.95f, animationSpec = tween(180, easing = easeOut))
                    )
            },
            label = "AvatarTransition"
        ) { targetAvatar ->
            when (targetAvatar) {
                ProfileAvatarType.MALE -> MaleAvatarCanvas(
                    modifier = Modifier
                        .size(size)
                        .testTag("profile_avatar_male")
                )
                ProfileAvatarType.FEMALE -> FemaleAvatarCanvas(
                    modifier = Modifier
                        .size(size)
                        .testTag("profile_avatar_female")
                )
                ProfileAvatarType.ANONYMOUS -> AnonymousAvatarCanvas(
                    modifier = Modifier
                        .size(size)
                        .testTag("profile_avatar_anonymous")
                )
                ProfileAvatarType.CHILD_MALE -> ChildMaleAvatarCanvas(
                    modifier = Modifier
                        .size(size)
                        .testTag("profile_avatar_child_male")
                )
                ProfileAvatarType.CHILD_FEMALE -> ChildFemaleAvatarCanvas(
                    modifier = Modifier
                        .size(size)
                        .testTag("profile_avatar_child_female")
                )
                ProfileAvatarType.CHILD_ANONYMOUS -> ChildAnonymousAvatarCanvas(
                    modifier = Modifier
                        .size(size)
                        .testTag("profile_avatar_child_anonymous")
                )
            }
        }
    }
}

/**
 * Clean, friendly Male illustrated avatar in the MedTrack palette.
 */
@Composable
private fun MaleAvatarCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft ambient inner circle glow
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.75f),
            radius = w * 0.46f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 2. Shoulders / Chest (Dusty Teal shirt with subtle rounded collar)
        val shoulderPath = Path().apply {
            moveTo(w * 0.16f, h)
            cubicTo(
                w * 0.18f, h * 0.70f,
                w * 0.32f, h * 0.65f,
                w * 0.42f, h * 0.65f
            )
            lineTo(w * 0.58f, h * 0.65f)
            cubicTo(
                w * 0.68f, h * 0.65f,
                w * 0.82f, h * 0.70f,
                w * 0.84f, h
            )
            close()
        }
        drawPath(path = shoulderPath, color = ColorDustyTeal)

        // Collar accent in Warm Amber
        val collarPath = Path().apply {
            moveTo(w * 0.44f, h * 0.65f)
            lineTo(w * 0.50f, h * 0.72f)
            lineTo(w * 0.56f, h * 0.65f)
        }
        drawPath(
            path = collarPath,
            color = ColorWarmAmber,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 3. Neck
        drawRoundRect(
            color = Color(0xFFF7E4D0),
            topLeft = Offset(w * 0.44f, h * 0.50f),
            size = Size(w * 0.12f, h * 0.18f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 4. Head / Face
        drawCircle(
            color = Color(0xFFF9EAD9),
            radius = w * 0.20f,
            center = Offset(w * 0.5f, h * 0.40f)
        )

        // 5. Short modern Hair silhouette in Burnt Apricot / Dark Warm tone
        val hairPath = Path().apply {
            moveTo(w * 0.30f, h * 0.38f)
            cubicTo(
                w * 0.28f, h * 0.22f,
                w * 0.38f, h * 0.18f,
                w * 0.50f, h * 0.18f
            )
            cubicTo(
                w * 0.62f, h * 0.18f,
                w * 0.72f, h * 0.22f,
                w * 0.70f, h * 0.38f
            )
            cubicTo(
                w * 0.68f, h * 0.30f,
                w * 0.56f, h * 0.26f,
                w * 0.50f, h * 0.27f
            )
            cubicTo(
                w * 0.44f, h * 0.26f,
                w * 0.32f, h * 0.30f,
                w * 0.30f, h * 0.38f
            )
            close()
        }
        drawPath(path = hairPath, color = ColorBurntApricot)

        // 6. Friendly minimal eyes & smile
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.2f,
            center = Offset(w * 0.44f, h * 0.40f)
        )
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.2f,
            center = Offset(w * 0.56f, h * 0.40f)
        )

        val smilePath = Path().apply {
            moveTo(w * 0.46f, h * 0.46f)
            quadraticTo(w * 0.50f, h * 0.49f, w * 0.54f, h * 0.46f)
        }
        drawPath(
            path = smilePath,
            color = ColorDarkWarmText,
            style = Stroke(width = 1.8f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Clean, friendly Female illustrated avatar in matching MedTrack palette and proportions.
 */
@Composable
private fun FemaleAvatarCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft ambient inner circle glow
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.75f),
            radius = w * 0.46f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 2. Long hair backdrop
        val hairBackPath = Path().apply {
            moveTo(w * 0.26f, h * 0.38f)
            cubicTo(
                w * 0.22f, h * 0.50f,
                w * 0.24f, h * 0.68f,
                w * 0.30f, h * 0.74f
            )
            lineTo(w * 0.70f, h * 0.74f)
            cubicTo(
                w * 0.76f, h * 0.68f,
                w * 0.78f, h * 0.50f,
                w * 0.74f, h * 0.38f
            )
            close()
        }
        drawPath(path = hairBackPath, color = ColorWarmAmber)

        // 3. Shoulders / Chest (Warm Amber top with Dusty Teal trim)
        val shoulderPath = Path().apply {
            moveTo(w * 0.16f, h)
            cubicTo(
                w * 0.18f, h * 0.70f,
                w * 0.32f, h * 0.65f,
                w * 0.42f, h * 0.65f
            )
            lineTo(w * 0.58f, h * 0.65f)
            cubicTo(
                w * 0.68f, h * 0.65f,
                w * 0.82f, h * 0.70f,
                w * 0.84f, h
            )
            close()
        }
        drawPath(path = shoulderPath, color = ColorWarmAmber)

        // Collar accent in Dusty Teal
        val collarPath = Path().apply {
            moveTo(w * 0.43f, h * 0.65f)
            quadraticTo(w * 0.50f, h * 0.73f, w * 0.57f, h * 0.65f)
        }
        drawPath(
            path = collarPath,
            color = ColorDustyTeal,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // 4. Neck
        drawRoundRect(
            color = Color(0xFFF7E4D0),
            topLeft = Offset(w * 0.44f, h * 0.50f),
            size = Size(w * 0.12f, h * 0.18f),
            cornerRadius = CornerRadius(4f, 4f)
        )

        // 5. Head / Face
        drawCircle(
            color = Color(0xFFF9EAD9),
            radius = w * 0.195f,
            center = Offset(w * 0.5f, h * 0.40f)
        )

        // 6. Hair front / styling in Warm Amber
        val hairFrontPath = Path().apply {
            moveTo(w * 0.28f, h * 0.36f)
            cubicTo(
                w * 0.28f, h * 0.20f,
                w * 0.40f, h * 0.17f,
                w * 0.50f, h * 0.17f
            )
            cubicTo(
                w * 0.60f, h * 0.17f,
                w * 0.72f, h * 0.20f,
                w * 0.72f, h * 0.36f
            )
            cubicTo(
                w * 0.68f, h * 0.27f,
                w * 0.56f, h * 0.24f,
                w * 0.50f, h * 0.28f
            )
            cubicTo(
                w * 0.44f, h * 0.24f,
                w * 0.32f, h * 0.27f,
                w * 0.28f, h * 0.36f
            )
            close()
        }
        drawPath(path = hairFrontPath, color = ColorWarmAmber)

        // 7. Friendly minimal eyes & smile
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.2f,
            center = Offset(w * 0.44f, h * 0.40f)
        )
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.2f,
            center = Offset(w * 0.56f, h * 0.40f)
        )

        val smilePath = Path().apply {
            moveTo(w * 0.46f, h * 0.46f)
            quadraticTo(w * 0.50f, h * 0.49f, w * 0.54f, h * 0.46f)
        }
        drawPath(
            path = smilePath,
            color = ColorDarkWarmText,
            style = Stroke(width = 1.8f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Clean, friendly neutral Anonymous silhouette for "Prefer not to say".
 * Clearly distinct from Male/Female while maintaining the warm, approachable aesthetic.
 */
@Composable
private fun AnonymousAvatarCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft Pale Teal background glow
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.35f),
            radius = w * 0.46f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 2. Neutral geometric shoulders in Pale Teal / Dusty Teal
        val shoulderPath = Path().apply {
            moveTo(w * 0.18f, h)
            cubicTo(
                w * 0.20f, h * 0.68f,
                w * 0.34f, h * 0.62f,
                w * 0.42f, h * 0.62f
            )
            lineTo(w * 0.58f, h * 0.62f)
            cubicTo(
                w * 0.66f, h * 0.62f,
                w * 0.80f, h * 0.68f,
                w * 0.82f, h
            )
            close()
        }
        drawPath(path = shoulderPath, color = ColorPaleTeal)

        // Subtle Dusty Teal trim on shoulders
        drawPath(
            path = Path().apply {
                moveTo(w * 0.42f, h * 0.62f)
                lineTo(w * 0.58f, h * 0.62f)
            },
            color = ColorDustyTeal,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // 3. Neutral geometric head silhouette in Dusty Teal
        drawCircle(
            color = ColorDustyTeal,
            radius = w * 0.20f,
            center = Offset(w * 0.5f, h * 0.38f)
        )

        // 4. Subtle inner Warm Ivory geometric accent
        drawCircle(
            color = ColorWarmIvory.copy(alpha = 0.90f),
            radius = w * 0.08f,
            center = Offset(w * 0.5f, h * 0.38f)
        )
    }
}

/**
 * Friendly, cute Boy Child illustrated avatar in MedTrack aesthetic.
 * Features rounded joyful proportions, playful cowlick hair, blush cheeks, and striped tee.
 */
@Composable
private fun ChildMaleAvatarCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft warm ambient glow
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.85f),
            radius = w * 0.46f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 2. Child Shoulders & Striped T-Shirt (Dusty Teal base with Warm Amber stripe)
        val shoulderPath = Path().apply {
            moveTo(w * 0.20f, h)
            cubicTo(
                w * 0.22f, h * 0.74f,
                w * 0.35f, h * 0.69f,
                w * 0.44f, h * 0.69f
            )
            lineTo(w * 0.56f, h * 0.69f)
            cubicTo(
                w * 0.65f, h * 0.69f,
                w * 0.78f, h * 0.74f,
                w * 0.80f, h
            )
            close()
        }
        drawPath(path = shoulderPath, color = ColorDustyTeal)

        // Cute rounded neckline
        val neckLinePath = Path().apply {
            moveTo(w * 0.43f, h * 0.69f)
            quadraticTo(w * 0.50f, h * 0.76f, w * 0.57f, h * 0.69f)
        }
        drawPath(
            path = neckLinePath,
            color = ColorWarmAmber,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )

        // 3. Short child neck
        drawRoundRect(
            color = Color(0xFFF7E4D0),
            topLeft = Offset(w * 0.45f, h * 0.54f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // 4. Large round child head & chubby cheeks
        drawCircle(
            color = Color(0xFFF9EAD9),
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.42f)
        )

        // 5. Cute playful boy haircut with cowlick tuft
        val hairPath = Path().apply {
            // Left side
            moveTo(w * 0.27f, h * 0.42f)
            cubicTo(
                w * 0.25f, h * 0.24f,
                w * 0.36f, h * 0.17f,
                w * 0.48f, h * 0.17f
            )
            // Playful cowlick / tuft
            cubicTo(
                w * 0.50f, h * 0.11f,
                w * 0.55f, h * 0.13f,
                w * 0.54f, h * 0.17f
            )
            // Right side
            cubicTo(
                w * 0.64f, h * 0.17f,
                w * 0.74f, h * 0.24f,
                w * 0.73f, h * 0.42f
            )
            // Bangs
            cubicTo(
                w * 0.68f, h * 0.32f,
                w * 0.58f, h * 0.28f,
                w * 0.50f, h * 0.30f
            )
            cubicTo(
                w * 0.42f, h * 0.28f,
                w * 0.32f, h * 0.32f,
                w * 0.27f, h * 0.42f
            )
            close()
        }
        drawPath(path = hairPath, color = ColorBurntApricot)

        // 6. Sweet Rosy Blush on cheeks
        drawCircle(
            color = Color(0xFFF6C8BE).copy(alpha = 0.85f),
            radius = w * 0.045f,
            center = Offset(w * 0.37f, h * 0.46f)
        )
        drawCircle(
            color = Color(0xFFF6C8BE).copy(alpha = 0.85f),
            radius = w * 0.045f,
            center = Offset(w * 0.63f, h * 0.46f)
        )

        // 7. Joyful child eyes with shine
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.4f,
            center = Offset(w * 0.43f, h * 0.41f)
        )
        drawCircle(
            color = Color(0xFFFAF4EC),
            radius = 0.9f,
            center = Offset(w * 0.42f, h * 0.40f)
        )
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.4f,
            center = Offset(w * 0.57f, h * 0.41f)
        )
        drawCircle(
            color = Color(0xFFFAF4EC),
            radius = 0.9f,
            center = Offset(w * 0.56f, h * 0.40f)
        )

        // 8. Happy wide child smile
        val smilePath = Path().apply {
            moveTo(w * 0.45f, h * 0.48f)
            quadraticTo(w * 0.50f, h * 0.53f, w * 0.55f, h * 0.48f)
        }
        drawPath(
            path = smilePath,
            color = ColorDarkWarmText,
            style = Stroke(width = 2.0f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Friendly, cute Girl Child illustrated avatar in MedTrack aesthetic.
 * Features sweet twin side buns/pigtails with ribbon bows, rosy cheeks, and Peter Pan collar.
 */
@Composable
private fun ChildFemaleAvatarCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft warm ambient glow
        drawCircle(
            color = ColorWarmCream.copy(alpha = 0.85f),
            radius = w * 0.46f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 2. Cute side hair buns
        drawCircle(
            color = ColorWarmAmber,
            radius = w * 0.09f,
            center = Offset(w * 0.28f, h * 0.28f)
        )
        drawCircle(
            color = ColorWarmAmber,
            radius = w * 0.09f,
            center = Offset(w * 0.72f, h * 0.28f)
        )

        // Little bow ties on buns in Dusty Teal
        drawCircle(
            color = ColorDustyTeal,
            radius = w * 0.035f,
            center = Offset(w * 0.32f, h * 0.31f)
        )
        drawCircle(
            color = ColorDustyTeal,
            radius = w * 0.035f,
            center = Offset(w * 0.68f, h * 0.31f)
        )

        // 3. Child Shoulders (Warm Amber top with Dusty Teal rounded Peter Pan collar)
        val shoulderPath = Path().apply {
            moveTo(w * 0.20f, h)
            cubicTo(
                w * 0.22f, h * 0.74f,
                w * 0.35f, h * 0.69f,
                w * 0.44f, h * 0.69f
            )
            lineTo(w * 0.56f, h * 0.69f)
            cubicTo(
                w * 0.65f, h * 0.69f,
                w * 0.78f, h * 0.74f,
                w * 0.80f, h
            )
            close()
        }
        drawPath(path = shoulderPath, color = ColorWarmAmber)

        // Rounded Peter Pan collar
        drawCircle(
            color = ColorDustyTeal,
            radius = w * 0.05f,
            center = Offset(w * 0.46f, h * 0.71f)
        )
        drawCircle(
            color = ColorDustyTeal,
            radius = w * 0.05f,
            center = Offset(w * 0.54f, h * 0.71f)
        )

        // 4. Short child neck
        drawRoundRect(
            color = Color(0xFFF7E4D0),
            topLeft = Offset(w * 0.45f, h * 0.54f),
            size = Size(w * 0.10f, h * 0.16f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // 5. Large round child head & chubby cheeks
        drawCircle(
            color = Color(0xFFF9EAD9),
            radius = w * 0.22f,
            center = Offset(w * 0.5f, h * 0.42f)
        )

        // 6. Girl bangs and hair front in Warm Amber
        val hairFrontPath = Path().apply {
            moveTo(w * 0.27f, h * 0.40f)
            cubicTo(
                w * 0.26f, h * 0.22f,
                w * 0.38f, h * 0.18f,
                w * 0.50f, h * 0.18f
            )
            cubicTo(
                w * 0.62f, h * 0.18f,
                w * 0.74f, h * 0.22f,
                w * 0.73f, h * 0.40f
            )
            cubicTo(
                w * 0.68f, h * 0.30f,
                w * 0.57f, h * 0.26f,
                w * 0.50f, h * 0.29f
            )
            cubicTo(
                w * 0.43f, h * 0.26f,
                w * 0.32f, h * 0.30f,
                w * 0.27f, h * 0.40f
            )
            close()
        }
        drawPath(path = hairFrontPath, color = ColorWarmAmber)

        // 7. Sweet Rosy Blush on cheeks
        drawCircle(
            color = Color(0xFFF6C8BE).copy(alpha = 0.85f),
            radius = w * 0.045f,
            center = Offset(w * 0.37f, h * 0.46f)
        )
        drawCircle(
            color = Color(0xFFF6C8BE).copy(alpha = 0.85f),
            radius = w * 0.045f,
            center = Offset(w * 0.63f, h * 0.46f)
        )

        // 8. Sparkling child eyes
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.4f,
            center = Offset(w * 0.43f, h * 0.41f)
        )
        drawCircle(
            color = Color(0xFFFAF4EC),
            radius = 0.9f,
            center = Offset(w * 0.42f, h * 0.40f)
        )
        drawCircle(
            color = ColorDarkWarmText,
            radius = 2.4f,
            center = Offset(w * 0.57f, h * 0.41f)
        )
        drawCircle(
            color = Color(0xFFFAF4EC),
            radius = 0.9f,
            center = Offset(w * 0.56f, h * 0.40f)
        )

        // 9. Joyful smile
        val smilePath = Path().apply {
            moveTo(w * 0.45f, h * 0.48f)
            quadraticTo(w * 0.50f, h * 0.53f, w * 0.55f, h * 0.48f)
        }
        drawPath(
            path = smilePath,
            color = ColorDarkWarmText,
            style = Stroke(width = 2.0f, cap = StrokeCap.Round)
        )
    }
}

/**
 * Friendly Anonymous Child avatar for "Prefer not to say".
 * Clearly distinctive from adult neutral avatar with playful child proportions and cute cap/accent.
 */
@Composable
private fun ChildAnonymousAvatarCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Soft Pale Teal background glow
        drawCircle(
            color = ColorPaleTeal.copy(alpha = 0.45f),
            radius = w * 0.46f,
            center = Offset(w * 0.5f, h * 0.5f)
        )

        // 2. Child geometric shoulders in Pale Teal
        val shoulderPath = Path().apply {
            moveTo(w * 0.22f, h)
            cubicTo(
                w * 0.24f, h * 0.73f,
                w * 0.36f, h * 0.68f,
                w * 0.44f, h * 0.68f
            )
            lineTo(w * 0.56f, h * 0.68f)
            cubicTo(
                w * 0.64f, h * 0.68f,
                w * 0.76f, h * 0.73f,
                w * 0.78f, h
            )
            close()
        }
        drawPath(path = shoulderPath, color = ColorPaleTeal)

        // Collar accent in Warm Amber
        drawPath(
            path = Path().apply {
                moveTo(w * 0.43f, h * 0.68f)
                lineTo(w * 0.57f, h * 0.68f)
            },
            color = ColorWarmAmber,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // 3. Child head in Dusty Teal
        drawCircle(
            color = ColorDustyTeal,
            radius = w * 0.21f,
            center = Offset(w * 0.5f, h * 0.41f)
        )

        // 4. Playful pom-pom / top knot dot
        drawCircle(
            color = ColorWarmAmber,
            radius = w * 0.05f,
            center = Offset(w * 0.5f, h * 0.18f)
        )

        // 5. Friendly inner face accent
        drawCircle(
            color = ColorWarmIvory.copy(alpha = 0.92f),
            radius = w * 0.09f,
            center = Offset(w * 0.5f, h * 0.41f)
        )
    }
}

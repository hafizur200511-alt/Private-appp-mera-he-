package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardBackgroundGlass
import com.example.ui.theme.CardBackgroundGlassBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.LiquidGlassSpecularBorder
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextDim
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.WarningAmber

@Composable
fun AnimatedGlowingBackground(content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_glass_anim")
    val animOffset1 by infiniteTransition.animateFloat(
        initialValue = -60f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_float_1"
    )
    val animOffset2 by infiniteTransition.animateFloat(
        initialValue = 40f,
        targetValue = -40f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "orb_float_2"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (size.width <= 1f || size.height <= 1f) return@Canvas

            val r1 = (size.width * 0.75f).coerceAtLeast(10f)
            val r2 = (size.width * 0.85f).coerceAtLeast(10f)
            val r3 = (size.width * 0.80f).coerceAtLeast(10f)
            val r4 = (size.width * 0.55f).coerceAtLeast(10f)

            // Liquid Orb 1: Electric Cyan Caustic Glow (Top Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(AccentCyan.copy(alpha = pulseAlpha), Color(0x110072FE), Color.Transparent),
                    center = Offset(size.width * 0.85f + animOffset1, size.height * 0.10f + animOffset2 * 0.5f),
                    radius = r1
                )
            )
            // Liquid Orb 2: Deep Sapphire Blue Core (Center Left)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(PrimaryBlue.copy(alpha = 0.30f), Color(0x1800F5FF), Color.Transparent),
                    center = Offset(size.width * 0.12f + animOffset2, size.height * 0.42f + animOffset1 * 0.4f),
                    radius = r2
                )
            )
            // Liquid Orb 3: Radiant Emerald Glow (Bottom Center)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(EmeraldGreen.copy(alpha = pulseAlpha * 0.85f), Color(0x1110B981), Color.Transparent),
                    center = Offset(size.width * 0.5f + animOffset1 * 0.5f, size.height * 0.86f + animOffset2 * 0.6f),
                    radius = r3
                )
            )
            // Liquid Orb 4: Subtle Ultraviolet Shimmer (Bottom Right)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x28833AB4), Color(0x10FD1D1D), Color.Transparent),
                    center = Offset(size.width * 0.90f - animOffset2, size.height * 0.65f),
                    radius = r4
                )
            )
            // Top specular caustic sheen line
            drawLine(
                brush = Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0x3300F5FF), Color(0x44FFFFFF), Color(0x220072FE), Color.Transparent)
                ),
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 2f
            )
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 22.dp,
    borderBrush: Brush = Brush.linearGradient(
        listOf(
            Color(0x66FFFFFF),
            Color(0x3300F5FF),
            Color(0x150072FE),
            Color(0x44FFFFFF)
        )
    ),
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = Color(0xD907142A),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = modifier
        .shadow(elevation = 14.dp, shape = shape, spotColor = Color(0x4400F5FF), ambientColor = Color(0x260072FE))
        .clip(shape)
        .background(backgroundColor)
        .border(width = borderWidth, brush = borderBrush, shape = shape)
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun LuxuryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradient: Brush = Brush.horizontalGradient(listOf(Color(0xFF047857), Color(0xFF10B981), Color(0xFF059669))),
    leadingIcon: ImageVector? = null
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) gradient else Brush.horizontalGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))))
            .border(1.dp, if (enabled) Brush.horizontalGradient(listOf(Color(0x8834D399), Color(0x3300F5FF))) else SolidColor(Color.Transparent), RoundedCornerShape(16.dp))
            .clickable(enabled = enabled && !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = TextWhite,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    accentColor: Color = AccentCyan
) {
    GlassCard(
        modifier = modifier,
        cornerRadius = 16.dp,
        borderBrush = Brush.linearGradient(listOf(accentColor.copy(alpha = 0.5f), Color.Transparent))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value,
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = TextDim,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "ACTIVE", "COMPLETED", "APPROVED" -> Triple(EmeraldGreen.copy(alpha = 0.2f), EmeraldGreenLight, status)
        "PENDING", "AWAITING_ADMIN" -> Triple(WarningAmber.copy(alpha = 0.2f), WarningAmber, "Pending")
        "PROCESSING", "IN_PROGRESS" -> Triple(AccentCyan.copy(alpha = 0.2f), AccentCyan, "In Progress")
        "INACTIVE", "UNAVAILABLE" -> Triple(Color(0x3364748B), TextDim, "Inactive")
        "CANCELLED", "REJECTED", "FAILED" -> Triple(ErrorRed.copy(alpha = 0.2f), ErrorRed, status)
        else -> Triple(Color(0x22CBD5E1), TextWhite, status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, textColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

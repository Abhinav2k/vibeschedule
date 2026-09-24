package com.vibeschedule.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.ui.theme.GlassBorderBrush
import com.vibeschedule.app.ui.theme.SurfaceGlass
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderBrush: Brush = GlassBorderBrush,
    backgroundColor: Color = SurfaceGlass,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(BorderStroke(0.75.dp, borderBrush), shape)
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun LiquidTabSwitcher(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    tabs: List<String>,
    modifier: Modifier = Modifier
) {
    val pillShape = CircleShape

    Box(
        modifier = modifier
            .clip(pillShape)
            .background(Color(0x12FFFFFF))
            .border(BorderStroke(0.75.dp, Color(0x1CFFFFFF)), pillShape)
            .padding(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) Color(0x30FFFFFF) else Color.Transparent,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "tabBg"
                )
                val animatedTextColor by animateColorAsState(
                    targetValue = if (isSelected) TextPrimary else TextSecondary,
                    animationSpec = tween(durationMillis = 150),
                    label = "tabText"
                )

                Box(
                    modifier = Modifier
                        .clip(pillShape)
                        .background(animatedBg)
                        .then(if (isSelected) Modifier.border(BorderStroke(0.5.dp, Color(0x35FFFFFF)), pillShape) else Modifier)
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = { onTabSelected(index) })
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        letterSpacing = (-0.1).sp,
                        color = animatedTextColor
                    )
                }
            }
        }
    }
}

@Composable
fun GlowingIndicator(isActive: Boolean, modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.85f,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "alpha"
    )

    val color = if (isActive) Color.White else Color(0x55FFFFFF)

    Box(modifier = modifier.size(14.dp), contentAlignment = Alignment.Center) {
        if (isActive) {
            Box(
                modifier = Modifier.size(14.dp).scale(scale).clip(CircleShape).background(color.copy(alpha = alpha * 0.35f))
            )
        }
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(color))
    }
}

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Floating Liquid Glass Navigation Bar inspired by LastWave Native's liquid glass player card.
 * Features a floating rounded capsule, specular top rim light, and fluid glowing active indicator.
 */
@Composable
fun FloatingLiquidGlassBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavTabItem("Home", Icons.Rounded.Home, Icons.Outlined.Home),
        NavTabItem("Schedules", Icons.Rounded.Schedule, Icons.Outlined.Schedule),
        NavTabItem("Settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
    )

    val capsuleShape = CircleShape

    Box(
        modifier = modifier
            .shadow(elevation = 20.dp, shape = capsuleShape, spotColor = Color.Black, ambientColor = Color.Black)
            .clip(capsuleShape)
            .background(Color(0xDC121318))
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x66FFFFFF),
                            Color(0x22FFFFFF),
                            Color(0x0EFFFFFF)
                        )
                    )
                ),
                capsuleShape
            )
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.Transparent,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "tabBg"
                )
                val animatedContentColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF0A0A0F) else Color(0xFF9E9EA8),
                    animationSpec = tween(durationMillis = 150),
                    label = "tabContent"
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(animatedBg)
                        .then(
                            if (isSelected) {
                                Modifier.border(BorderStroke(0.5.dp, Color(0x66FFFFFF)), CircleShape)
                            } else {
                                Modifier
                            }
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onTabSelected(index) }
                        )
                        .padding(horizontal = if (isSelected) 16.dp else 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.title,
                            tint = animatedContentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = animatedContentColor,
                            letterSpacing = (-0.2).sp
                        )
                    }
                }
            }
        }
    }
}


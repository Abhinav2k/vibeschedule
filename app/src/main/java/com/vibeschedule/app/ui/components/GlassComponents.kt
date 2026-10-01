package com.vibeschedule.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.ui.theme.GlassBorderBrush
import com.vibeschedule.app.ui.theme.SurfaceGlass
import com.vibeschedule.app.ui.theme.TextPrimary
import com.vibeschedule.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

data class TabBounds(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

/**
 * Floating Liquid Glass Navigation Bar inspired by LastWave Native's liquid glass player card.
 * Features a continuous-curvature squircle dock capsule, specular top rim light, interactive radial touch glow,
 * and a single translucent frosted liquid glass indicator pill that smoothly slides with spatial spring physics across tabs.
 */
@Composable
fun FloatingLiquidGlassBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val items = remember {
        listOf(
            NavTabItem("Home", Icons.Rounded.Home, Icons.Outlined.Home),
            NavTabItem("Schedules", Icons.Rounded.Schedule, Icons.Outlined.Schedule),
            NavTabItem("Settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
        )
    }

    // Textbook pill (capsule / stadium) shape for both the floating dock and the sliding indicator
    val dockPillShape = CircleShape
    val pillShape = CircleShape

    // Track layout bounds of each tab
    val tabBounds = remember { mutableStateMapOf<Int, TabBounds>() }

    // Spatial spring physics from LastWave ExpressiveMotion
    val navSpring = remember {
        spring<Float>(
            dampingRatio = 0.76f,
            stiffness = 380f
        )
    }

    // Animatable properties for the sliding liquid glass indicator pill
    val indicatorOffsetX = remember { Animatable(0f) }
    val indicatorOffsetY = remember { Animatable(0f) }
    val indicatorWidth = remember { Animatable(0f) }
    val indicatorHeight = remember { Animatable(0f) }
    var isInitialized by remember { mutableStateOf(false) }

    // Interactive touch coordinates for LastWave radial glow effect
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var isTouching by remember { mutableStateOf(false) }

    // Smoothly slide the indicator pill when selectedTab changes or layout measures
    LaunchedEffect(selectedTab, tabBounds[selectedTab]) {
        val bounds = tabBounds[selectedTab] ?: return@LaunchedEffect
        if (bounds.width <= 0f) return@LaunchedEffect
        if (!isInitialized) {
            indicatorOffsetX.snapTo(bounds.x)
            indicatorOffsetY.snapTo(bounds.y)
            indicatorWidth.snapTo(bounds.width)
            indicatorHeight.snapTo(bounds.height)
            isInitialized = true
        } else {
            launch { indicatorOffsetX.animateTo(bounds.x, navSpring) }
            launch { indicatorOffsetY.animateTo(bounds.y, navSpring) }
            launch { indicatorWidth.animateTo(bounds.width, navSpring) }
            launch { indicatorHeight.animateTo(bounds.height, navSpring) }
        }
    }

    Box(
        modifier = modifier
            // Multi-layered soft ambient drop shadow for authentic floating elevation
            .shadow(
                elevation = 18.dp,
                shape = dockPillShape,
                clip = false,
                ambientColor = Color(0x60000000),
                spotColor = Color(0x90000000)
            )
            // Clip to clean pill capsule shape
            .clip(dockPillShape)
            // Translucent glass gradient body (LastWave-native recipe)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0x8C1C2030), // Translucent frosted slate-indigo top
                        Color(0x65111422), // Highly transparent middle for glass refraction
                        Color(0x80171A2A)  // Translucent bottom
                    )
                )
            )
            // Specular rim stroke - catches top light and ambient bottom reflection
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x85FFFFFF), // Bright specular highlight on top curve
                            Color(0x28FFFFFF), // Soft transition
                            Color(0x0CFFFFFF), // Subtle rim
                            Color(0x24FFFFFF)  // Subtle bottom bounce highlight
                        )
                    )
                ),
                dockPillShape
            )
            // LastWave interactive drag/touch inspector for fluid radial light glow
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    touchPosition = down.position
                    isTouching = true
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == down.id }
                        if (change == null || !change.pressed) {
                            isTouching = false
                            touchPosition = null
                            break
                        }
                        touchPosition = change.position
                    }
                }
            }
    ) {
        // Internal specular glass sheen (top curvature light reflection from LastWave)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(dockPillShape)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x25FFFFFF),
                        0.28f to Color(0x0CFFFFFF),
                        0.60f to Color.Transparent
                    )
                )
        )

        // LastWave interactive radial liquid glass glow on touch
        if (isTouching && touchPosition != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(dockPillShape)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                center = touchPosition!!,
                                radius = size.minDimension * 1.5f
                            ),
                            blendMode = BlendMode.Plus
                        )
                    }
            )
        }

        // Inner track containing the sliding liquid glass pill indicator and tab items
        Box(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
        ) {
            // The single translucent frosted liquid glass indicator pill that slides horizontally to the active tab
            if (isInitialized && indicatorWidth.value > 0f) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = indicatorOffsetX.value.roundToInt(),
                                y = indicatorOffsetY.value.roundToInt()
                            )
                        }
                        .size(
                            width = with(density) { indicatorWidth.value.toDp() },
                            height = with(density) { indicatorHeight.value.toDp() }
                        )
                        .shadow(
                            elevation = 6.dp,
                            shape = pillShape,
                            ambientColor = Color(0x25000000),
                            spotColor = Color(0x40000000)
                        )
                        .clip(pillShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x44FFFFFF), // Luminous frosted white top
                                    Color(0x22FFFFFF)  // Luminous translucent white bottom
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0x95FFFFFF), // Brilliant specular top rim
                                        Color(0x35FFFFFF)  // Soft lower rim
                                    )
                                )
                            ),
                            pillShape
                        )
                ) {
                    // Meniscus lens radial sheen inside the sliding pill
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(pillShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(Color(0x2AFFFFFF), Color.Transparent),
                                    center = Offset(indicatorWidth.value * 0.5f, 0f),
                                    radius = maxOf(indicatorWidth.value, indicatorHeight.value)
                                )
                            )
                    )
                }
            }

            // Tab items row positioned on top of the sliding indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    val animatedContentColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFFFFFFFF) else Color(0x80FFFFFF),
                        animationSpec = tween(durationMillis = 180),
                        label = "tabContentColor"
                    )

                    Box(
                        modifier = Modifier
                            .onGloballyPositioned { coordinates ->
                                val pos = coordinates.positionInParent()
                                val size = coordinates.size
                                tabBounds[index] = TabBounds(
                                    x = pos.x,
                                    y = pos.y,
                                    width = size.width.toFloat(),
                                    height = size.height.toFloat()
                                )
                            }
                            .clip(pillShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    if (selectedTab != index) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onTabSelected(index)
                                    }
                                }
                            )
                            .padding(
                                horizontal = if (isSelected) 16.dp else 13.dp,
                                vertical = 9.dp
                            ),
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
                                modifier = Modifier.size(20.dp)
                            )

                            // Only active tab expands its label! Inactive tabs are clean icons only
                            AnimatedVisibility(
                                visible = isSelected,
                                enter = fadeIn(animationSpec = tween(180, delayMillis = 40)) +
                                        expandHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = 0.76f,
                                                stiffness = 380f
                                            )
                                        ),
                                exit = fadeOut(animationSpec = tween(100)) +
                                        shrinkHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = 0.76f,
                                                stiffness = 380f
                                            )
                                        )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = animatedContentColor,
                                        letterSpacing = (-0.2).sp,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


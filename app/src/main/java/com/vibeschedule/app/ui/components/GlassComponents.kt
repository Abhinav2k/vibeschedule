package com.vibeschedule.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
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
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import kotlin.math.abs
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

private const val FLOATING_CARD_REFRACTION_SHADER = """
    uniform shader content;
    uniform float2 size;
    uniform float2 offset;
    uniform float4 cornerRadii;
    uniform float refractionHeight;
    uniform float refractionAmount;
    uniform float depthEffect;

    float radiusAt(float2 coord, float4 radii) {
        if (coord.x >= 0.0) {
            if (coord.y <= 0.0) return radii.y;
            else return radii.z;
        } else {
            if (coord.y <= 0.0) return radii.x;
            else return radii.w;
        }
    }

    float sdRoundedRect(float2 coord, float2 halfSize, float radius) {
        float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
        float outside = length(max(cornerCoord, 0.0)) - radius;
        float inside = min(max(cornerCoord.x, cornerCoord.y), 0.0);
        return outside + inside;
    }

    float2 gradSdRoundedRect(float2 coord, float2 halfSize, float radius) {
        float2 cornerCoord = abs(coord) - (halfSize - float2(radius));
        if (cornerCoord.x >= 0.0 || cornerCoord.y >= 0.0) {
            return sign(coord) * normalize(max(cornerCoord, 0.0));
        } else {
            float gradX = step(cornerCoord.y, cornerCoord.x);
            return sign(coord) * float2(gradX, 1.0 - gradX);
        }
    }

    float circleMap(float x) {
        return 1.0 - sqrt(max(0.0, 1.0 - x * x));
    }

    half4 main(float2 coord) {
        if (size.x <= 0.0 || size.y <= 0.0 || refractionHeight <= 0.0) {
            return content.eval(coord);
        }
        float2 halfSize = size * 0.5;
        float2 centeredCoord = (coord + offset) - halfSize;
        float radius = radiusAt(coord, cornerRadii);
        
        float sd = sdRoundedRect(centeredCoord, halfSize, radius);
        if (-sd >= refractionHeight) {
            return content.eval(coord);
        }
        sd = min(sd, 0.0);
        
        float ratio = clamp(1.0 - -sd / refractionHeight, 0.0, 1.0);
        float d = circleMap(ratio) * refractionAmount;
        float gradRadius = min(radius * 1.5, min(halfSize.x, halfSize.y));
        float2 baseGrad = gradSdRoundedRect(centeredCoord, halfSize, gradRadius);
        float lenGrad = length(baseGrad);
        float2 grad = lenGrad > 0.001 ? normalize(baseGrad + depthEffect * normalize(centeredCoord)) : float2(0.0);
        
        float2 refractedCoord = coord + d * grad;
        refractedCoord = clamp(refractedCoord, float2(0.0, 0.0), size);
        return content.eval(refractedCoord);
    }
"""

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

    // GPU-accelerated AGSL runtime shader for optical refraction distortion on the floating card (Android 13+ / 16)
    val cardRefractionShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { RuntimeShader(FLOATING_CARD_REFRACTION_SHADER) }.getOrNull()
        } else null
    }

    // Textbook pill (capsule / stadium) shape for both the floating dock and the sliding indicator
    val dockPillShape = CircleShape
    val pillShape = CircleShape

    // Track layout bounds of each tab
    val tabBounds = remember { mutableStateMapOf<Int, TabBounds>() }

    // Synchronized spring physics matching the screen parallax transition
    val navSpring = remember {
        spring<Float>(
            dampingRatio = 0.78f,
            stiffness = 420f
        )
    }

    // Dynamic target bounds based on current tab selection
    val currentBounds = tabBounds[selectedTab]
    val targetX = currentBounds?.x ?: 0f
    val targetY = currentBounds?.y ?: 0f
    val targetW = currentBounds?.width ?: 0f
    val targetH = currentBounds?.height ?: 0f

    var hasInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(currentBounds) {
        if (!hasInitialized && currentBounds != null && currentBounds.width > 0f) {
            hasInitialized = true
        }
    }

    // Smoothly animated properties driven on frame 0 in lockstep with tab changes
    val animX by animateFloatAsState(
        targetValue = targetX,
        animationSpec = if (!hasInitialized) snap() else navSpring,
        label = "pillX"
    )
    val animY by animateFloatAsState(
        targetValue = targetY,
        animationSpec = if (!hasInitialized) snap() else navSpring,
        label = "pillY"
    )
    val animW by animateFloatAsState(
        targetValue = targetW,
        animationSpec = if (!hasInitialized) snap() else navSpring,
        label = "pillW"
    )
    val animH by animateFloatAsState(
        targetValue = targetH,
        animationSpec = if (!hasInitialized) snap() else navSpring,
        label = "pillH"
    )

    // Interactive touch coordinates for LastWave radial glow effect
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var isTouching by remember { mutableStateOf(false) }

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
            // Optical lens refraction distortion for the entire floating card container
            .graphicsLayer {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && cardRefractionShader != null && size.width > 0f && size.height > 0f) {
                    val pillRadius = size.height / 2f
                    cardRefractionShader.setFloatUniform("size", size.width, size.height)
                    cardRefractionShader.setFloatUniform("offset", 0f, 0f)
                    cardRefractionShader.setFloatUniform("cornerRadii", pillRadius, pillRadius, pillRadius, pillRadius)
                    cardRefractionShader.setFloatUniform("refractionHeight", size.height * 0.45f)
                    cardRefractionShader.setFloatUniform("refractionAmount", -18f)
                    cardRefractionShader.setFloatUniform("depthEffect", 0.18f)
                    renderEffect = RenderEffect.createRuntimeShaderEffect(cardRefractionShader, "content").asComposeRenderEffect()
                }
            }
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
            // Specular rim stroke with authentic glass reflections
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x99FFFFFF), // Crisp specular white reflection at top
                            Color(0x35FFFFFF), // Translucent side reflection
                            Color(0x18FFFFFF), // Deep lower refraction
                            Color(0x40FFFFFF)  // Bottom bounce highlight
                        )
                    )
                ),
                dockPillShape
            )
            // Interactive touch inspector for fluid radial light glow
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
        // Internal specular glass sheen
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(dockPillShape)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x2EFFFFFF),
                        0.22f to Color(0x12FFFFFF),
                        0.60f to Color.Transparent
                    )
                )
        )

        // Radial liquid glass glow on touch
        if (isTouching && touchPosition != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(dockPillShape)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x28FFFFFF),
                                    Color(0x10FFFFFF),
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
            // The single translucent frosted liquid glass indicator pill
            if (hasInitialized && animW > 0f) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = animX.roundToInt(),
                                y = animY.roundToInt()
                            )
                        }
                        .size(
                            width = with(density) { animW.toDp() },
                            height = with(density) { animH.toDp() }
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
                                    Color(0x20FFFFFF)  // Luminous translucent white bottom
                                )
                            )
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xD0FFFFFF), // Brilliant specular top rim
                                        Color(0x45FFFFFF)  // Soft lower rim
                                    )
                                )
                            ),
                            pillShape
                        )
                ) {
                    // Optical lens radial caustic sheen inside the sliding pill
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(pillShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x40FFFFFF), // Bright specular center
                                        Color(0x15FFFFFF), // Soft translucent mid
                                        Color.Transparent
                                    ),
                                    center = Offset(animW * 0.5f, animH * 0.25f),
                                    radius = maxOf(animW, animH)
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
                                if (size.width > 0 && size.height > 0) {
                                    tabBounds[index] = TabBounds(
                                        x = pos.x,
                                        y = pos.y,
                                        width = size.width.toFloat(),
                                        height = size.height.toFloat()
                                    )
                                }
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
                                enter = fadeIn(animationSpec = tween(180, delayMillis = 20)) +
                                        expandHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = 0.78f,
                                                stiffness = 420f
                                            )
                                        ),
                                exit = fadeOut(animationSpec = tween(120)) +
                                        shrinkHorizontally(
                                            animationSpec = spring(
                                                dampingRatio = 0.78f,
                                                stiffness = 420f
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


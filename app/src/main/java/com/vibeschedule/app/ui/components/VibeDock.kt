package com.vibeschedule.app.ui.components

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vibeschedule.app.ui.util.Haptics
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeChild
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

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

private const val LIQUID_GLASS_PILL_SHADER = """
    uniform float2 uResolution;
    uniform float uMotion;
    uniform float uMotionDir;
    uniform float2 uTouchPos;
    uniform float uTouchActive;

    float sdCapsule(float2 p, float2 halfSize, float r) {
        float2 d = abs(p) - (halfSize - float2(r));
        return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
    }

    half4 main(float2 fragCoord) {
        if (uResolution.x <= 0.0 || uResolution.y <= 0.0) {
            return half4(0.0, 0.0, 0.0, 0.0);
        }

        float2 halfSize = uResolution * 0.5;
        float2 p = fragCoord - halfSize;
        float radius = min(halfSize.x, halfSize.y);

        float dist = sdCapsule(p, halfSize, radius);
        if (dist > 0.5) {
            return half4(0.0, 0.0, 0.0, 0.0);
        }

        float innerDist = -dist;
        float edgeAa = smoothstep(0.5, -0.5, dist);
        float dNorm = clamp(innerDist / 6.0, 0.0, 1.0);
        float edgeSubtle = smoothstep(0.0, 1.0, 1.0 - dNorm) * 0.04;
        float totalA = (0.10 + edgeSubtle) * edgeAa;

        return half4(totalA, totalA, totalA, totalA);
    }
"""

private const val LIQUID_GLASS_DOCK_SHADER = """
    uniform float2 uResolution;
    uniform float uTouchActive;
    uniform float2 uTouchPos;

    float sdCapsule(float2 p, float2 halfSize, float r) {
        float2 d = abs(p) - (halfSize - float2(r));
        return min(max(d.x, d.y), 0.0) + length(max(d, 0.0)) - r;
    }

    float2 gradCapsule(float2 p, float2 halfSize, float r) {
        float2 d = abs(p) - (halfSize - float2(r));
        if (d.x > 0.0 || d.y > 0.0) {
            return sign(p) * normalize(max(d, 0.0));
        }
        float gx = step(d.y, d.x);
        return sign(p) * float2(gx, 1.0 - gx);
    }

    half4 main(float2 fragCoord) {
        if (uResolution.x <= 0.0 || uResolution.y <= 0.0) {
            return half4(0.06, 0.07, 0.10, 0.35);
        }

        float2 halfSize = uResolution * 0.5;
        float2 p = fragCoord - halfSize;
        float radius = min(halfSize.x, halfSize.y);

        float dist = sdCapsule(p, halfSize, radius);
        if (dist > 0.5) return half4(0.0, 0.0, 0.0, 0.0);

        float innerDist = -dist;
        float dNorm = clamp(innerDist / 20.0, 0.0, 1.0);
        float z = sqrt(1.0 - (1.0 - dNorm) * (1.0 - dNorm));

        float2 normGrad = gradCapsule(p, halfSize, radius);
        float2 normal2D = normalize(normGrad * (1.0 - z * 0.4) + (p / halfSize) * 0.4);

        float fresnel = pow(1.0 - z, 2.5);
        float rim = smoothstep(0.0, 3.0, innerDist) * fresnel * 0.45;

        float2 lightDir = normalize(float2(-0.2, -0.98));
        float spec = pow(max(0.0, dot(normal2D, -lightDir)), 16.0) * 0.40;
        float bottom = max(0.0, dot(normal2D, float2(0.0, 1.0))) * pow(1.0 - dNorm, 2.0) * 0.22;

        float touchGlow = 0.0;
        if (uTouchActive > 0.01) {
            float tDist = length(fragCoord - uTouchPos);
            touchGlow = exp(-tDist * tDist / 4500.0) * uTouchActive * 0.35;
        }

        float vert = fragCoord.y / uResolution.y;
        float baseAlpha = mix(0.14, 0.08, vert);

        float rCol = mix(0.10, 0.06, vert) + rim * 0.65 + spec + touchGlow;
        float gCol = mix(0.12, 0.08, vert) + rim * 0.65 + spec + touchGlow;
        float bCol = mix(0.18, 0.12, vert) + rim * 0.85 + spec + touchGlow;
        float totalAlpha = clamp(baseAlpha + rim + spec * 0.5 + bottom + touchGlow, 0.0, 0.68);
        float edgeAa = smoothstep(0.5, -0.5, dist);
        float finalA = totalAlpha * edgeAa;

        return half4(rCol * finalA, gCol * finalA, bCol * finalA, finalA);
    }
"""

@Composable
fun VibeDock(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    val view = LocalView.current
    val density = LocalDensity.current

    val items = remember {
        listOf(
            NavTabItem("Home", Icons.Rounded.Home, Icons.Outlined.Home),
            NavTabItem("Schedules", Icons.Rounded.Schedule, Icons.Outlined.Schedule),
            NavTabItem("Settings", Icons.Rounded.Settings, Icons.Outlined.Settings)
        )
    }

    val pillShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { RuntimeShader(LIQUID_GLASS_PILL_SHADER) }.getOrNull()
        } else null
    }

    val dockShader = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            runCatching { RuntimeShader(LIQUID_GLASS_DOCK_SHADER) }.getOrNull()
        } else null
    }

    val dockPillShape = CircleShape
    val pillShape = CircleShape

    val tabBounds = remember { mutableStateMapOf<Int, TabBounds>() }

    val navSpring = remember {
        spring<Float>(
            dampingRatio = 0.78f,
            stiffness = 420f
        )
    }

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

    val deltaX = targetX - animX
    val motionFactor = if (hasInitialized) (abs(deltaX) / 75f).coerceIn(0f, 1.2f) else 0f
    val motionDir = if (hasInitialized && abs(deltaX) > 0.5f) sign(deltaX) else 0f
    val stretchX = motionFactor * 0.25f
    val squashY = motionFactor * 0.10f
    val fluidLagX = -motionDir * motionFactor * 3.5f

    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var isTouching by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = dockPillShape,
                clip = false,
                ambientColor = Color(0x60000000),
                spotColor = Color(0x90000000)
            )
            .clip(dockPillShape)
            .then(
                if (hazeState != null) {
                    Modifier.hazeChild(
                        state = hazeState,
                        shape = dockPillShape,
                        style = HazeStyle(
                            blurRadius = 24.dp,
                            tint = Color(0x35101322),
                            noiseFactor = 0.06f
                        )
                    )
                } else Modifier
            )
            .drawBehind {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && dockShader != null && size.width > 0f && size.height > 0f) {
                    dockShader.setFloatUniform("uResolution", size.width, size.height)
                    dockShader.setFloatUniform("uTouchActive", if (isTouching) 1f else 0f)
                    dockShader.setFloatUniform("uTouchPos", touchPosition?.x ?: (size.width / 2f), touchPosition?.y ?: (size.height / 2f))
                    drawRoundRect(
                        brush = ShaderBrush(dockShader),
                        size = size,
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                    )
                } else {
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x401C2030),
                                Color(0x20111422),
                                Color(0x35171A2A)
                            )
                        ),
                        size = size,
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                    )
                }
            }
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x90FFFFFF),
                            Color(0x30FFFFFF),
                            Color(0x18FFFFFF),
                            Color(0x38FFFFFF)
                        )
                    )
                ),
                dockPillShape
            )
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
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(dockPillShape)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0x28FFFFFF),
                        0.25f to Color(0x10FFFFFF),
                        0.65f to Color.Transparent
                    )
                )
        )

        if (isTouching && touchPosition != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(dockPillShape)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x24FFFFFF),
                                    Color(0x0CFFFFFF),
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

        Box(
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 5.dp)
        ) {
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
                            elevation = 2.dp,
                            shape = pillShape,
                            ambientColor = Color(0x15000000),
                            spotColor = Color(0x20000000)
                        )
                        .graphicsLayer {
                            scaleX = 1f + stretchX
                            scaleY = 1f - squashY
                            translationX = fluidLagX
                        }
                        .clip(pillShape)
                        .then(
                            if (hazeState != null) {
                                Modifier.hazeChild(
                                    state = hazeState,
                                    shape = pillShape,
                                    style = HazeStyle(
                                        blurRadius = 14.dp,
                                        tint = Color(0x15FFFFFF),
                                        noiseFactor = 0f
                                    )
                                )
                            } else Modifier
                        )
                        .drawBehind {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && pillShader != null && size.width > 0f && size.height > 0f) {
                                pillShader.setFloatUniform("uResolution", size.width, size.height)
                                pillShader.setFloatUniform("uMotion", motionFactor)
                                pillShader.setFloatUniform("uMotionDir", (deltaX / 75f).coerceIn(-1f, 1f))
                                pillShader.setFloatUniform("uTouchPos", touchPosition?.x ?: 0f, touchPosition?.y ?: 0f)
                                pillShader.setFloatUniform("uTouchActive", if (isTouching) 1f else 0f)
                                drawRoundRect(
                                    brush = ShaderBrush(pillShader),
                                    size = size,
                                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                )
                            } else {
                                drawRoundRect(
                                    brush = Brush.verticalGradient(
                                        listOf(
                                            Color(0x1CFFFFFF),
                                            Color(0x0EFFFFFF)
                                        )
                                    ),
                                    size = size,
                                    cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                                )
                            }
                        }
                        .border(
                            BorderStroke(
                                1.dp,
                                Color(0x35FFFFFF)
                            ),
                            pillShape
                        )
                )
            }

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
                    val contentScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.06f else 1.0f,
                        animationSpec = navSpring,
                        label = "tabContentScale"
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
                                        Haptics.tabChange(view)
                                        onTabSelected(index)
                                    }
                                }
                            )
                            .padding(
                                horizontal = if (isSelected) 16.dp else 13.dp,
                                vertical = 9.dp
                            )
                            .graphicsLayer {
                                scaleX = contentScale
                                scaleY = contentScale
                            },
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

// Backward-compatible alias
@Composable
fun FloatingLiquidGlassBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null
) {
    VibeDock(
        selectedTab = selectedTab,
        onTabSelected = onTabSelected,
        modifier = modifier,
        hazeState = hazeState
    )
}

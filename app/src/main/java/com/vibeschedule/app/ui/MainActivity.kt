package com.vibeschedule.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.vibeschedule.app.ui.components.FloatingLiquidGlassBottomBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.model.SoundMode
import com.vibeschedule.app.ui.components.AddEditScheduleDialog
import com.vibeschedule.app.ui.screens.HomeScreen
import com.vibeschedule.app.ui.screens.SchedulesScreen
import com.vibeschedule.app.ui.screens.SettingsScreen
import com.vibeschedule.app.ui.theme.VibeScheduleTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            VibeScheduleTheme {
                RequestNotificationPermission()
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.evaluateStatus()
    }

    @Composable
    private fun RequestNotificationPermission() {
        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {}
            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var previousTab by remember { mutableIntStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<ScheduleRule?>(null) }
    val currentSoundMode by viewModel.currentSoundMode.collectAsState()
    val hazeState = remember { HazeState() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 12.dp).size(38.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Vibration,
                                contentDescription = "VibeSchedule",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                title = {
                    Text(
                        text = "VibeSchedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    AssistChip(
                        modifier = Modifier.padding(end = 12.dp),
                        onClick = { viewModel.evaluateStatus() },
                        label = {
                            Text(
                                text = when (currentSoundMode) {
                                    SoundMode.VIBRATE -> "Vibrate"
                                    SoundMode.SILENT -> "Silent"
                                    SoundMode.NORMAL -> "Normal"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = when (currentSoundMode) {
                                    SoundMode.VIBRATE -> Icons.Rounded.Vibration
                                    SoundMode.SILENT -> Icons.Rounded.NotificationsOff
                                    SoundMode.NORMAL -> Icons.Rounded.Notifications
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = null
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Screen content container captured in real-time by Haze for authentic backdrop distortion & blur
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .haze(hazeState)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding()),
                    transitionSpec = {
                        val goingRight = targetState > previousTab
                        val slideSpring = spring<IntOffset>(
                            dampingRatio = 0.78f,
                            stiffness = 420f
                        )
                        val fadeSpec = tween<Float>(durationMillis = 200, easing = FastOutSlowInEasing)

                        val enter = slideInHorizontally(
                            animationSpec = slideSpring
                        ) { fullWidth ->
                            if (goingRight) (fullWidth * 0.20f).toInt() else (-fullWidth * 0.20f).toInt()
                        } + fadeIn(animationSpec = fadeSpec)

                        val exit = slideOutHorizontally(
                            animationSpec = slideSpring
                        ) { fullWidth ->
                            if (goingRight) (-fullWidth * 0.20f).toInt() else (fullWidth * 0.20f).toInt()
                        } + fadeOut(animationSpec = fadeSpec)

                        enter togetherWith exit
                    },
                    label = "tabTransition"
                ) { target ->
                    when (target) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToSchedules = { previousTab = selectedTab; selectedTab = 1 }
                        )
                        1 -> SchedulesScreen(
                            viewModel = viewModel,
                            onEditRule = { rule -> editingRule = rule; showDialog = true }
                        )
                        2 -> SettingsScreen()
                    }
                }
            }

            // Floating Action Button for Schedules Tab (Tab 1), positioned cleanly above the floating bar
            AnimatedVisibility(
                visible = selectedTab == 1,
                enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.8f),
                exit = fadeOut(animationSpec = tween(120)) + scaleOut(targetScale = 0.8f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 86.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = { editingRule = null; showDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Schedule", style = MaterialTheme.typography.labelLarge) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                )
            }

            // Truly Floating Liquid Glass Bottom Navigation Bar with real-time backdrop blur & optical distortion
            FloatingLiquidGlassBottomBar(
                selectedTab = selectedTab,
                onTabSelected = { newTab ->
                    previousTab = selectedTab
                    selectedTab = newTab
                },
                hazeState = hazeState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            )
        }

        if (showDialog) {
            AddEditScheduleDialog(
                initialRule = editingRule,
                onDismiss = { showDialog = false },
                onSave = { rule ->
                    if (editingRule == null) {
                        viewModel.addSchedule(rule)
                        Toast.makeText(context, "Added '${rule.title}'", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.updateSchedule(rule)
                        Toast.makeText(context, "Updated '${rule.title}'", Toast.LENGTH_SHORT).show()
                    }
                    showDialog = false
                }
            )
        }
    }
}

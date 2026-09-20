package com.vibeschedule.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.vibeschedule.app.model.ScheduleRule
import com.vibeschedule.app.ui.components.AddEditScheduleDialog
import com.vibeschedule.app.ui.components.LiquidTabSwitcher
import com.vibeschedule.app.ui.screens.HomeScreen
import com.vibeschedule.app.ui.screens.SchedulesScreen
import com.vibeschedule.app.ui.theme.AccentPurple
import com.vibeschedule.app.ui.theme.BackgroundMeshBrush
import com.vibeschedule.app.ui.theme.GlassBorderBrush
import com.vibeschedule.app.ui.theme.VibeScheduleTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            VibeScheduleTheme(darkTheme = true) {
                RequestNotificationPermission()
                MainAppScreen(viewModel = viewModel)
            }
        }
    }

    @Composable
    private fun RequestNotificationPermission() {
        val context = LocalContext.current
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* Granted or denied */ }

            LaunchedEffect(Unit) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Home, 1 = Schedules

    var showDialog by remember { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<ScheduleRule?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundMeshBrush)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Minimal Top Bar: Icon at Left, Liquid Tab Switcher at Center
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Left Icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x18FFFFFF))
                            .border(1.dp, GlassBorderBrush, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "VibeSchedule",
                            tint = AccentPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Center Liquid Tab Switcher
                    LiquidTabSwitcher(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        tabs = listOf("Home", "Schedules"),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            },
            floatingActionButton = {
                if (selectedTab == 1) {
                    FloatingActionButton(
                        onClick = {
                            editingRule = null
                            showDialog = true
                        },
                        containerColor = AccentPurple,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Schedule")
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "tabTransition"
                ) { target ->
                    when (target) {
                        0 -> HomeScreen(
                            viewModel = viewModel,
                            onNavigateToSchedules = { selectedTab = 1 }
                        )
                        1 -> SchedulesScreen(
                            viewModel = viewModel,
                            onEditRule = { rule ->
                                editingRule = rule
                                showDialog = true
                            }
                        )
                    }
                }
            }
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

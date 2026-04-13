package vn.io.litever.alarm.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.alarm.features.alarm.ui.alarmGraph
import vn.io.litever.alarm.features.alarm.ui.alarmListRoute
import vn.io.litever.alarm.features.alarm.ui.alarmEditRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import vn.io.litever.alarm.features.settings.ui.settingsGraph
import vn.io.litever.alarm.features.settings.ui.settingsRoute

import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.alarm.core.datastore.AlarmPreferencesDataSource
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesDataSource: AlarmPreferencesDataSource
) : ViewModel() {
    val themeMode = preferencesDataSource.themeMode
    val colorPalette = preferencesDataSource.colorPalette
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLockScreenBypass()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleLockScreenBypass()
        
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = "SYSTEM")
            val colorPalette by viewModel.colorPalette.collectAsState(initial = "DYNAMIC")
            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }
            
            ReMindTheme(darkTheme = darkTheme, colorPalette = colorPalette) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    
                    // Observe Global Ringing State
                    val alarmRingManager = dagger.hilt.EntryPoints.get(
                        applicationContext,
                        vn.io.litever.remind.core.reminder.di.ReminderRingManagerEntryPoint::class.java
                    ).reminderRingManager()
                    val ringingAlarmId by alarmRingManager.ringingAlarmId.collectAsState()
                    
                    LaunchedEffect(ringingAlarmId) {
                        ringingAlarmId?.let { id ->
                            navController.navigate("alarm_ringing_route/$id") {
                                launchSingleTop = true
                            }
                        }
                    }

                    Scaffold(
                        bottomBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            val isBottomBarVisible = currentRoute == vn.io.litever.alarm.features.alarm.ui.alarmListRoute || 
                                currentRoute == vn.io.litever.alarm.features.settings.ui.settingsRoute

                            if (isBottomBarVisible) {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Filled.Alarm, contentDescription = "Alarm") },
                                        label = { Text("Báo thức") },
                                        selected = currentRoute == vn.io.litever.alarm.features.alarm.ui.alarmListRoute,
                                        onClick = {
                                            navController.navigate(vn.io.litever.alarm.features.alarm.ui.alarmListRoute) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                                        label = { Text("Cài đặt") },
                                        selected = currentRoute == vn.io.litever.alarm.features.settings.ui.settingsRoute,
                                        onClick = {
                                            navController.navigate(vn.io.litever.alarm.features.settings.ui.settingsRoute) {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier.padding(bottom = paddingValues.calculateBottomPadding())
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = vn.io.litever.alarm.features.alarm.ui.alarmListRoute
                            ) {
                                alarmGraph(
                                    onNavigateToEdit = { id ->
                                        navController.navigate("alarm_edit_route/$id")
                                    },
                                    onNavigateToRingtoneSelection = { currentUri ->
                                        // Set initial URI in the CURRENT entry so the next screen can read it from PREVIOUS entry
                                        navController.currentBackStackEntry?.savedStateHandle?.set("initialUri", currentUri)
                                        navController.navigate(vn.io.litever.alarm.features.alarm.ui.ringtoneSelectionRoute)
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    navController = navController
                                )
                                settingsGraph()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleLockScreenBypass() {
        val isRingingIntent = intent?.data?.toString()?.contains("alarm/ring") == true
        if (isRingingIntent) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }
        }
    }
}
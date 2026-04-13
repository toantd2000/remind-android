package vn.io.litever.remind.app

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
import vn.io.litever.remind.features.reminder.ui.reminderGraph
import vn.io.litever.remind.features.reminder.ui.reminderListRoute
import vn.io.litever.remind.features.reminder.ui.reminderEditRoute
import vn.io.litever.remind.features.reminder.ui.ringtoneSelectionRoute
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
import vn.io.litever.remind.features.settings.ui.settingsGraph
import vn.io.litever.remind.features.settings.ui.settingsRoute
import vn.io.litever.remind.features.settings.ui.navigateToGeneralSettings
import vn.io.litever.remind.features.settings.ui.navigateToQA
import vn.io.litever.remind.features.settings.ui.navigateToPermissions
import vn.io.litever.remind.features.settings.ui.navigateToAlarmSettings

import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.remind.core.datastore.ReminderPreferencesDataSource
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesDataSource: ReminderPreferencesDataSource
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
                    val reminderRingManager = dagger.hilt.EntryPoints.get(
                        applicationContext,
                        vn.io.litever.remind.core.reminder.di.ReminderRingManagerEntryPoint::class.java
                    ).reminderRingManager()
                    val ringingReminderId by reminderRingManager.ringingReminderId.collectAsState()
                    
                    LaunchedEffect(ringingReminderId) {
                        ringingReminderId?.let { id ->
                            navController.navigate("reminder_ringing_route/$id") {
                                launchSingleTop = true
                            }
                        }
                    }

                    Scaffold(
                        bottomBar = {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentRoute = navBackStackEntry?.destination?.route

                            val isBottomBarVisible = currentRoute == reminderListRoute || 
                                currentRoute == settingsRoute

                            if (isBottomBarVisible) {
                                NavigationBar {
                                    NavigationBarItem(
                                        icon = { Icon(Icons.Filled.Alarm, contentDescription = "Reminder") },
                                        label = { Text("Nhắc nhở") },
                                        selected = currentRoute == reminderListRoute,
                                        onClick = {
                                            navController.navigate(reminderListRoute) {
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
                                        selected = currentRoute == settingsRoute,
                                        onClick = {
                                            navController.navigate(settingsRoute) {
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
                                startDestination = reminderListRoute
                            ) {
                                reminderGraph(
                                    onNavigateToEdit = { id ->
                                        navController.navigate("reminder_edit_route/$id")
                                    },
                                    onNavigateToRingtoneSelection = { currentUri ->
                                        // Set initial URI in the CURRENT entry so the next screen can read it from PREVIOUS entry
                                        navController.currentBackStackEntry?.savedStateHandle?.set("initialUri", currentUri)
                                        navController.navigate(ringtoneSelectionRoute)
                                    },
                                    onNavigateBack = {
                                        navController.popBackStack()
                                    },
                                    navController = navController
                                )
                                settingsGraph(
                                    onNavigateToGeneralSettings = { navController.navigateToGeneralSettings() },
                                    onNavigateToQA = { navController.navigateToQA() },
                                    onNavigateToPermissions = { navController.navigateToPermissions() },
                                    onNavigateToAlarmSettings = { navController.navigateToAlarmSettings() },
                                    onNavigateBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleLockScreenBypass() {
        val isRingingIntent = intent?.data?.toString()?.contains("remind/ring") == true
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
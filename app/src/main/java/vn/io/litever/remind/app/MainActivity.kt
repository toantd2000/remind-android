package vn.io.litever.remind.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import vn.io.litever.remind.core.designsystem.components.BrandLogo
import vn.io.litever.remind.core.designsystem.components.BrandingSplashScreen
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
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import vn.io.litever.remind.features.settings.ui.settingsGraph
import vn.io.litever.remind.features.settings.ui.settingsRoute
import vn.io.litever.remind.features.settings.ui.navigateToGeneralSettings
import vn.io.litever.remind.features.settings.ui.navigateToQA
import vn.io.litever.remind.features.settings.ui.navigateToPermissions
import vn.io.litever.remind.features.settings.ui.navigateToAlarmSettings
import vn.io.litever.remind.features.settings.ui.navigateToLicenses
import vn.io.litever.remind.features.settings.ui.navigateToUpdateHistory
import vn.io.litever.remind.core.designsystem.components.ReMindLogo
import vn.io.litever.remind.features.mission.ui.missionGraph
import vn.io.litever.remind.features.mission.ui.navigateToTypingMissionConfig
import vn.io.litever.remind.features.mission.ui.navigateToPhraseSelection
import vn.io.litever.remind.features.mission.ui.navigateToMissionRinging


import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import vn.io.litever.remind.core.datastore.ReminderPreferencesDataSource
import javax.inject.Inject
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.res.Configuration
import androidx.compose.ui.res.stringResource
import java.util.Locale

import vn.io.litever.remind.core.domain.repository.ReminderRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesDataSource: ReminderPreferencesDataSource,
    private val reminderRingManager: vn.io.litever.remind.core.reminder.ReminderRingManager,
    reminderRepository: ReminderRepository
) : ViewModel() {
    val themeMode = preferencesDataSource.themeMode
    val colorPalette = preferencesDataSource.colorPalette
    val language = preferencesDataSource.language
    val acknowledgingReminderId = reminderRingManager.acknowledgingReminderId

    val activeSnoozingReminderId = reminderRepository.getAllReminders()
        .map { reminders -> 
            reminders.firstOrNull { it.snoozeNextTriggerTime != null }?.id 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val missedReminderId = reminderRepository.getAllReminders()
        .map { reminders -> 
            reminders.firstOrNull { it.isMissed }?.id 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
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
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        handleLockScreenBypass()
        
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = "SYSTEM")
            val colorPalette by viewModel.colorPalette.collectAsState(initial = "DYNAMIC")
            val language by viewModel.language.collectAsState(
                initial = if (java.util.Locale.getDefault().language == "vi") "vi" else "en"
            )
            
            val context = LocalContext.current
            val localizedContext = remember(language) {
                val locale = Locale(language)
                Locale.setDefault(locale)
                val config = Configuration(context.resources.configuration)
                config.setLocale(locale)
                config.setLayoutDirection(locale)
                val configurationContext = context.createConfigurationContext(config)
                
                object : android.content.ContextWrapper(context) {
                    override fun getResources() = configurationContext.resources
                    override fun getAssets() = configurationContext.assets
                }
            }

            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }
            
            CompositionLocalProvider(LocalContext provides localizedContext) {
                ReMindTheme(darkTheme = darkTheme, colorPalette = colorPalette) {
                    var showBranding by remember { mutableStateOf(true) }

                    if (showBranding) {
                        BrandingSplashScreen(onFinished = { showBranding = false })
                    } else {
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
                        val activeSnoozingReminderId by viewModel.activeSnoozingReminderId.collectAsState()
                        val missedReminderId by viewModel.missedReminderId.collectAsState()
                        
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        val acknowledgingReminderId by viewModel.acknowledgingReminderId.collectAsState(initial = null as Long?)

                        LaunchedEffect(ringingReminderId, activeSnoozingReminderId, missedReminderId, acknowledgingReminderId) {
                            val idToRing = ringingReminderId ?: activeSnoozingReminderId
                            val idMessage = acknowledgingReminderId ?: missedReminderId
                            
                            val currentEntry = navController.currentBackStackEntry
                            val currentDest = currentEntry?.destination?.route
                            val currentId = currentEntry?.arguments?.getLong("reminderId")

                            if (idMessage != null) {
                                val isAlreadyMessageId = currentDest == "reminder_message_route/{reminderId}" && currentId == idMessage
                                
                                if (!isAlreadyMessageId) {
                                    navController.navigate("reminder_message_route/$idMessage") {
                                        popUpTo(reminderListRoute) {
                                            inclusive = false
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            } else if (idToRing != null) {
                                // NO OVERLAP RULE: Don't interrupt if user is in a mission
                                // But DO interrupt if user is just viewing a missed message
                                val isUserInMission = currentDest?.startsWith("mission_ringing_route") == true
                                
                                if (!isUserInMission) {
                                    val isAlreadyRingingId = currentDest == "reminder_ringing_route/{reminderId}" && currentId == idToRing
                                    if (!isAlreadyRingingId) {
                                        navController.navigate("reminder_ringing_route/$idToRing") {
                                            popUpTo(reminderListRoute) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            } else {
                                if (currentDest == "reminder_ringing_route/{reminderId}" ||
                                    currentDest == "reminder_message_route/{reminderId}") {
                                    // Only pop if we were blocking the app and the state is now cleared
                                    navController.popBackStack()
                                }
                            }
                        }

                        Scaffold(
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentRoute = navBackStackEntry?.destination?.route

                                val isBottomBarVisible = currentRoute == reminderListRoute || 
                                    currentRoute == settingsRoute

                                if (isBottomBarVisible) {
                                    NavigationBar {
                                        NavigationBarItem(
                                            icon = { Icon(Icons.Rounded.Alarm, contentDescription = "Reminder") },
                                            label = { Text(stringResource(R.string.navigation_reminders)) },
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
                                            icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                                            label = { Text(stringResource(R.string.navigation_settings)) },
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .consumeWindowInsets(paddingValues)
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
                                        onNavigateToSnoozeSettings = { enabled, interval, repeatCount ->
                                            navController.currentBackStackEntry?.savedStateHandle?.set("snoozeEnabled", enabled)
                                            navController.currentBackStackEntry?.savedStateHandle?.set("snoozeInterval", interval)
                                            navController.currentBackStackEntry?.savedStateHandle?.set("snoozeRepeatCount", repeatCount)
                                            navController.navigate(vn.io.litever.remind.features.reminder.ui.snoozeSettingsRoute)
                                        },
                                        onNavigateToPermissions = { 
                                            navController.navigateToPermissions()
                                        },
                                        onNavigateToMissionRinging = { reminderId ->
                                            navController.navigateToMissionRinging(reminderId)
                                        },
                                        onNavigateToMessage = { reminderId ->
                                            // Navigation handled globally
                                            // navController.navigate("reminder_message_route/$reminderId")
                                        },
                                        onNavigateBack = {
                                            navController.popBackStack()
                                        },
                                        onAddMissionClick = {
                                            // Handle showMissionSelection bottom sheet in ReminderEditRoute
                                            // or navigate if it's a separate screen. 
                                            // Actually, I'll pass a dummy here and handle it inside ReminderEditRoute.
                                        },
                                        onMissionClick = { mission ->
                                            if (mission.type == vn.io.litever.remind.core.model.MissionType.TYPING) {
                                                val config = mission.config as? vn.io.litever.remind.core.model.TypingMissionConfig
                                                navController.currentBackStackEntry?.savedStateHandle?.set("repetitions", mission.repeatCount)
                                                navController.currentBackStackEntry?.savedStateHandle?.set("selectedPhraseIds", config?.selectedPhraseIds ?: emptyList())
                                                navController.navigateToTypingMissionConfig(mission.reminderId)
                                            }
                                        },
                                        navController = navController
                                    )
                                    missionGraph(
                                        onNavigateToPhraseSelection = { reminderId, selectedIds ->
                                            navController.currentBackStackEntry?.savedStateHandle?.set("selectedPhraseIds", selectedIds)
                                            navController.navigateToPhraseSelection(reminderId)
                                        },
                                        onPhrasesSelected = { phraseIds ->
                                            navController.previousBackStackEntry?.savedStateHandle?.set("selectedPhraseIds", phraseIds)
                                            navController.popBackStack()
                                        },
                                        onSaveMission = { mission ->
                                            navController.previousBackStackEntry?.savedStateHandle?.set("updatedMission", mission)
                                            navController.popBackStack()
                                        },
                                        onMissionFinish = { id ->
                                            navController.popBackStack()
                                        },
                                        onBackClick = { navController.popBackStack() },
                                        navController = navController
                                    )
                                    settingsGraph(
                                        onNavigateToGeneralSettings = { navController.navigateToGeneralSettings() },
                                        onNavigateToQA = { navController.navigateToQA() },
                                        onNavigateToPermissions = { navController.navigateToPermissions() },
                                        onNavigateToAlarmSettings = { navController.navigateToAlarmSettings() },
                                        onNavigateToLicenses = { navController.navigateToLicenses() },
                                        onNavigateToUpdateHistory = { navController.navigateToUpdateHistory() },
                                        onNavigateBack = { navController.popBackStack() }

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
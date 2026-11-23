package vn.io.litever.remind.app

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.app.LocaleManager
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHost
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.domain.repository.ProRepository
import vn.io.litever.remind.core.alarm.ReminderSyncManager
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.designsystem.components.BrandingSplashScreen
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.core.model.BarcodeMissionConfig
import vn.io.litever.remind.core.model.MathMissionConfig
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.RandomMissionConfig
import vn.io.litever.remind.core.model.ShakeMissionConfig
import vn.io.litever.remind.core.model.ShakeSensitivity
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.core.model.TypingMode
import vn.io.litever.remind.features.alarms.ui.AlarmListRoute
import vn.io.litever.remind.features.alarms.ui.alarmGraph
import vn.io.litever.remind.features.alarms.ui.ringtoneSelectionRoute
import vn.io.litever.remind.features.mission.ui.missionGraph
import vn.io.litever.remind.features.mission.ui.navigateToBarcodeMissionConfig
import vn.io.litever.remind.features.mission.ui.navigateToMathMissionConfig
import vn.io.litever.remind.features.mission.ui.navigateToMemoryGameConfig
import vn.io.litever.remind.features.mission.ui.navigateToMissionRinging
import vn.io.litever.remind.features.mission.ui.navigateToPhraseSelection
import vn.io.litever.remind.features.mission.ui.navigateToRandomMissionConfig
import vn.io.litever.remind.features.mission.ui.navigateToShakeMissionConfig
import vn.io.litever.remind.features.mission.ui.navigateToTypingMissionConfig
import vn.io.litever.remind.features.reminders.ui.RemindersListRoute
import vn.io.litever.remind.features.reminders.ui.navigateToReminderEdit
import vn.io.litever.remind.features.reminders.ui.remindersGraph
import vn.io.litever.remind.features.settings.ui.navigateToAttributions
import vn.io.litever.remind.features.settings.ui.navigateToGeneralSettings
import vn.io.litever.remind.features.settings.ui.navigateToLicenses
import vn.io.litever.remind.features.settings.ui.navigateToPaywall
import vn.io.litever.remind.features.settings.ui.navigateToPermissions
import vn.io.litever.remind.features.settings.ui.settingsGraph
import vn.io.litever.remind.features.settings.ui.settingsRoute
import vn.io.litever.remind.app.ui.components.ExitAppDialog
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesDataSource: AlarmPreferencesDataSource,
    private val alarmRingManager: vn.io.litever.remind.core.alarm.AlarmRingManager,
    private val reminderAlertManager: vn.io.litever.remind.core.alarm.alert.ReminderAlertManager,
    private val missedAlarmRepository: vn.io.litever.remind.core.domain.repository.MissedAlarmRepository,
    private val alarmSyncManager: vn.io.litever.remind.core.alarm.AlarmSyncManager,
    private val reminderSyncManager: ReminderSyncManager,
    alarmRepository: AlarmRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            alarmSyncManager.sync()
            reminderSyncManager.sync()
        }
    }

    val themeMode = preferencesDataSource.themeMode
    val colorPalette = preferencesDataSource.colorPalette
    val language = preferencesDataSource.language

    val isAdFreeActive: StateFlow<Boolean> = combine(
        preferencesDataSource.isProUser,
        preferencesDataSource.adsDisabledUntil
    ) { isPro, disabledUntil ->
        isPro || disabledUntil > System.currentTimeMillis()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val ringingAlarmId = alarmRingManager.ringingAlarmId
    val ringingReminderId = reminderAlertManager.playbackState
        .map { state ->
            if (state is vn.io.litever.remind.core.alarm.alert.AlertPlaybackState.Playing && state.isPersistent) {
                state.reminderId
            } else {
                null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val activeSnoozingAlarmId = alarmRepository.getAllAlarms()
        .map { alarms ->
            alarms.firstOrNull { it.snoozeNextTriggerTime != null }?.id
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val missedAlarms = missedAlarmRepository.getAllMissedAlarms()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun dismissMissedAlarms() {
        viewModelScope.launch {
            missedAlarmRepository.deleteAllMissedAlarms()
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var adManager: AdManager

    @Inject
    lateinit var proRepository: ProRepository

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLockScreenBypass()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        adManager.initialize()
        proRepository.startConnection()

        handleLockScreenBypass()

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        window.isNavigationBarContrastEnforced = false
        @Suppress("DEPRECATION")
        window.isStatusBarContrastEnforced = false
        window.attributes.layoutInDisplayCutoutMode =
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        setContent {
            val themeMode by viewModel.themeMode.collectAsState(initial = "SYSTEM")
            val colorPalette by viewModel.colorPalette.collectAsState(initial = "BLACK")
            val language by viewModel.language.collectAsState(
                initial = if (LocalLocale.current.platformLocale.language == "vi") "vi" else "en"
            )

            LaunchedEffect(language) {
                val localeManager = getSystemService(Context.LOCALE_SERVICE) as LocaleManager
                localeManager.applicationLocales = LocaleList.forLanguageTags(language)
            }

            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            CompositionLocalProvider(
                LocalAdManager provides adManager
            ) {
                ReMindTheme(darkTheme = darkTheme, colorPalette = colorPalette) {
                    var showBranding by remember { mutableStateOf(true) }

                    if (showBranding) {
                        BrandingSplashScreen(onFinished = { showBranding = false })
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            val navController = rememberNavController()
                            val snackbarHostState = remember { SnackbarHostState() }

                            // Observe Global Ringing State
                            val alarmRingManagerEntryPoint = dagger.hilt.EntryPoints.get(
                                applicationContext,
                                vn.io.litever.remind.core.alarm.di.AlarmRingManagerEntryPoint::class.java
                            )
                            val alarmRingManager = alarmRingManagerEntryPoint.alarmRingManager()
                            val ringingAlarmId by alarmRingManager.ringingAlarmId.collectAsState()
                            val activeSnoozingalarmId by viewModel.activeSnoozingAlarmId.collectAsState()
                            val ringingReminderId by viewModel.ringingReminderId.collectAsState()
                            val missedAlarms by viewModel.missedAlarms.collectAsState()

                            val navBackStackEntry by navController.currentBackStackEntryAsState()

                            if (missedAlarms.isNotEmpty()) {
                                vn.io.litever.remind.features.alarms.ui.components.MissedAlarmDialog(
                                    missedAlarms = missedAlarms,
                                    onDismiss = { viewModel.dismissMissedAlarms() }
                                )
                            }

                            val isNavReady = navBackStackEntry != null
                            var lastNavigatedRingingReminderId by remember { mutableStateOf<Long?>(null) }

                            LaunchedEffect(
                                ringingAlarmId,
                                activeSnoozingalarmId,
                                ringingReminderId,
                                isNavReady
                            ) {
                                if (!isNavReady) return@LaunchedEffect

                                val currentEntry = navController.currentBackStackEntry
                                val currentDest = currentEntry?.destination?.route
                                val currentAlarmId = currentEntry?.arguments?.getLong("alarmId")
                                val currentReminderId = currentEntry?.arguments?.getLong("reminderId")

                                val idRingingAlarm = ringingAlarmId
                                val idRingingReminder = ringingReminderId
                                val idSnoozingAlarm = activeSnoozingalarmId

                                if (idRingingAlarm != null) {
                                    // PRIORITY 1: Actively ringing alarm
                                    val isUserInMission =
                                        currentDest?.startsWith("mission_ringing_route") == true

                                    if (!isUserInMission) {
                                        val isAlreadyRingingId =
                                            currentDest == "alarm_ringing_route/{alarmId}" && currentAlarmId == idRingingAlarm
                                        if (!isAlreadyRingingId) {
                                            navController.navigate("alarm_ringing_route/$idRingingAlarm") {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                } else if (idSnoozingAlarm != null) {
                                    // PRIORITY 2: Snoozing alarm countdown
                                    val isUserInMission =
                                        currentDest?.startsWith("mission_ringing_route") == true

                                    if (!isUserInMission) {
                                        val isAlreadyRingingId =
                                            currentDest == "alarm_ringing_route/{alarmId}" && currentAlarmId == idSnoozingAlarm
                                        if (!isAlreadyRingingId) {
                                            navController.navigate("alarm_ringing_route/$idSnoozingAlarm") {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                } else if (idRingingReminder != null) {
                                    // PRIORITY 3: Actively ringing reminder (Single Source of Truth)
                                    if (idRingingReminder != lastNavigatedRingingReminderId) {
                                        val isAlreadyRingingId =
                                            currentDest == "reminder_ringing_route/{reminderId}" && currentReminderId == idRingingReminder
                                        if (!isAlreadyRingingId) {
                                            lastNavigatedRingingReminderId = idRingingReminder
                                            navController.navigate("reminder_ringing_route/$idRingingReminder") {
                                                popUpTo(navController.graph.startDestinationId) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        } else {
                                            lastNavigatedRingingReminderId = idRingingReminder
                                        }
                                    }
                                } else {
                                    lastNavigatedRingingReminderId = null
                                    if (currentDest == "alarm_ringing_route/{alarmId}" ||
                                        currentDest == "reminder_ringing_route/{reminderId}"
                                    ) {
                                        // Only pop if we were on a ringing screen and the state is now cleared
                                        navController.popBackStack()
                                    }
                                }
                            }

                            val currentRoute = navBackStackEntry?.destination?.route

                            val isBottomBarVisible = currentRoute == AlarmListRoute ||
                                    currentRoute == RemindersListRoute ||
                                    currentRoute == settingsRoute

                            val isAdFreeActive by viewModel.isAdFreeActive.collectAsState()
                            val context = LocalContext.current
                            var showExitDialog by remember { mutableStateOf(false) }

                            BackHandler(enabled = currentRoute == AlarmListRoute) {
                                showExitDialog = true
                            }

                            if (showExitDialog) {
                                ExitAppDialog(
                                    onDismissRequest = { showExitDialog = false },
                                    onConfirmExit = {
                                        showExitDialog = false
                                        var currentContext = context
                                        while (currentContext is ContextWrapper) {
                                            if (currentContext is Activity) {
                                                currentContext.finish()
                                                return@ExitAppDialog
                                            }
                                            currentContext = currentContext.baseContext
                                        }
                                    },
                                    isAdFreeActive = isAdFreeActive
                                )
                            }

                            Scaffold(
                                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                                bottomBar = {
                                    if (isBottomBarVisible) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(NavigationBarDefaults.containerColor)
                                                .windowInsetsPadding(
                                                    WindowInsets.safeDrawing.only(
                                                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                                                    )
                                                )
                                        ) {
                                            ReMindNavigationBar(
                                                currentRoute = currentRoute,
                                                windowInsets = WindowInsets(0, 0, 0, 0),
                                                onNavigateToDestination = { route ->
                                                    navController.navigate(route) {
                                                        popUpTo(navController.graph.startDestinationId) {
                                                            saveState = true
                                                        }
                                                        launchSingleTop = true
                                                        restoreState = true
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            adManager.MicroAdStrip(
                                                placement = AdPlacement.SETTINGS_BOTTOM_STRIP,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            ) { paddingValues ->
                                    val layoutDirection = LocalLayoutDirection.current
                                    val actualPadding = PaddingValues(
                                        start = paddingValues.calculateStartPadding(layoutDirection),
                                        top = paddingValues.calculateTopPadding(),
                                        end = paddingValues.calculateEndPadding(layoutDirection),
                                        bottom = if (isBottomBarVisible) paddingValues.calculateBottomPadding() else LiteverTheme.spacing.none
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(actualPadding)
                                            .consumeWindowInsets(actualPadding)
                                    ) {
                                        NavHost(
                                            navController = navController,
                                            startDestination = AlarmListRoute
                                        ) {
                                            alarmGraph(
                                                onNavigateToEdit = { id ->
                                                    navController.navigate("alarm_edit_route/$id")
                                                },
                                                onNavigateToRingtoneSelection = { currentUri ->
                                                    // Set initial URI in the CURRENT entry so the next screen can read it from PREVIOUS entry
                                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                                        "initialUri",
                                                        currentUri
                                                    )
                                                    navController.navigate(ringtoneSelectionRoute)
                                                },
                                                onNavigateToPermissions = {
                                                    navController.navigateToPermissions()
                                                },
                                                onNavigateToMissionRinging = { alarmId ->
                                                    navController.navigateToMissionRinging(alarmId)
                                                },
                                                onNavigateBack = {
                                                    navController.popBackStack()
                                                },
                                                onMissionClick = { mission ->
                                                    if (mission.type == MissionType.TYPING) {
                                                        val config =
                                                            mission.config as? TypingMissionConfig
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "repetitions",
                                                            mission.repeatCount
                                                        )
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "selectedPhraseIds",
                                                            config?.selectedPhraseIds ?: emptyList()
                                                        )
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "typingMode",
                                                            config?.mode?.name ?: TypingMode.NORMAL.name
                                                        )
                                                        navController.navigateToTypingMissionConfig(
                                                            mission.alarmId
                                                        )
                                                    } else if (mission.type == MissionType.MATH) {
                                                        val config = mission.config as? MathMissionConfig
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "repetitions",
                                                            mission.repeatCount
                                                        )
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "mathDifficulty",
                                                            config?.difficulty?.name ?: vn.io.litever.remind.core.model.MathDifficulty.NORMAL.name
                                                        )
                                                        navController.navigateToMathMissionConfig(
                                                            mission.alarmId
                                                        )
                                                    } else if (mission.type == MissionType.MEMORY_FIND_COLOR_TILES) {
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "repetitions",
                                                            mission.repeatCount
                                                        )
                                                        navController.navigateToMemoryGameConfig(
                                                            mission.alarmId
                                                        )
                                                    } else if (mission.type == MissionType.SHAKE) {
                                                        val config = mission.config as? ShakeMissionConfig
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "shakeTargetCount",
                                                            config?.targetCount ?: 30
                                                        )
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "shakeSensitivity",
                                                            (config?.sensitivity ?: ShakeSensitivity.NORMAL).name
                                                        )
                                                        navController.navigateToShakeMissionConfig(mission.alarmId)
                                                    } else if (mission.type == MissionType.BARCODE) {
                                                        val config = mission.config as? BarcodeMissionConfig
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "targetBarcode",
                                                            config?.targetBarcode ?: ""
                                                        )
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "barcodeLabel",
                                                            config?.label ?: ""
                                                        )
                                                        navController.navigateToBarcodeMissionConfig(mission.alarmId)
                                                    } else if (mission.type == MissionType.RANDOM) {
                                                        val config = mission.config as? RandomMissionConfig
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "repetitions",
                                                            mission.repeatCount
                                                        )
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "randomDifficulty",
                                                            (config?.difficulty ?: vn.io.litever.remind.core.model.MathDifficulty.NORMAL).name
                                                        )
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "randomCandidates",
                                                            (config?.candidateTypes ?: emptyList()).map { it.name }
                                                        )
                                                        navController.navigateToRandomMissionConfig(mission.alarmId)
                                                    }
                                                },
                                                onNavigateToPreview = { id ->
                                                    navController.navigate("alarm_preview_route/$id")
                                                },
                                                onNavigateToMissionPreview = { id ->
                                                    navController.navigate("mission_ringing_route/$id?isPreview=true")
                                                },
                                                onNavigateToPaywall = { initialSlide ->
                                                    navController.navigateToPaywall(initialSlide)
                                                },
                                                navController = navController
                                            )
                                            missionGraph(
                                                onNavigateToPhraseSelection = { alarmId, selectedIds ->
                                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                                        "selectedPhraseIds",
                                                        selectedIds
                                                    )
                                                    navController.navigateToPhraseSelection(alarmId)
                                                },
                                                onPhrasesSelected = { phraseIds ->
                                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                                        "selectedPhraseIds",
                                                        phraseIds
                                                    )
                                                    navController.popBackStack()
                                                },
                                                onSaveMission = { mission ->
                                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                                        "updatedMission",
                                                        mission
                                                    )
                                                    navController.popBackStack()
                                                },
                                                onMissionFinish = { id ->
                                                    navController.popBackStack()
                                                },
                                                onBackClick = { navController.popBackStack() },
                                                onNavigateToPaywall = { initialSlide ->
                                                    navController.navigateToPaywall(initialSlide)
                                                },
                                                navController = navController
                                            )
                                            remindersGraph(
                                                onNavigateToEdit = { reminderId ->
                                                    navController.navigateToReminderEdit(reminderId)
                                                },
                                                onNavigateToRingtoneSelection = { currentUri ->
                                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                                        "initialUri",
                                                        currentUri
                                                    )
                                                    navController.navigate(ringtoneSelectionRoute)
                                                },
                                                onNavigateToPermissions = {
                                                    navController.navigateToPermissions()
                                                },
                                                onNavigateToPaywall = { initialSlide ->
                                                    navController.navigateToPaywall(initialSlide)
                                                },
                                                onNavigateBack = {
                                                    navController.popBackStack()
                                                },
                                                navController = navController
                                            )
                                            settingsGraph(
                                                onNavigateToGeneralSettings = { navController.navigateToGeneralSettings() },
                                                onNavigateToPermissions = { navController.navigateToPermissions() },
                                                onNavigateToAttributions = { navController.navigateToAttributions() },
                                                onNavigateToLicenses = { navController.navigateToLicenses() },
                                                onNavigateToPaywall = { initialSlide ->
                                                    navController.navigateToPaywall(initialSlide)
                                                },
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
        val uriString = intent?.data?.toString()
        val isRingingIntent = uriString?.contains("remind/ring") == true ||
                uriString?.contains("remind/ringing") == true
        if (isRingingIntent) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }
}

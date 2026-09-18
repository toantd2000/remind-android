package vn.io.litever.remind.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.io.litever.designsystem.components.snackbar.LvSnackbarHost
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.datastore.AlarmPreferencesDataSource
import vn.io.litever.remind.core.designsystem.components.BrandingSplashScreen
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.domain.repository.AlarmRepository
import vn.io.litever.remind.features.alarms.ui.AlarmListRoute
import vn.io.litever.remind.features.alarms.ui.alarmGraph
import vn.io.litever.remind.features.alarms.ui.ringtoneSelectionRoute
import vn.io.litever.remind.features.mission.ui.missionGraph
import vn.io.litever.remind.features.mission.ui.navigateToMemoryGameConfig
import vn.io.litever.remind.features.mission.ui.navigateToMissionRinging
import vn.io.litever.remind.features.mission.ui.navigateToPhraseSelection
import vn.io.litever.remind.features.mission.ui.navigateToTypingMissionConfig
import vn.io.litever.remind.features.settings.ui.navigateToAlarmSettings
import vn.io.litever.remind.features.settings.ui.navigateToAttributions
import vn.io.litever.remind.features.settings.ui.navigateToGeneralSettings
import vn.io.litever.remind.features.settings.ui.navigateToLicenses
import vn.io.litever.remind.features.settings.ui.navigateToPermissions
import vn.io.litever.remind.features.settings.ui.navigateToQA
import vn.io.litever.remind.features.settings.ui.settingsGraph
import vn.io.litever.remind.features.settings.ui.settingsRoute
import vn.io.litever.remind.features.today.ui.locationSearchRoute
import vn.io.litever.remind.features.today.ui.todayGraph
import vn.io.litever.remind.features.today.ui.todayRoute
import java.util.Locale
import javax.inject.Inject
import androidx.compose.ui.platform.LocalLocale
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.core.model.TypingMode

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesDataSource: AlarmPreferencesDataSource,
    private val alarmRingManager: vn.io.litever.remind.core.alarm.AlarmRingManager,
    private val missedAlarmRepository: vn.io.litever.remind.core.domain.repository.MissedAlarmRepository,
    private val alarmSyncManager: vn.io.litever.remind.core.alarm.AlarmSyncManager,
    alarmRepository: AlarmRepository
) : ViewModel() {
    init {
        viewModelScope.launch {
            alarmSyncManager.sync()
        }
    }

    val themeMode = preferencesDataSource.themeMode
    val colorPalette = preferencesDataSource.colorPalette
    val language = preferencesDataSource.language
    val acknowledgingAlarmId = alarmRingManager.acknowledgingAlarmId

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLockScreenBypass()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        adManager.initialize()

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
            val colorPalette by viewModel.colorPalette.collectAsState(initial = "RED")
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
                            val missedAlarms by viewModel.missedAlarms.collectAsState()

                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            navBackStackEntry?.destination?.route
                            val acknowledgingAlarmId by viewModel.acknowledgingAlarmId.collectAsState(
                                initial = null as Long?
                            )

                            if (missedAlarms.isNotEmpty()) {
                                vn.io.litever.remind.features.alarms.ui.components.MissedAlarmDialog(
                                    missedAlarms = missedAlarms,
                                    onDismiss = { viewModel.dismissMissedAlarms() }
                                )
                            }

                            val isNavReady = navBackStackEntry != null
                            LaunchedEffect(
                                ringingAlarmId,
                                activeSnoozingalarmId,
                                acknowledgingAlarmId,
                                isNavReady
                            ) {
                                if (!isNavReady) return@LaunchedEffect

                                val idToRing = ringingAlarmId ?: activeSnoozingalarmId
                                val idMessage = acknowledgingAlarmId

                                val currentEntry = navController.currentBackStackEntry
                                val currentDest = currentEntry?.destination?.route
                                val currentId = currentEntry?.arguments?.getLong("alarmId")

                                if (idMessage != null) {
                                    val isAlreadyMessageId =
                                        currentDest == "alarm_message_route/{alarmId}" && currentId == idMessage

                                    if (!isAlreadyMessageId) {
                                        navController.navigate("alarm_message_route/$idMessage") {
                                            popUpTo(AlarmListRoute) {
                                                inclusive = false
                                            }
                                            launchSingleTop = true
                                        }
                                    }
                                } else if (idToRing != null) {
                                    // NO OVERLAP RULE: Don't interrupt if user is in a mission
                                    // But DO interrupt if user is just viewing a missed message
                                    val isUserInMission =
                                        currentDest?.startsWith("mission_ringing_route") == true

                                    if (!isUserInMission) {
                                        val isAlreadyRingingId =
                                            currentDest == "alarm_ringing_route/{alarmId}" && currentId == idToRing
                                        if (!isAlreadyRingingId) {
                                            navController.navigate("alarm_ringing_route/$idToRing") {
                                                popUpTo(AlarmListRoute) {
                                                    inclusive = false
                                                }
                                                launchSingleTop = true
                                            }
                                        }
                                    }
                                } else {
                                    if (currentDest == "alarm_ringing_route/{alarmId}" ||
                                        currentDest == "alarm_message_route/{alarmId}"
                                    ) {
                                        // Only pop if we were blocking the app and the state is now cleared
                                        navController.popBackStack()
                                    }
                                }
                            }

                            val currentRoute = navBackStackEntry?.destination?.route

                            val isBottomBarVisible = currentRoute == AlarmListRoute ||
                                    currentRoute == settingsRoute ||
                                    currentRoute == todayRoute

                            Scaffold(
                                snackbarHost = { LvSnackbarHost(hostState = snackbarHostState) },
                                bottomBar = {
                                        if (isBottomBarVisible) {
                                            ReMindNavigationBar(
                                                currentRoute = currentRoute,
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
                                                onNavigateToMessage = { alarmId ->
                                                    // Navigation handled globally
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
                                                    } else if (mission.type == MissionType.MEMORY_FIND_COLOR_TILES) {
                                                        navController.currentBackStackEntry?.savedStateHandle?.set(
                                                            "repetitions",
                                                            mission.repeatCount
                                                        )
                                                        navController.navigateToMemoryGameConfig(
                                                            mission.alarmId
                                                        )
                                                    }
                                                },
                                                onNavigateToPreview = { id ->
                                                    navController.navigate("alarm_preview_route/$id")
                                                },
                                                onNavigateToMissionPreview = { id ->
                                                    navController.navigate("mission_ringing_route/$id?isPreview=true")
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
                                                navController = navController
                                            )
                                            todayGraph(
                                                onNavigateToLocationSearch = {
                                                    navController.navigate(locationSearchRoute)
                                                },
                                                onBackClick = {
                                                    navController.popBackStack()
                                                }
                                            )
                                            settingsGraph(
                                                onNavigateToGeneralSettings = { navController.navigateToGeneralSettings() },
                                                onNavigateToQA = { navController.navigateToQA() },
                                                onNavigateToPermissions = { navController.navigateToPermissions() },
                                                onNavigateToAlarmSettings = { navController.navigateToAlarmSettings() },
                                                onNavigateToLicenses = { navController.navigateToLicenses() },
                                                onNavigateToAttributions = { navController.navigateToAttributions() },
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
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }
}

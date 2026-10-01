package vn.io.litever.remind.app

import androidx.activity.ComponentActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import vn.io.litever.remind.app.test.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.AdConfig
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.CurrentWeather
import vn.io.litever.remind.core.model.DailySummary
import java.time.DayOfWeek
import vn.io.litever.remind.core.model.MemoryGameBoard
import vn.io.litever.remind.core.model.MemoryTilesMissionConfig
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.model.Reminder
import vn.io.litever.remind.core.model.ReminderTriggerType
import vn.io.litever.remind.core.model.TimeTrigger
import vn.io.litever.remind.core.model.TodayBriefing
import vn.io.litever.remind.core.model.TodayMetadata
import vn.io.litever.remind.core.model.TypingMissionConfig
import vn.io.litever.remind.core.model.WeatherResponse
import vn.io.litever.remind.features.alarms.ui.AlarmListScreen
import vn.io.litever.remind.features.alarms.ui.AlarmMessageScreen
import vn.io.litever.remind.features.alarms.ui.AlarmRingingScreen
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.features.mission.ui.MissionRingingScreen
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingUiState
import vn.io.litever.remind.features.reminders.ui.ReminderListScreen
import vn.io.litever.remind.features.reminders.viewmodel.ReminderListUiState
import java.time.LocalTime

/**
 * Automated screenshot capture using Fastlane Screengrab for Play Store listings.
 * 
 * Strict architectural isolation:
 * - Completely bypasses Room DB / DAOs / ViewModels (100% in-memory stateless Composables).
 * - Multi-language mock data driven by androidTest res strings (English & Vietnamese).
 * - Rendered in Light Mode per store listing requirements.
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class ScreenshotsTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    companion object {
        @ClassRule
        @JvmField
        val localeTestRule = LocaleTestRule()

        @BeforeClass
        @JvmStatic
        fun setUpScreenshotStrategy() {
            Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        }
    }

    private fun getTestString(resId: Int): String {
        return InstrumentationRegistry.getInstrumentation().context.getString(resId)
    }

    private fun captureScreenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
        val strategy = Screengrab.getDefaultScreenshotStrategy()
        val callback = ExternalStorageScreenshotCallback(context)
        Screengrab.screenshot(name, strategy, callback)
    }

    @Test
    fun test01AlarmListScreen() {
        val mockAlarms = listOf(
            Alarm(
                id = 1L,
                time = LocalTime.of(6, 30),
                label = getTestString(R.string.mock_alarm_1_label),
                isEnabled = true,
                repeatDays = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY
                ),
                vibrationEnabled = true,
                volume = 90
            ),
            Alarm(
                id = 2L,
                time = LocalTime.of(7, 15),
                label = getTestString(R.string.mock_alarm_2_label),
                isEnabled = true,
                repeatDays = listOf(
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                ),
                vibrationEnabled = true,
                volume = 80
            ),
            Alarm(
                id = 3L,
                time = LocalTime.of(12, 30),
                label = getTestString(R.string.mock_alarm_3_label),
                isEnabled = true,
                repeatDays = emptyList(),
                vibrationEnabled = false,
                volume = 70
            ),
            Alarm(
                id = 4L,
                time = LocalTime.of(22, 30),
                label = getTestString(R.string.mock_alarm_4_label),
                isEnabled = false,
                repeatDays = listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.SUNDAY
                ),
                vibrationEnabled = true,
                volume = 60
            )
        )

        composeTestRule.setContent {
            ReMindTheme(darkTheme = false) {
                val snackbarHostState = remember { SnackbarHostState() }
                AlarmListScreen(
                    alarms = mockAlarms,
                    is24HourFormat = false,
                    nextAlarmState = NextAlarmUiState.Remaining(
                        days = 0,
                        hours = 7,
                        minutes = 15,
                        alarm = mockAlarms[0]
                    ),
                    hasCriticalPermissions = true,
                    snackbarHostState = snackbarHostState,
                    onToggleAlarm = {},
                    onDeleteAlarm = {},
                    onDuplicateAlarm = {},
                    onSkipOnce = {},
                    onCancelSkip = {},
                    onDeleteDisabledAlarms = {},
                    onAddAlarmClick = {},
                    onAlarmClick = {},
                    onPreviewClick = {},
                    onNavigateToPermissions = {},
                    onRewardGranted = {},
                    isAdFreeActive = true
                )
            }
        }

        composeTestRule.waitForIdle()
        Thread.sleep(600)
        captureScreenshot("1_alarm_list")
    }

    @Test
    fun test02ReminderListScreen() {
        val mockReminders = listOf(
            Reminder(
                id = 101L,
                title = getTestString(R.string.mock_reminder_1_title),
                notes = getTestString(R.string.mock_reminder_1_notes),
                isEnabled = true,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(time = listOf(LocalTime.of(9, 30)))
            ),
            Reminder(
                id = 102L,
                title = getTestString(R.string.mock_reminder_2_title),
                notes = getTestString(R.string.mock_reminder_2_notes),
                isEnabled = true,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(time = listOf(LocalTime.of(17, 30)))
            ),
            Reminder(
                id = 103L,
                title = getTestString(R.string.mock_reminder_3_title),
                notes = getTestString(R.string.mock_reminder_3_notes),
                isEnabled = true,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(time = listOf(LocalTime.of(14, 0)))
            ),
            Reminder(
                id = 104L,
                title = getTestString(R.string.mock_reminder_4_title),
                notes = getTestString(R.string.mock_reminder_4_notes),
                isEnabled = false,
                triggerType = ReminderTriggerType.FIXED_TIME,
                timeTrigger = TimeTrigger(time = listOf(LocalTime.of(18, 0)))
            )
        )

        composeTestRule.setContent {
            ReMindTheme(darkTheme = false) {
                val snackbarHostState = remember { SnackbarHostState() }
                ReminderListScreen(
                    uiState = ReminderListUiState(
                        reminders = mockReminders,
                        is24HourFormat = false,
                        hasPermissionWarning = false
                    ),
                    snackbarHostState = snackbarHostState,
                    onToggleReminder = {},
                    onReminderClick = {},
                    onEditReminderClick = {},
                    onAddReminderClick = {},
                    onNavigateToPermissions = {},
                    onDismissPermissionWarning = {},
                    onDeleteReminder = {},
                    onDuplicateReminder = {},
                    onDeleteDisabledReminders = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        Thread.sleep(600)
        captureScreenshot("2_reminder_tab")
    }

    @Test
    fun test03AlarmRingingScreen() {
        val mockAlarm = Alarm(
            id = 1L,
            time = LocalTime.of(6, 30),
            label = getTestString(R.string.mock_alarm_1_label),
            message = getTestString(R.string.mock_ringing_message),
            isEnabled = true,
            snoozeEnabled = true,
            autoSilenceMinutes = 3,
            volume = 100
        )

        composeTestRule.setContent {
            ReMindTheme(darkTheme = false) {
                AlarmRingingScreen(
                    alarm = mockAlarm,
                    is24HourFormat = false,
                    autoSilenceCountdown = 180,
                    onDismiss = {},
                    onSnooze = {},
                    onStartMission = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        Thread.sleep(800)
        captureScreenshot("3_ringing")
    }

    @Test
    fun test04MissionTypingScreen() {
        val phraseContent = getTestString(R.string.mock_mission_typing_phrase)
        val mockPhrase = Phrase(
            id = 1L,
            content = phraseContent,
            categoryId = "motivation"
        )
        val mockAlarm = Alarm(
            id = 1L,
            time = LocalTime.of(6, 30),
            label = getTestString(R.string.mock_alarm_1_label)
        )

        composeTestRule.setContent {
            ReMindTheme(darkTheme = false) {
                MissionRingingScreen(
                    uiState = MissionRingingUiState(
                        isLoading = false,
                        alarm = mockAlarm,
                        missions = listOf(
                            Mission(
                                id = 1L,
                                alarmId = 1L,
                                type = MissionType.TYPING,
                                order = 0,
                                repeatCount = 1,
                                config = TypingMissionConfig()
                            )
                        ),
                        currentMissionIndex = 0,
                        currentRepetition = 1,
                        missionData = listOf(mockPhrase),
                        currentTargetData = mockPhrase,
                        timeoutCountdown = 28
                    ),
                    userInput = phraseContent.take(15),
                    onUserInputChange = {},
                    onFinish = {},
                    onAbandon = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        Thread.sleep(600)
        captureScreenshot("4_mission_typing")
    }

    @Test
    fun test05MissionMemoryScreen() {
        val memoryBoard = MemoryGameBoard(
            gridSize = 3,
            targetTiles = 3,
            targetIndices = listOf(1, 4, 7)
        )
        val mockAlarm = Alarm(
            id = 1L,
            time = LocalTime.of(6, 30),
            label = getTestString(R.string.mock_alarm_1_label)
        )

        composeTestRule.setContent {
            ReMindTheme(darkTheme = false) {
                MissionRingingScreen(
                    uiState = MissionRingingUiState(
                        isLoading = false,
                        alarm = mockAlarm,
                        missions = listOf(
                            Mission(
                                id = 2L,
                                alarmId = 1L,
                                type = MissionType.MEMORY_FIND_COLOR_TILES,
                                order = 0,
                                repeatCount = 3,
                                config = MemoryTilesMissionConfig(gridSize = 3, targetTiles = 3)
                            )
                        ),
                        currentMissionIndex = 0,
                        currentRepetition = 1,
                        missionData = listOf(memoryBoard),
                        currentTargetData = memoryBoard,
                        timeoutCountdown = 24
                    ),
                    userInput = "",
                    onUserInputChange = {},
                    onFinish = {},
                    onAbandon = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        Thread.sleep(600)
        captureScreenshot("5_mission_memory")
    }

    @Test
    fun test06AlarmMessageScreen() {
        val mockAlarm = Alarm(
            id = 1L,
            time = LocalTime.of(6, 30),
            label = getTestString(R.string.mock_alarm_1_label),
            message = getTestString(R.string.mock_ringing_message)
        )

        val mockWeather = WeatherResponse(
            locationName = getTestString(R.string.mock_weather_city),
            current = CurrentWeather(
                lastUpdated = "2026-09-29 06:30",
                tempC = 25.5,
                feelsLikeC = 26.0,
                isDay = 1,
                conditionText = getTestString(R.string.mock_weather_condition),
                conditionIcon = "//cdn.weatherapi.com/weather/64x64/day/116.png",
                conditionCode = 1003,
                aqiIndex = 2,
                precipMm = 0.0
            ),
            dailySummary = DailySummary(
                maxTemp = 30.0,
                minTemp = 24.0,
                chanceOfRain = 10
            ),
            hourlyForecast = emptyList(),
            aiAnalysis = vn.io.litever.remind.core.model.AiAnalysis(hint = "A pleasant morning for outdoor exercise")
        )

        val mockBriefing = TodayBriefing(
            messages = listOf(getTestString(R.string.mock_briefing_greeting)),
            adConfig = AdConfig(enableAds = false),
            metadata = TodayMetadata(date = "2026-09-29", isHoliday = false)
        )

        composeTestRule.setContent {
            ReMindTheme(darkTheme = false) {
                AlarmMessageScreen(
                    alarm = mockAlarm,
                    is24HourFormat = false,
                    weather = mockWeather,
                    todayBriefing = mockBriefing,
                    isAdFreeActive = true,
                    onFinish = {}
                )
            }
        }

        composeTestRule.waitForIdle()
        Thread.sleep(600)
        captureScreenshot("6_alarm_message")
    }
}

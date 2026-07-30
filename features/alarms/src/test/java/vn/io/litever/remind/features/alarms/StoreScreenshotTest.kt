package vn.io.litever.remind.features.alarms

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import vn.io.litever.remind.core.model.*
import vn.io.litever.remind.features.alarms.ui.AlarmListScreen
import vn.io.litever.remind.features.alarms.ui.AlarmMessageScreen
import vn.io.litever.remind.features.alarms.ui.AlarmRingingScreen
import vn.io.litever.remind.features.alarms.ui.state.NextAlarmUiState
import vn.io.litever.remind.core.testing.DeviceType
import vn.io.litever.remind.core.testing.StoreScreenshotHelper
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import android.app.Activity
import androidx.compose.ui.Modifier
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.v2.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import java.time.LocalDate
import java.time.LocalTime

abstract class BaseStoreScreenshotTest(private val deviceType: DeviceType) {
    @get:Rule
    val composeTestRule = createComposeRule()

    protected open val locale = "vi"
    
    protected open val mockAlarms = listOf(
        Alarm(
            id = 1,
            time = LocalTime.of(6, 30),
            label = "Thức dậy",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            message = "Chào ngày mới năng động nhé!"
        ),
        Alarm(
            id = 2,
            time = LocalTime.of(12, 0),
            label = "Ăn trưa",
            isEnabled = false,
            repeatDays = emptyList(),
            date = LocalDate.now().plusDays(1)
        ),
        Alarm(
            id = 3,
            time = LocalTime.of(18, 0),
            label = "Tập thể dục",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )
    )

    protected open val mockNextAlarmState = NextAlarmUiState.Remaining(days = 0, hours = 7, minutes = 15)

    protected val fakeAdManager = object : AdManager {
        override val adState: StateFlow<AdState> = MutableStateFlow(AdState.Idle)
        override fun initialize() {}
        override fun loadAd(placement: AdPlacement) {}
        override fun showAd(activity: Activity, placement: AdPlacement, onAdDismissed: () -> Unit) { onAdDismissed() }
        override fun isAdLoaded(placement: AdPlacement): Boolean = false
        @Composable override fun NativeAdView(placement: AdPlacement, modifier: Modifier) {}
    }

    protected fun captureScreen(imageName: String, content: @Composable () -> Unit) {
        composeTestRule.setContent {
            CompositionLocalProvider(LocalAdManager provides fakeAdManager) {
                ReMindTheme() {
                    content()
                }
            }
        }
        val path = StoreScreenshotHelper.getScreenshotPath(locale, deviceType, imageName)
        composeTestRule.onRoot().captureRoboImage(path)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h914dp-port-xxhdpi", sdk = [34])
class MobileScreenshotTest : BaseStoreScreenshotTest(DeviceType.PHONE) {

    @Test
    fun captureAlarmList() {
        captureScreen("1_alarm_list") {
            AlarmListScreen(
                alarms = mockAlarms,
                is24HourFormat = true,
                nextAlarmState = mockNextAlarmState,
                hasCriticalPermissions = true,
                snackbarHostState = SnackbarHostState(),
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

    @Test
    fun captureRinging() {
        captureScreen("3_ringing") {
            AlarmRingingScreen(
                alarm = mockAlarms.first(),
                onDismiss = {},
                onSnooze = {},
                onStartMission = {}
            )
        }
    }

    @Test
    fun captureAlarmMessage() {
        val mockWeather = WeatherResponse(
            locationName = "Hanoi",
            current = CurrentWeather(
                lastUpdated = "2026-04-26 07:45",
                tempC = 25.5,
                feelsLikeC = 27.0,
                isDay = 1,
                conditionText = "Trời nhiều mây",
                conditionIcon = "https://cdn.weatherapi.com/weather/64x64/day/119.png",
                conditionCode = 1006,
                aqiIndex = 2,
                precipMm = 0.0
            ),
            dailySummary = DailySummary(maxTemp = 30.0, minTemp = 24.0, chanceOfRain = 10),
            hourlyForecast = emptyList(),
            aiAnalysis = AiAnalysis(hint = "Thời tiết ổn định, thích hợp cho các hoạt động ngoài trời."),
            aiStatus = "completed"
        )

        val mockReminder = ReminderResponse(
            messages = listOf(
                "Hãy bắt đầu ngày mới bằng một nụ cười rạng rỡ!",
                "Đừng quên uống đủ nước trong ngày nhé."
            ),
            adConfig = AdConfig(enableAds = false, nativeId = "mock-native-ad-id"),
            metadata = ReminderMetadata(date = "04-26", isHoliday = false),
            aiStatus = "completed"
        )

        captureScreen("5_alarm_message") {
            AlarmMessageScreen(
                alarm = mockAlarms.first(),
                is24HourFormat = true,
                weather = mockWeather,
                reminder = mockReminder,
                onFinish = {}
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w600dp-h960dp-port-xhdpi", sdk = [34])
class Tablet7ScreenshotTest : BaseStoreScreenshotTest(DeviceType.TABLET_7_INCH) {
    override val mockAlarms = listOf(
        Alarm(
            id = 1,
            time = LocalTime.of(6, 30),
            label = "Chào buổi sáng (Tablet)",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            message = "Bắt đầu ngày làm việc trên màn hình lớn!"
        )
    )

    @Test
    fun captureAlarmList() {
        captureScreen("1_alarm_list") {
            AlarmListScreen(
                alarms = mockAlarms,
                is24HourFormat = true,
                nextAlarmState = mockNextAlarmState,
                hasCriticalPermissions = true,
                snackbarHostState = SnackbarHostState(),
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

    @Test
    fun captureRinging() {
        captureScreen("2_ringing") {
            AlarmRingingScreen(
                alarm = mockAlarms.first(),
                onDismiss = {},
                onSnooze = {},
                onStartMission = {}
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w800dp-h1280dp-port-xhdpi", sdk = [34])
class Tablet10ScreenshotTest : BaseStoreScreenshotTest(DeviceType.TABLET_10_INCH) {
    override val mockAlarms = listOf(
        Alarm(
            id = 1,
            time = LocalTime.of(6, 30),
            label = "Chào buổi sáng (Tablet Pro)",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            message = "Trải nghiệm hoàn hảo trên Tablet 10 inch!"
        )
    )

    @Test
    fun captureRinging() {
        captureScreen("1_ringing") {
            AlarmRingingScreen(
                alarm = mockAlarms.first(),
                onDismiss = {},
                onSnooze = {},
                onStartMission = {}
            )
        }
    }
}

abstract class EnBaseStoreScreenshotTest(deviceType: DeviceType) : BaseStoreScreenshotTest(deviceType) {
    override val locale = "en-US"
    override val mockAlarms = listOf(
        Alarm(
            id = 1,
            time = LocalTime.of(6, 30),
            label = "Wake up",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            message = "Have a great day!"
        ),
        Alarm(
            id = 2,
            time = LocalTime.of(12, 0),
            label = "Lunch",
            isEnabled = false,
            repeatDays = emptyList(),
            date = LocalDate.now().plusDays(1)
        ),
        Alarm(
            id = 3,
            time = LocalTime.of(18, 0),
            label = "Exercise",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        )
    )
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "en-w412dp-h914dp-port-xxhdpi", sdk = [34])
class EnMobileScreenshotTest : EnBaseStoreScreenshotTest(DeviceType.PHONE) {

    @Test
    fun captureAlarmList() {
        captureScreen("1_alarm_list") {
            AlarmListScreen(
                alarms = mockAlarms,
                is24HourFormat = true,
                nextAlarmState = mockNextAlarmState,
                hasCriticalPermissions = true,
                snackbarHostState = SnackbarHostState(),
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

    @Test
    fun captureRinging() {
        captureScreen("3_ringing") {
            AlarmRingingScreen(
                alarm = mockAlarms.first(),
                onDismiss = {},
                onSnooze = {},
                onStartMission = {}
            )
        }
    }

    @Test
    fun captureAlarmMessage() {
        val mockWeather = WeatherResponse(
            locationName = "London",
            current = CurrentWeather(
                lastUpdated = "2026-04-26 07:45",
                tempC = 15.5,
                feelsLikeC = 15.0,
                isDay = 1,
                conditionText = "Cloudy",
                conditionIcon = "https://cdn.weatherapi.com/weather/64x64/day/119.png",
                conditionCode = 1006,
                aqiIndex = 2,
                precipMm = 0.0
            ),
            dailySummary = DailySummary(maxTemp = 20.0, minTemp = 14.0, chanceOfRain = 10),
            hourlyForecast = emptyList(),
            aiAnalysis = AiAnalysis(hint = "Stable weather, suitable for outdoor activities."),
            aiStatus = "completed"
        )

        val mockReminder = ReminderResponse(
            messages = listOf(
                "Let's start the day with a big smile!",
                "Don't forget to drink enough water today."
            ),
            adConfig = AdConfig(enableAds = false, nativeId = "mock-native-ad-id"),
            metadata = ReminderMetadata(date = "04-26", isHoliday = false),
            aiStatus = "completed"
        )

        captureScreen("5_alarm_message") {
            AlarmMessageScreen(
                alarm = mockAlarms.first(),
                is24HourFormat = true,
                weather = mockWeather,
                reminder = mockReminder,
                onFinish = {}
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "en-w600dp-h960dp-port-tvdpi", sdk = [34])
class EnTablet7ScreenshotTest : EnBaseStoreScreenshotTest(DeviceType.TABLET_7_INCH) {
    override val mockAlarms = listOf(
        Alarm(
            id = 1,
            time = LocalTime.of(6, 30),
            label = "Good morning (Tablet)",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            message = "Start your day on a big screen!"
        )
    )

    @Test
    fun captureAlarmList() {
        captureScreen("1_alarm_list") {
            AlarmListScreen(
                alarms = mockAlarms,
                is24HourFormat = true,
                nextAlarmState = mockNextAlarmState,
                hasCriticalPermissions = true,
                snackbarHostState = SnackbarHostState(),
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

    @Test
    fun captureRinging() {
        captureScreen("2_ringing") {
            AlarmRingingScreen(
                alarm = mockAlarms.first(),
                onDismiss = {},
                onSnooze = {},
                onStartMission = {}
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "en-w800dp-h1280dp-port-xhdpi", sdk = [34])
class EnTablet10ScreenshotTest : EnBaseStoreScreenshotTest(DeviceType.TABLET_10_INCH) {
    override val mockAlarms = listOf(
        Alarm(
            id = 1,
            time = LocalTime.of(6, 30),
            label = "Good morning (Tablet Pro)",
            isEnabled = true,
            repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            message = "Perfect experience on 10-inch screen!"
        )
    )

    @Test
    fun captureRinging() {
        captureScreen("1_ringing") {
            AlarmRingingScreen(
                alarm = mockAlarms.first(),
                onDismiss = {},
                onSnooze = {},
                onStartMission = {}
            )
        }
    }
}

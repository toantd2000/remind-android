package vn.io.litever.remind.features.remind

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.junit4.createComposeRule
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import android.app.Activity
import androidx.compose.ui.Modifier
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.AdConfig
import vn.io.litever.remind.core.model.AiAnalysis
import vn.io.litever.remind.core.model.CurrentWeather
import vn.io.litever.remind.core.model.DailySummary
import vn.io.litever.remind.core.model.ReminderMetadata
import vn.io.litever.remind.core.model.ReminderResponse
import vn.io.litever.remind.core.model.WeatherResponse
import vn.io.litever.remind.features.remind.ui.RemindScreen
import vn.io.litever.remind.core.testing.DeviceType
import vn.io.litever.remind.core.testing.StoreScreenshotHelper

abstract class BaseRemindScreenshotTest(
    private val deviceType: DeviceType
) {
    @get:Rule
    val composeTestRule = createComposeRule()

    protected open val locale = "vi"

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
                ReMindTheme {
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
@Config(qualifiers = "vi-w412dp-h914dp-port-xxhdpi", sdk = [34])
class RemindScreenshotTest : BaseRemindScreenshotTest(DeviceType.PHONE) {

    @Test
    fun captureReminderTab() {
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

        captureScreen("2_reminder_tab") {
            RemindScreen(
                weather = mockWeather,
                reminder = mockReminder,
                isRefreshing = false,
                isProcessing = false,
                onRefresh = {}
            )
        }
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "en-w412dp-h914dp-port-xxhdpi", sdk = [34])
class EnRemindScreenshotTest : BaseRemindScreenshotTest(DeviceType.PHONE) {
    override val locale = "en-US"

    @Test
    fun captureReminderTab() {
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

        captureScreen("2_reminder_tab") {
            RemindScreen(
                weather = mockWeather,
                reminder = mockReminder,
                isRefreshing = false,
                isProcessing = false,
                onRefresh = {}
            )
        }
    }
}

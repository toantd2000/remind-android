package vn.io.litever.remind.features.mission

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.core.model.DayOfWeek
import vn.io.litever.remind.core.model.Mission
import vn.io.litever.remind.core.model.MissionType
import vn.io.litever.remind.core.model.Phrase
import vn.io.litever.remind.core.testing.DeviceType
import vn.io.litever.remind.core.testing.StoreScreenshotHelper
import vn.io.litever.remind.features.mission.ui.MissionRingingScreen
import vn.io.litever.remind.features.mission.viewmodel.MissionRingingUiState
import java.time.LocalTime

abstract class BaseMissionScreenshotTest(private val deviceType: DeviceType) {
    @get:Rule
    val composeTestRule = createComposeRule()

    protected open val locale = "vi"
    
    protected open val mockAlarm = Alarm(
        id = 1,
        time = LocalTime.of(6, 30),
        label = "Thức dậy",
        isEnabled = true,
        repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
        message = "Chào ngày mới năng động nhé!"
    )

    protected fun captureScreen(imageName: String, content: @Composable () -> Unit) {
        composeTestRule.setContent {
            ReMindTheme() {
                content()
            }
        }
        val path = StoreScreenshotHelper.getScreenshotPath(locale, deviceType, imageName)
        composeTestRule.onRoot().captureRoboImage(path)
    }
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w412dp-h914dp-port-xxhdpi", sdk = [34])
class MobileMissionScreenshotTest : BaseMissionScreenshotTest(DeviceType.PHONE) {
    @Test
    fun captureTypingMission() {
        val phrase = Phrase(content = "Hôm nay là một ngày tuyệt vời để bắt đầu những thử thách mới.", categoryId = "custom")
        val uiState = MissionRingingUiState(
            isLoading = false,
            alarm = mockAlarm,
            missions = listOf(Mission(alarmId = 1, type = MissionType.TYPING, order = 0, repeatCount = 1)),
            currentMissionIndex = 0,
            currentTargetData = phrase,
            timeoutCountdown = 30
        )
        captureScreen("4_mission") {
            MissionRingingScreen(
                uiState = uiState,
                userInput = "Hôm nay là một ngày tuyệt vời để bắ",
                onUserInputChange = {},
                onFinish = {},
                onAbandon = {}
            )
        }
    }
}

abstract class EnBaseMissionScreenshotTest(deviceType: DeviceType) : BaseMissionScreenshotTest(deviceType) {
    override val locale = "en-US"
    
    override val mockAlarm = Alarm(
        id = 1,
        time = LocalTime.of(6, 30),
        label = "Wake up",
        isEnabled = true,
        repeatDays = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
        message = "Have a great day!"
    )
}

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "en-w412dp-h914dp-port-xxhdpi", sdk = [34])
class EnMobileMissionScreenshotTest : EnBaseMissionScreenshotTest(DeviceType.PHONE) {
    @Test
    fun captureTypingMission() {
        val phrase = Phrase(content = "Today is a great day to start new challenges.", categoryId = "custom")
        val uiState = MissionRingingUiState(
            isLoading = false,
            alarm = mockAlarm,
            missions = listOf(Mission(alarmId = 1, type = MissionType.TYPING, order = 0, repeatCount = 1)),
            currentMissionIndex = 0,
            currentTargetData = phrase,
            timeoutCountdown = 30
        )
        captureScreen("4_mission") {
            MissionRingingScreen(
                uiState = uiState,
                userInput = "Today is a great day to st",
                onUserInputChange = {},
                onFinish = {},
                onAbandon = {}
            )
        }
    }
}

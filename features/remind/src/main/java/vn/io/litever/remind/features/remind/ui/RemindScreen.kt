package vn.io.litever.remind.features.remind.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.remind.core.designsystem.components.ReMindLoadingIconButton
import vn.io.litever.remind.core.designsystem.components.WeatherInfoView
import vn.io.litever.remind.core.designsystem.components.ReminderInfoView
import vn.io.litever.remind.core.designsystem.components.NativeAdView
import vn.io.litever.remind.features.remind.R
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.designsystem.theme.LiteverTheme.typography
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.*

@Composable
fun RemindRoute(
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {},
    viewModel: RemindViewModel = hiltViewModel()
) {
    val weather by viewModel.weather.collectAsState()
    val reminder by viewModel.reminder.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkAndRefreshIfProcessing()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    RemindScreen(
        weather = weather,
        reminder = reminder,
        isRefreshing = isRefreshing,
        isProcessing = isProcessing,
        onRefresh = viewModel::refresh,
        onLocationClick = onLocationClick,
        modifier = modifier
    )
}

@Composable
fun RemindScreen(
    weather: WeatherResponse?,
    reminder: ReminderResponse?,
    isRefreshing: Boolean,
    isProcessing: Boolean,
    onRefresh: () -> Unit,
    onLocationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when (hour) {
        in 5..11 -> stringResource(R.string.greeting_morning)
        in 12..17 -> stringResource(R.string.greeting_afternoon)
        in 18..21 -> stringResource(R.string.greeting_evening)
        else -> stringResource(R.string.greeting_night)
    }

    ReMindScaffold(
        topBar = {
            ReMindTopAppBar(
                title = greeting,
                actions = {
                    ReMindLoadingIconButton(
                        onClick = onRefresh,
                        icon = Icons.Rounded.Refresh,
                        loading = isRefreshing,
                        contentDescription = stringResource(R.string.refresh)
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weather != null) {
                WeatherInfoView(
                    weather = weather,
                    onLocationClick = onLocationClick
                )
            } else if (isRefreshing) {
                CircularProgressIndicator()
            }

            Spacer(modifier = Modifier.height(16.dp))

            ReminderInfoView(reminder = reminder)

            if (reminder?.adConfig?.enableAds == true) {
                Spacer(modifier = Modifier.height(16.dp))
                NativeAdView(adId = reminder?.adConfig?.nativeId)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RemindScreenPreview() {
    val mockWeather = WeatherResponse(
        locationName = "Hanoi",
        current = CurrentWeather(
            lastUpdated = "2026-04-26 07:45",
            tempC = 25.5,
            feelsLikeC = 27.0,
            isDay = 1,
            conditionText = "Cloudy",
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
        adConfig = AdConfig(enableAds = true, nativeId = "mock-native-ad-id"),
        metadata = ReminderMetadata(date = "04-26", isHoliday = false),
        aiStatus = "completed"
    )

    ReMindTheme {
        RemindScreen(
            weather = mockWeather,
            reminder = mockReminder,
            isRefreshing = false,
            isProcessing = false,
            onRefresh = {}
        )
    }
}

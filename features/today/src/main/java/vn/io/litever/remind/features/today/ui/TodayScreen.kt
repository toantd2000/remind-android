package vn.io.litever.remind.features.today.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import vn.io.litever.remind.core.designsystem.components.ReMindTopAppBar
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.remind.core.ads.api.AdManager
import vn.io.litever.remind.core.ads.api.AdPlacement
import vn.io.litever.remind.core.ads.api.AdState
import vn.io.litever.remind.core.ads.api.LocalAdManager
import vn.io.litever.remind.core.designsystem.components.ReMindLoadingIconButton
import vn.io.litever.remind.core.designsystem.components.TodayQuoteView
import vn.io.litever.remind.core.designsystem.components.WeatherInfoView
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.AdConfig
import vn.io.litever.remind.core.model.AiAnalysis
import vn.io.litever.remind.core.model.CurrentWeather
import vn.io.litever.remind.core.model.DailySummary
import vn.io.litever.remind.core.model.TodayBriefing
import vn.io.litever.remind.core.model.TodayMetadata
import vn.io.litever.remind.core.model.WeatherResponse
import vn.io.litever.remind.features.today.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayRoute(
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {},
    viewModel: TodayViewModel = hiltViewModel()
) {
    val weather by viewModel.weather.collectAsState()
    val todayBriefing by viewModel.todayBriefing.collectAsState()
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

    TodayScreen(
        weather = weather,
        todayBriefing = todayBriefing,
        isRefreshing = isRefreshing,
        isProcessing = isProcessing,
        onRefresh = viewModel::refresh,
        onLocationClick = onLocationClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    weather: WeatherResponse?,
    todayBriefing: TodayBriefing?,
    isRefreshing: Boolean,
    isProcessing: Boolean,
    onRefresh: () -> Unit,
    onLocationClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = stringResource(getGreetingStringRes(hour))

    Scaffold(
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
                .padding(LiteverTheme.spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weather != null) {
                WeatherInfoView(
                    weather = weather,
                    onLocationClick = onLocationClick
                )
            } else if (isRefreshing) {
                CircularProgressIndicator(
                    color = LiteverTheme.colors.primary
                )
            }

            Spacer(modifier = Modifier.height(LiteverTheme.spacing.medium))

            TodayQuoteView(todayBriefing = todayBriefing)

            LocalAdManager.current.NativeAdView(
                placement = AdPlacement.REMIND_NATIVE,
                modifier = Modifier.padding(top = LiteverTheme.spacing.medium)
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

fun getGreetingStringRes(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.greeting_morning
    in 12..17 -> R.string.greeting_afternoon
    in 18..21 -> R.string.greeting_evening
    else -> R.string.greeting_night
}

@Preview(showBackground = true)
@Composable
fun TodayScreenPreview() {
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

    val mockTodayBriefing = TodayBriefing(
        messages = listOf(
            "Hãy bắt đầu ngày mới bằng một nụ cười rạng rỡ!",
            "Đừng quên uống đủ nước trong ngày nhé."
        ),
        adConfig = AdConfig(enableAds = true, nativeId = "mock-native-ad-id"),
        metadata = TodayMetadata(date = "04-26", isHoliday = false),
        aiStatus = "completed"
    )

    ReMindTheme {
        CompositionLocalProvider(LocalAdManager provides PreviewAdManager) {
            TodayScreen(
                weather = mockWeather,
                todayBriefing = mockTodayBriefing,
                isRefreshing = false,
                isProcessing = false,
                onRefresh = {}
            )
        }
    }
}

private object PreviewAdManager : AdManager {
    override val adState: StateFlow<AdState> = MutableStateFlow(AdState.Idle)
    override fun initialize() {}
    override fun loadAd(placement: AdPlacement) {}
    override fun showAd(activity: Activity, placement: AdPlacement, onAdDismissed: () -> Unit) {}
    override fun isAdLoaded(placement: AdPlacement): Boolean = false

    @Composable
    override fun NativeAdView(placement: AdPlacement, modifier: Modifier) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(250.dp)
                .background(LiteverTheme.colors.surfaceVariant, LiteverTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Native Ad Preview ($placement)",
                style = LiteverTheme.typography.labelLarge,
                color = LiteverTheme.colors.onSurfaceVariant
            )
        }
    }
}

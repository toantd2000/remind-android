package vn.io.litever.remind.features.alarms.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Star
import vn.io.litever.remind.core.designsystem.components.ReMindScaffold
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.Alarm
import vn.io.litever.remind.features.alarms.viewmodel.AlarmRingingViewModel
import androidx.compose.ui.tooling.preview.Preview
import vn.io.litever.remind.core.designsystem.components.ReMindButton
import vn.io.litever.remind.core.designsystem.components.WeatherInfoView
import vn.io.litever.remind.core.model.*
import java.time.LocalTime
import java.util.Locale

@Composable
fun AlarmMessageRoute(
    alarmId: Long,
    onFinish: () -> Unit,
    viewModel: AlarmRingingViewModel = hiltViewModel()
) {
    val alarm by viewModel.alarm.collectAsState()
    val weather by viewModel.weather.collectAsState()

    AlarmMessageScreen(
        alarm = alarm,
        weather = weather,
        onFinish = {
            viewModel.onFinishMessage()
            onFinish()
        }
    )
}

@Composable
fun AlarmMessageScreen(
    alarm: Alarm?,
    weather: vn.io.litever.remind.core.model.WeatherResponse?,
    onFinish: () -> Unit
) {
    BackHandler { }
    ReMindScaffold { padding ->
        val isMissed = alarm?.isMissed == true
        val statusColor = if (isMissed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        val icon = if (isMissed) Icons.Rounded.AlarmOff else Icons.Rounded.NotificationsActive
        val statusTitle = if (isMissed) {
            stringResource(vn.io.litever.remind.features.alarms.R.string.missed_alarm_title)
        } else {
            stringResource(vn.io.litever.remind.features.alarms.R.string.alarm_summary_title)
        }

        // Subtle gradient background based on status
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Header Section
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = statusColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = statusTitle,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = statusColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    statusColor.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // Time with small AM/PM
                    val displayTime = alarm?.time ?: LocalTime.now()
                    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm")
                    val amPmFormatter = java.time.format.DateTimeFormatter.ofPattern("a")
                    
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = displayTime.format(timeFormatter),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-1).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = displayTime.format(amPmFormatter).uppercase(Locale.getDefault()),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (!alarm?.label.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = alarm?.label ?: "",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (!alarm?.message.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = alarm?.message ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            WeatherInfoView(
                weather = weather,
                isCompact = true
            )

            Spacer(modifier = Modifier.weight(1f))

            ReMindButton(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(vn.io.litever.remind.features.alarms.R.string.alarm_message_dismiss),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmMessageScreenPreview() {
    val mockWeather = WeatherResponse(
        locationName = "Hanoi",
        current = CurrentWeather(
            lastUpdated = "2026-04-26 07:45",
            tempC = 23.7,
            feelsLikeC = 25.9,
            isDay = 1,
            conditionText = "Partly Cloudy",
            conditionIcon = "https://cdn.weatherapi.com/weather/64x64/day/116.png",
            conditionCode = 1003,
            aqiIndex = 3,
            precipMm = 0.0
        ),
        dailySummary = DailySummary(maxTemp = 29.6, minTemp = 22.2, chanceOfRain = 88),
        hourlyForecast = emptyList(),
        aiAnalysis = AiAnalysis(hint = "Trời mát, mang theo ô vì có thể có mưa rào.")
    )

    ReMindTheme {
        AlarmMessageScreen(
            alarm = Alarm(
                id = 1,
                time = LocalTime.of(7, 30),
                label = "Morning Yoga",
                message = "Time to stretch and start your day!"
            ),
            weather = mockWeather,
            onFinish = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AlarmMessageMissedScreenPreview() {
    val mockWeather = WeatherResponse(
        locationName = "Hanoi",
        current = CurrentWeather(
            lastUpdated = "2026-04-26 07:45",
            tempC = 33.5, // Hot
            feelsLikeC = 36.0,
            isDay = 1,
            conditionText = "Sunny",
            conditionIcon = "https://cdn.weatherapi.com/weather/64x64/day/113.png",
            conditionCode = 1000,
            aqiIndex = 5,
            precipMm = 0.0
        ),
        dailySummary = DailySummary(maxTemp = 35.0, minTemp = 26.0, chanceOfRain = 0),
        hourlyForecast = emptyList(),
        aiAnalysis = AiAnalysis(hint = "Trời rất nóng, hãy uống đủ nước và mặc đồ thoáng mát.")
    )

    ReMindTheme(darkTheme = true) {
        AlarmMessageScreen(
            alarm = Alarm(
                id = 2,
                time = LocalTime.of(8, 0),
                label = "Important Meeting",
                message = "You missed this important alarm.",
                isMissed = true
            ),
            weather = mockWeather,
            onFinish = {}
        )
    }
}











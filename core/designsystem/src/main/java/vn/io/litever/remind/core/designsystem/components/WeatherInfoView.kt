package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import vn.io.litever.designsystem.theme.TailwindColors
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.*

@Composable
fun WeatherInfoView(
    weather: WeatherResponse?,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    onLocationClick: () -> Unit = {}
) {
    if (weather == null) return

    if (isCompact) {
        CompactWeatherView(weather, modifier, onLocationClick)
    } else {
        FullWeatherView(weather, modifier, onLocationClick)
    }
}

@Composable
private fun getWeatherColors(temp: Double, isDay: Int): List<Color> {
    val isNight = isDay == 0
    return when {
        isNight -> listOf(TailwindColors.Indigo.c800, TailwindColors.Violet.c900) // Night
        temp < 15 -> listOf(TailwindColors.Sky.c300, TailwindColors.Cyan.c200)    // Cold
        temp < 25 -> listOf(TailwindColors.Emerald.c300, TailwindColors.Green.c100) // Mild
        temp < 32 -> listOf(TailwindColors.Amber.c200, TailwindColors.Amber.c50)   // Warm
        else -> listOf(TailwindColors.Orange.c300, TailwindColors.Rose.c100)       // Hot
    }
}

@Composable
private fun FullWeatherView(
    weather: WeatherResponse,
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {}
) {
    val weatherColors = getWeatherColors(weather.current.tempC, weather.current.isDay)
    val isNight = weather.current.isDay == 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            weatherColors[0].copy(alpha = if (isNight) 0.2f else 0.3f),
                            weatherColors[1].copy(alpha = if (isNight) 0.3f else 0.3f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1: Location
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onLocationClick() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = weather.locationName ?: "Unknown",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Change Location",
                            modifier = Modifier.size(20.dp).padding(start = 4.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Row 2: Main Info (Temp Left, Icon Right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Temperature & Min/Max
                    Column {
                        Text(
                            text = "${weather.current.tempC.toInt()}°",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 72.sp,
                                letterSpacing = (-2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cao: ${weather.dailySummary.maxTemp.toInt()}° • Thấp: ${weather.dailySummary.minTemp.toInt()}°",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Cảm giác như ${weather.current.feelsLikeC.toInt()}°",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Right: Condition Icon & Text
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        AsyncImage(
                            model = weather.current.conditionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = weather.current.conditionText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 3: AI Hint (Free floating)
                if (weather.aiAnalysis.hint.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "✨",
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = weather.aiAnalysis.hint,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 20.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Attribution
                Text(
                    text = "Powered by WeatherAPI",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
private fun CompactWeatherView(
    weather: WeatherResponse,
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {}
) {
    val weatherColors = getWeatherColors(weather.current.tempC, weather.current.isDay)
    val isNight = weather.current.isDay == 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            weatherColors[0].copy(alpha = if (isNight) 0.15f else 0.2f),
                            weatherColors[1].copy(alpha = if (isNight) 0.2f else 0.2f)
                        )
                    )
                )
                .clickable { onLocationClick() }
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Icon inside a subtle circle
                Box(
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .padding(6.dp)
                ) {
                    AsyncImage(
                        model = weather.current.conditionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // Top row: Temp & Cond + Location
                    Text(
                        text = "${weather.current.tempC.toInt()}°C • ${weather.current.conditionText}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Bottom row: AI Hint
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "✨",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = weather.aiAnalysis.hint,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherInfoViewPreview() {
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
        dailySummary = DailySummary(
            maxTemp = 29.6,
            minTemp = 22.2,
            chanceOfRain = 88
        ),
        hourlyForecast = listOf(
            HourlyForecast("07:00", 23.7, 0, "Partly Cloudy"),
            HourlyForecast("08:00", 25.3, 0, "Partly Cloudy"),
            HourlyForecast("09:00", 26.9, 0, "Cloudy"),
            HourlyForecast("10:00", 28.3, 72, "Patchy rain nearby")
        ),
        aiAnalysis = AiAnalysis(
            hint = "Mặc đồ thoáng mát, mang theo ô vì trời sắp mưa lúc 10h."
        )
    )

    ReMindTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Full View", style = MaterialTheme.typography.titleMedium)
            WeatherInfoView(weather = mockWeather)

            Text("Compact View", style = MaterialTheme.typography.titleMedium)
            WeatherInfoView(weather = mockWeather, isCompact = true)
        }
    }
}
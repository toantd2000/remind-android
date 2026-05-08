package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.magnifier
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import vn.io.litever.designsystem.components.LiteverTextButton
import vn.io.litever.designsystem.theme.TailwindColors
import vn.io.litever.remind.core.designsystem.R
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
    return when {
        temp < 15 -> listOf(TailwindColors.Sky.c300, TailwindColors.Cyan.c200)    // Cold
        temp < 25 -> listOf(TailwindColors.Emerald.c300, TailwindColors.Green.c100) // Mild
        temp < 32 -> listOf(TailwindColors.Amber.c200, TailwindColors.Amber.c50)   // Warm
        else -> listOf(TailwindColors.Orange.c300, TailwindColors.Rose.c100)       // Hot
    }
}

@Composable
private fun getPanaIllustration(hint: String): Int {
    val lowerHint = hint.lowercase()
    return when {
        lowerHint.contains("mưa") -> R.drawable.raining_pana
        lowerHint.contains("ô") -> R.drawable.umbrella_pana
        lowerHint.contains("nắng") -> R.drawable.sunny_day_pana
        lowerHint.contains("gió") -> R.drawable.windy_day_pana
        lowerHint.contains("lạnh") -> R.drawable.fall_is_coming_pana
        else -> R.drawable.windy_day_pana
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
        shape = RoundedCornerShape(8.dp),
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
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Location & Main Temperature Row
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Location, Temperature, Min/Max
                    Column(modifier = Modifier.weight(1f)) {
                        LiteverTextButton(
                            onClick = onLocationClick,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = weather.locationName ?: stringResource(R.string.weather_unknown_location),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = stringResource(R.string.weather_change_location),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(start = 4.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(
                            text = "${weather.current.tempC.toInt()}°",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 72.sp,
                                letterSpacing = (-2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        Text(
                            text = stringResource(
                                R.string.weather_temp_range,
                                weather.dailySummary.maxTemp.toInt(),
                                weather.dailySummary.minTemp.toInt()
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Text(
                            text = stringResource(R.string.weather_feels_like, weather.current.feelsLikeC.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp),

                        )
                    }

                    // Right: Illustration (Pana)
                    val panaRes = getPanaIllustration(weather.aiAnalysis.hint)
                    Image(
                        painter = painterResource(id = panaRes),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(start = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                // Condition Row: Icon & Text (below temperature)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    AsyncImage(
                        model = weather.current.conditionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = weather.current.conditionText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                

                // AI Hint Section
                if (weather.aiAnalysis.hint.isNotBlank()) {
                    Text(
                        text = "✨ "  + weather.aiAnalysis.hint,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                }
                
                // Attribution
                Text(
                    text = stringResource(R.string.weather_powered_by),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp, bottom = 8.dp),
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
        shape = RoundedCornerShape(8.dp),
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
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Pana Illustration - Sử dụng Box + matchParentSize để không làm bung chiều cao Row
                val panaRes = getPanaIllustration(weather.aiAnalysis.hint)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f)
                ) {
                    Image(
                        painter = painterResource(id = panaRes),
                        contentDescription = null,
                        modifier = Modifier
                            .matchParentSize()
                            .padding(all = 2.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 8.dp, bottom = 8.dp, end = 8.dp)
                ) {
                    // Row 1: Temperature & Condition Summary
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.weather_temp_celsius, weather.current.tempC.toInt()) + " • ",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                        )

                        AsyncImage(
                            model = weather.current.conditionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = weather.current.conditionText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Row 2: AI Hint
                    Text(
                        text = "✨ " + weather.aiAnalysis.hint,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
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
package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import vn.io.litever.designsystem.components.button.LvButton
import vn.io.litever.designsystem.components.button.LvButtonType
import vn.io.litever.designsystem.components.core.LvSemantic
import vn.io.litever.designsystem.theme.LiteverTheme
import vn.io.litever.designsystem.theme.palettes.blueDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.blueLightColorScheme
import vn.io.litever.designsystem.theme.palettes.greenDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.greenLightColorScheme
import vn.io.litever.designsystem.theme.palettes.indigoDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.indigoLightColorScheme
import vn.io.litever.designsystem.theme.palettes.orangeDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.orangeLightColorScheme
import vn.io.litever.designsystem.theme.palettes.redDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.redLightColorScheme
import vn.io.litever.designsystem.theme.palettes.yellowDarkColorScheme
import vn.io.litever.designsystem.theme.palettes.yellowLightColorScheme
import vn.io.litever.remind.core.designsystem.R
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.AiAnalysis
import vn.io.litever.remind.core.model.CurrentWeather
import vn.io.litever.remind.core.model.DailySummary
import vn.io.litever.remind.core.model.HourlyForecast
import vn.io.litever.remind.core.model.WeatherResponse

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
private fun getWeatherColors(temp: Double): List<Color> {
    val isDark = isSystemInDarkTheme()
    return when {
        temp < 15 -> if (isDark) {
            listOf(indigoDarkColorScheme.primaryContainer, blueDarkColorScheme.primaryContainer)
        } else {
            listOf(indigoLightColorScheme.primaryContainer, blueLightColorScheme.primaryContainer)
        }
        temp < 25 -> if (isDark) {
            listOf(greenDarkColorScheme.primaryContainer, greenDarkColorScheme.secondaryContainer)
        } else {
            listOf(greenLightColorScheme.primaryContainer, greenLightColorScheme.secondaryContainer)
        }
        temp < 32 -> if (isDark) {
            listOf(yellowDarkColorScheme.primaryContainer, orangeDarkColorScheme.primaryContainer)
        } else {
            listOf(yellowLightColorScheme.primaryContainer, orangeLightColorScheme.primaryContainer)
        }
        else -> if (isDark) {
            listOf(orangeDarkColorScheme.primaryContainer, redDarkColorScheme.primaryContainer)
        } else {
            listOf(orangeLightColorScheme.primaryContainer, redLightColorScheme.primaryContainer)
        }
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
    val weatherColors = getWeatherColors(weather.current.tempC)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = LiteverTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = weatherColors
                    )
                )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)) {

                // Location & Main Temperature Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LiteverTheme.spacing.small)
                        .padding(top = LiteverTheme.spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Location, Temperature, Min/Max
                    Column(modifier = Modifier.weight(1f)) {
                        LvButton(
                            onClick = onLocationClick,
                            type = LvButtonType.Text,
                            semantic = LvSemantic.Neutral
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
                                        .size(LiteverTheme.spacing.mediumLarge)
                                        .padding(start = LiteverTheme.spacing.extraSmall),
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
                            modifier = Modifier.padding(start = LiteverTheme.spacing.small)
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
                            modifier = Modifier.padding(start = LiteverTheme.spacing.small)
                        )
                        Text(
                            text = stringResource(R.string.weather_feels_like, weather.current.feelsLikeC.toInt()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(
                                start = LiteverTheme.spacing.small,
                                top = LiteverTheme.spacing.tiny
                            )
                        )
                    }

                    // Right: Illustration (Pana)
                    val panaRes = getPanaIllustration(weather.aiAnalysis.hint)
                    Image(
                        painter = painterResource(id = panaRes),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1.0f)
                            .padding(start = LiteverTheme.spacing.small),
                        contentScale = ContentScale.Fit
                    )
                }

                // Condition Row: Icon & Text (below temperature)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = LiteverTheme.spacing.medium)
                ) {
                    AsyncImage(
                        model = weather.current.conditionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(LiteverTheme.spacing.small))
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
                        text = "✨ " + weather.aiAnalysis.hint,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = LiteverTheme.spacing.medium)
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
                        .padding(
                            end = LiteverTheme.spacing.medium,
                            bottom = LiteverTheme.spacing.small
                        ),
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
    val weatherColors = getWeatherColors(weather.current.tempC)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = LiteverTheme.shapes.large,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = weatherColors
                    )
                )
                .clickable { onLocationClick() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Pana Illustration
                val panaRes = getPanaIllustration(weather.aiAnalysis.hint)
                Image(
                    painter = painterResource(id = panaRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(start = LiteverTheme.spacing.small),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.width(LiteverTheme.spacing.smallMedium))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(
                            top = LiteverTheme.spacing.small,
                            bottom = LiteverTheme.spacing.small,
                            end = LiteverTheme.spacing.smallMedium
                        )
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
                            modifier = Modifier.size(LiteverTheme.spacing.medium)
                        )
                        Spacer(modifier = Modifier.width(LiteverTheme.spacing.extraSmall))
                        Text(
                            text = weather.current.conditionText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Row 2: AI Hint
                    if (weather.aiAnalysis.hint.isNotBlank()) {
                        Spacer(modifier = Modifier.height(LiteverTheme.spacing.extraSmall))
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
                .padding(LiteverTheme.spacing.medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.medium)
        ) {
            Text("Full View", style = MaterialTheme.typography.titleMedium)
            WeatherInfoView(weather = mockWeather)

            Text("Compact View", style = MaterialTheme.typography.titleMedium)
            WeatherInfoView(weather = mockWeather, isCompact = true)

            Text("Compact View (Long Hint - No Limit)", style = MaterialTheme.typography.titleMedium)
            WeatherInfoView(
                weather = mockWeather.copy(
                    aiAnalysis = AiAnalysis(
                        hint = "Hôm nay trời có thể có mưa rào rải rác vào buổi chiều, quý khách nên mang theo ô và áo mưa khi đi ra ngoài để tránh bị ướt."
                    )
                ),
                isCompact = true
            )
        }
    }
}

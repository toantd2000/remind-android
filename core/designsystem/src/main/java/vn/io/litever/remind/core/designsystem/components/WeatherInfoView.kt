package vn.io.litever.remind.core.designsystem.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val alpha = if (isDark) 0.3f else 0.7f
    val scheme = when {
        temp < 15 -> if (isDark) blueDarkColorScheme else blueLightColorScheme
        temp < 22 -> if (isDark) greenDarkColorScheme else greenLightColorScheme
        temp < 28 -> if (isDark) yellowDarkColorScheme else yellowLightColorScheme
        temp < 33 -> if (isDark) orangeDarkColorScheme else orangeLightColorScheme
        else -> if (isDark) redDarkColorScheme else redLightColorScheme
    }
    return listOf(scheme.primaryContainer, scheme.surfaceContainer).map { it.copy(alpha = alpha) }
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(LiteverTheme.shapes.extraLarge)
            .background(brush = Brush.linearGradient(weatherColors)),
        verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
    ) {
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
                            tint = MaterialTheme.colorScheme.onSurface
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
                    color = LiteverTheme.colors.onSurface,
                    modifier = Modifier.padding(start = LiteverTheme.spacing.small)
                )
                Text(
                    text = stringResource(R.string.weather_feels_like, weather.current.feelsLikeC.toInt()),
                    style = MaterialTheme.typography.labelSmall,
                    color = LiteverTheme.colors.onSurface,
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
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // AI Hint Section
        if (weather.aiAnalysis.hint.isNotBlank()) {
            Text(
                text = "✨ " + weather.aiAnalysis.hint,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
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
            color = LiteverTheme.colors.onSurface.copy(alpha = 0.6f),
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

@Composable
private fun CompactWeatherView(
    weather: WeatherResponse,
    modifier: Modifier = Modifier,
    onLocationClick: () -> Unit = {}
) {
    val weatherColors = getWeatherColors(weather.current.tempC)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(LiteverTheme.shapes.large)
            .background(brush = Brush.linearGradient(weatherColors))
            .clickable { onLocationClick() },
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
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Row 2: AI Hint
            if (weather.aiAnalysis.hint.isNotBlank()) {
                Spacer(modifier = Modifier.height(LiteverTheme.spacing.extraSmall))
                Text(
                    text = "✨ " + weather.aiAnalysis.hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = LiteverTheme.colors.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun WeatherScaleItemPreview(
    label: String,
    temp: Double
) {
    val mockWeather = WeatherResponse(
        locationName = "Hà Nội",
        current = CurrentWeather(
            lastUpdated = "2026-04-26 07:45",
            tempC = temp,
            feelsLikeC = temp + 1.2,
            isDay = 1,
            conditionText = "Trời nhiều mây",
            conditionIcon = "https://cdn.weatherapi.com/weather/64x64/day/116.png",
            conditionCode = 1003,
            aqiIndex = 3,
            precipMm = 0.0
        ),
        dailySummary = DailySummary(
            maxTemp = temp + 4.0,
            minTemp = temp - 3.0,
            chanceOfRain = 88
        ),
        hourlyForecast = emptyList(),
        aiAnalysis = AiAnalysis(
            hint = "Thời tiết mát mẻ, nên mang theo ô phòng mưa rào."
        )
    )

    ReMindTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LiteverTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(LiteverTheme.spacing.small)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                WeatherInfoView(weather = mockWeather)
                WeatherInfoView(weather = mockWeather, isCompact = true)
            }
        }
    }
}

@Preview(name = "1. Lam (< 15°C) Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "1. Lam (< 15°C) Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewScaleBlue() {
    WeatherScaleItemPreview(label = "🔵 Lam (< 15°C - Rất lạnh)", temp = 10.0)
}

@Preview(name = "2. Lục (15-22°C) Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "2. Lục (15-22°C) Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewScaleGreen() {
    WeatherScaleItemPreview(label = "🟢 Lục (15°C - 22°C - Mát mẻ)", temp = 18.0)
}

@Preview(name = "3. Vàng (22-28°C) Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "3. Vàng (22-28°C) Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewScaleYellow() {
    WeatherScaleItemPreview(label = "🟡 Vàng (22°C - 28°C - Ôn hòa)", temp = 25.0)
}

@Preview(name = "4. Cam (28-33°C) Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "4. Cam (28-33°C) Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewScaleOrange() {
    WeatherScaleItemPreview(label = "🟠 Cam (28°C - 33°C - Ấm áp)", temp = 30.0)
}

@Preview(name = "5. Đỏ (>= 33°C) Light", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "5. Đỏ (>= 33°C) Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewScaleRed() {
    WeatherScaleItemPreview(label = "🔴 Đỏ (>= 33°C - Nắng nóng)", temp = 35.0)
}

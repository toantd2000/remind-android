package vn.io.litever.remind.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import vn.io.litever.remind.core.designsystem.theme.ReMindTheme
import vn.io.litever.remind.core.model.*
import vn.io.litever.remind.core.model.WeatherResponse

@Composable
fun WeatherInfoView(
    weather: WeatherResponse?,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    if (weather == null) return

    if (isCompact) {
        CompactWeatherView(weather, modifier)
    } else {
        FullWeatherView(weather, modifier)
    }
}

@Composable
private fun getWeatherColors(temp: Double, isDay: Int): List<Color> {
    val isNight = isDay == 0
    return when {
        isNight -> listOf(Color(0xFF1A237E), Color(0xFF311B92)) // Night: Indigo/Deep Purple
        temp < 15 -> listOf(Color(0xFF81D4FA), Color(0xFFB2EBF2)) // Cold: Soft Blue
        temp < 25 -> listOf(Color(0xFFA5D6A7), Color(0xFFE8F5E9)) // Mild: Soft Green
        temp < 32 -> listOf(Color(0xFFFFE082), Color(0xFFFFF8E1)) // Warm: Soft Amber
        else -> listOf(Color(0xFFFFAB91), Color(0xFFFBE9E7))      // Hot: Soft Coral
    }
}

@Composable
private fun FullWeatherView(weather: WeatherResponse, modifier: Modifier = Modifier) {
    val weatherColors = getWeatherColors(weather.current.tempC, weather.current.isDay)
    val isNight = weather.current.isDay == 0
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            weatherColors[0].copy(alpha = if (isNight) 0.8f else 0.4f),
                            weatherColors[1].copy(alpha = if (isNight) 0.5f else 0.2f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Row 1: Location
                Text(
                    text = weather.locationName ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Row 2: 3 Columns
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Col 1: Temperature (Main Focus) - wrap content
                    Text(
                        text = "${weather.current.tempC.toInt()}°",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 64.sp
                        ),
                        modifier = Modifier.wrapContentWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Col 2: Icon + Condition Text - wrap content
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        AsyncImage(
                            model = weather.current.conditionIcon,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = weather.current.conditionText,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }

                    // Col 3: Details Cluster - takes remaining space
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f) 
                    ) {
                        // Compact Row for 3 params
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            WeatherDetailItem(Icons.Rounded.WaterDrop, "${weather.dailySummary.chanceOfRain}%")
                            WeatherDetailItem(Icons.Rounded.Air, weather.current.aqiIndex.toString())
                            WeatherDetailItem(Icons.Rounded.Thermostat, "${weather.current.precipMm}mm")
                        }
                        
                        Divider(
                            modifier = Modifier.width(40.dp).padding(vertical = 2.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        
                        Text(
                            text = "Cao: ${weather.dailySummary.maxTemp.toInt()}° Thấp: ${weather.dailySummary.minTemp.toInt()}°",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cảm giác như ${weather.current.feelsLikeC.toInt()}°",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Row 3: AI Hint
                if (weather.aiAnalysis.hint.isNotBlank()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("✨", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = weather.aiAnalysis.hint,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactWeatherView(weather: WeatherResponse, modifier: Modifier = Modifier) {
    val weatherColors = getWeatherColors(weather.current.tempC, weather.current.isDay)
    val isNight = weather.current.isDay == 0
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = weatherColors[0].copy(alpha = if (isNight) 0.25f else 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            weatherColors[0].copy(alpha = if (isNight) 0.3f else 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = weather.current.conditionIcon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${weather.current.tempC.toInt()}°C • ${weather.current.conditionText}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = weather.aiAnalysis.hint,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun WeatherDetailItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
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

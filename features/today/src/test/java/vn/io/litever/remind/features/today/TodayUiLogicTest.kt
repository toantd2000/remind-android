package vn.io.litever.remind.features.today

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import vn.io.litever.remind.core.model.AdConfig
import vn.io.litever.remind.core.model.AiAnalysis
import vn.io.litever.remind.core.model.CurrentWeather
import vn.io.litever.remind.core.model.DailySummary
import vn.io.litever.remind.core.model.TodayBriefing
import vn.io.litever.remind.core.model.TodayMetadata
import vn.io.litever.remind.core.model.WeatherResponse
import vn.io.litever.remind.features.today.ui.getGreetingStringRes

/**
 * Unit tests for UI business logic in :features:today module.
 */
class TodayUiLogicTest {

    // ------------------------------------------------------------------------
    // 1. Greeting Interval Tests
    // ------------------------------------------------------------------------

    @Test
    fun getGreetingStringRes_morningInterval_returnsMorningString() {
        val morningHours = listOf(5, 6, 8, 10, 11)
        for (hour in morningHours) {
            assertEquals(
                "Hour $hour should map to greeting_morning",
                R.string.greeting_morning,
                getGreetingStringRes(hour)
            )
        }
    }

    @Test
    fun getGreetingStringRes_afternoonInterval_returnsAfternoonString() {
        val afternoonHours = listOf(12, 13, 15, 16, 17)
        for (hour in afternoonHours) {
            assertEquals(
                "Hour $hour should map to greeting_afternoon",
                R.string.greeting_afternoon,
                getGreetingStringRes(hour)
            )
        }
    }

    @Test
    fun getGreetingStringRes_eveningInterval_returnsEveningString() {
        val eveningHours = listOf(18, 19, 20, 21)
        for (hour in eveningHours) {
            assertEquals(
                "Hour $hour should map to greeting_evening",
                R.string.greeting_evening,
                getGreetingStringRes(hour)
            )
        }
    }

    @Test
    fun getGreetingStringRes_nightInterval_returnsNightString() {
        val nightHours = listOf(22, 23, 0, 1, 3, 4)
        for (hour in nightHours) {
            assertEquals(
                "Hour $hour should map to greeting_night",
                R.string.greeting_night,
                getGreetingStringRes(hour)
            )
        }
    }

    // ------------------------------------------------------------------------
    // 2. AI Processing State Combination Logic Tests
    // ------------------------------------------------------------------------

    private fun isProcessing(weather: WeatherResponse?, todayBriefing: TodayBriefing?): Boolean {
        return weather?.aiStatus == "processing" || todayBriefing?.aiStatus == "processing"
    }

    private fun createWeatherResponse(aiStatus: String): WeatherResponse {
        return WeatherResponse(
            locationName = "Hanoi",
            current = CurrentWeather(
                lastUpdated = "2026-04-26 07:45",
                tempC = 25.5,
                feelsLikeC = 27.0,
                isDay = 1,
                conditionText = "Sunny",
                conditionIcon = "https://cdn.weatherapi.com/icon.png",
                conditionCode = 1000,
                aqiIndex = 1,
                precipMm = 0.0
            ),
            dailySummary = DailySummary(maxTemp = 30.0, minTemp = 24.0, chanceOfRain = 5),
            hourlyForecast = emptyList(),
            aiAnalysis = AiAnalysis(hint = "Fine day"),
            aiStatus = aiStatus
        )
    }

    private fun createTodayBriefing(aiStatus: String): TodayBriefing {
        return TodayBriefing(
            messages = listOf("Keep smiling!"),
            adConfig = AdConfig(enableAds = false),
            metadata = TodayMetadata(date = "09-11", isHoliday = false),
            aiStatus = aiStatus
        )
    }

    @Test
    fun isProcessing_whenBothNull_returnsFalse() {
        assertFalse(isProcessing(weather = null, todayBriefing = null))
    }

    @Test
    fun isProcessing_whenWeatherIsProcessing_returnsTrue() {
        val weather = createWeatherResponse("processing")
        val briefing = createTodayBriefing("completed")
        assertTrue(isProcessing(weather, briefing))
    }

    @Test
    fun isProcessing_whenBriefingIsProcessing_returnsTrue() {
        val weather = createWeatherResponse("completed")
        val briefing = createTodayBriefing("processing")
        assertTrue(isProcessing(weather, briefing))
    }

    @Test
    fun isProcessing_whenBothAreProcessing_returnsTrue() {
        val weather = createWeatherResponse("processing")
        val briefing = createTodayBriefing("processing")
        assertTrue(isProcessing(weather, briefing))
    }

    @Test
    fun isProcessing_whenBothAreCompleted_returnsFalse() {
        val weather = createWeatherResponse("completed")
        val briefing = createTodayBriefing("completed")
        assertFalse(isProcessing(weather, briefing))
    }

    @Test
    fun isProcessing_whenWeatherFailedAndBriefingCompleted_returnsFalse() {
        val weather = createWeatherResponse("failed")
        val briefing = createTodayBriefing("completed")
        assertFalse(isProcessing(weather, briefing))
    }

    // ------------------------------------------------------------------------
    // 3. Location Search Query Filter Logic Tests
    // ------------------------------------------------------------------------

    private fun isQueryValidForSearch(query: String): Boolean {
        return query.length > 2
    }

    @Test
    fun isQueryValidForSearch_lengthLessThanOrEqualToTwo_returnsFalse() {
        val shortQueries = listOf("", "A", "Ha", "  ")
        for (query in shortQueries) {
            if (query.length <= 2) {
                assertFalse(
                    "Query '$query' with length ${query.length} should not trigger search",
                    isQueryValidForSearch(query)
                )
            }
        }
    }

    @Test
    fun isQueryValidForSearch_lengthGreaterThanTwo_returnsTrue() {
        val validQueries = listOf("Han", "Hanoi", "Da Nang", "Ho Chi Minh")
        for (query in validQueries) {
            assertTrue(
                "Query '$query' with length ${query.length} should trigger search",
                isQueryValidForSearch(query)
            )
        }
    }
}

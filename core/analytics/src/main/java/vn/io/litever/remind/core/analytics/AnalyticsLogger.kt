package vn.io.litever.remind.core.analytics

interface AnalyticsLogger {
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())
}

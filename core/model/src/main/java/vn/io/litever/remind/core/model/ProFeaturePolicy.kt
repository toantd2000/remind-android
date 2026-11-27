package vn.io.litever.remind.core.model

/**
 * Single source of truth for Pro feature entitlements across UI, ViewModels,
 * Alarm/Reminder ringing services, Wake-Up Check, and Mission execution.
 *
 * Design principle — **Non-destructive degradation**:
 * Persisted Room/DataStore settings are preserved if a user's Pro subscription expires,
 * while this policy deterministically resolves the *effective* runtime & UI behavior
 * based on the current [isPro] entitlement. If the user re-subscribes, all previously
 * configured Pro settings seamlessly reactivate.
 */
object ProFeaturePolicy {
    const val FREE_MAX_MISSIONS = 1
    const val PRO_MAX_MISSIONS = 5

    fun maxMissions(isPro: Boolean): Int = if (isPro) PRO_MAX_MISSIONS else FREE_MAX_MISSIONS

    fun canUseMissionType(type: MissionType, isPro: Boolean): Boolean =
        isPro || type != MissionType.RANDOM

    fun isMissionSlotUnlocked(slotIndex: Int, isPro: Boolean): Boolean =
        slotIndex < maxMissions(isPro)

    /**
     * Returns whether a Pro-gated boolean feature is effectively active.
     * Always returns `false` when [isPro] is `false`, even if the stored setting was previously `true`.
     */
    fun isEffectiveEnabled(savedValue: Boolean, isPro: Boolean): Boolean =
        isPro && savedValue

    /**
     * Returns effective Wake-Up Check delay in minutes (`0` when [isPro] is `false`).
     */
    fun effectiveWakeUpCheckMinutes(savedMinutes: Int, isPro: Boolean): Int =
        if (isPro) savedMinutes.coerceAtLeast(0) else 0

    /**
     * Resolves the effective list of missions to execute when an alarm rings.
     * - Pro: up to [PRO_MAX_MISSIONS] sorted by order.
     * - Free: excludes Pro-only mission types (e.g. [MissionType.RANDOM]) and caps at [FREE_MAX_MISSIONS].
     */
    fun resolveEffectiveMissions(missions: List<Mission>, isPro: Boolean): List<Mission> {
        val sorted = missions.sortedBy { it.order }
        if (isPro) return sorted.take(PRO_MAX_MISSIONS)
        return sorted
            .filter { canUseMissionType(it.type, isPro = false) }
            .take(FREE_MAX_MISSIONS)
    }

    /**
     * Resolves an [Alarm] for runtime execution (ringing audio, TTS, Wake-Up Check, missions)
     * according to current [isPro] entitlement.
     */
    fun resolveEffectiveAlarm(alarm: Alarm, isPro: Boolean): Alarm {
        if (isPro) return alarm
        return alarm.copy(
            extraLoudEnabled = false,
            announceTimeTts = false,
            announceLabelTts = false,
            wakeUpCheckMinutes = 0,
            missions = resolveEffectiveMissions(alarm.missions, isPro = false)
        )
    }

    /**
     * Resolves a [ReminderAction] for runtime execution according to current [isPro] entitlement.
     */
    fun resolveEffectiveReminderAction(action: ReminderAction, isPro: Boolean): ReminderAction {
        if (isPro) return action
        return action.copy(
            extraLoudEnabled = false,
            announceTitleTts = false
        )
    }

    /**
     * Resolves a [Reminder] for runtime execution according to current [isPro] entitlement.
     */
    fun resolveEffectiveReminder(reminder: Reminder, isPro: Boolean): Reminder {
        if (isPro) return reminder
        return reminder.copy(
            action = resolveEffectiveReminderAction(reminder.action, isPro = false)
        )
    }
}

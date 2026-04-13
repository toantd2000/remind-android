package vn.io.litever.remind.core.reminder.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.reminder.ReminderRingManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ReminderRingManagerEntryPoint {
    fun reminderRingManager(): ReminderRingManager
}

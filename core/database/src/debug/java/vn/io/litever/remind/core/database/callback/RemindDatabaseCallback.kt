package vn.io.litever.remind.core.database.callback

import android.util.Log
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RemindDatabaseCallback : RoomDatabase.Callback() {
    private val TAG = "ReMind_DB_Debug"

    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        seedDebugData(db)
    }

    private fun seedDebugData(db: SupportSQLiteDatabase) {
        try {
            val cursor = db.query("SELECT COUNT(*) FROM alarms")
            cursor.moveToFirst()
            val count = cursor.getInt(0)
            cursor.close()

            if (count > 0) return // Already has data, skip seeding

            Log.i(TAG, "Database seeding: inserting sample alarms for debug build")
            val tomorrow = LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)

            db.beginTransaction()
            try {
                // 1. Morning wake-up (Weekdays, enabled, Math mission)
                db.execSQL(
                    """INSERT INTO alarms (hour, minute, label, isEnabled, repeatDays, date, vibrationEnabled, ringtoneUri, volume, snoozeEnabled, snoozeInterval, snoozeRepeatCount, autoSilenceMinutes, currentSnoozeCount, snoozeNextTriggerTime, message, skippedAt, gradualVolumeDurationSeconds, lastTriggeredTime, useAlarmStream, overrideDndEnabled)
                    VALUES (6, 0, '🌅 Thức dậy', 1, 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY', NULL, 1, NULL, 80, 1, 5, 3, 5, 0, NULL, 'Một ngày mới bắt đầu! Hãy tỉnh dậy và chinh phục thế giới! 💪', NULL, 30, NULL, 1, 0)"""
                )
                val alarm1Id = getLastInsertId(db)
                db.execSQL(
                    """INSERT INTO missions (alarmId, type, missionOrder, repeatCount, configJson)
                    VALUES ($alarm1Id, 'MATH', 0, 3, '{"difficulty":"NORMAL"}')"""
                )

                // 2. Exercise (Mon/Wed/Fri, enabled, Shake mission)
                db.execSQL(
                    """INSERT INTO alarms (hour, minute, label, isEnabled, repeatDays, date, vibrationEnabled, ringtoneUri, volume, snoozeEnabled, snoozeInterval, snoozeRepeatCount, autoSilenceMinutes, currentSnoozeCount, snoozeNextTriggerTime, message, skippedAt, gradualVolumeDurationSeconds, lastTriggeredTime, useAlarmStream, overrideDndEnabled)
                    VALUES (7, 0, '🏋️ Tập thể dục', 1, 'MONDAY,WEDNESDAY,FRIDAY', NULL, 1, NULL, 90, 0, 5, 3, 3, 0, NULL, 'Đã đến giờ vận động! Cơ thể khỏe mạnh, tinh thần minh mẫn! 🏃', NULL, 15, NULL, 1, 0)"""
                )
                val alarm2Id = getLastInsertId(db)
                db.execSQL(
                    """INSERT INTO missions (alarmId, type, missionOrder, repeatCount, configJson)
                    VALUES ($alarm2Id, 'SHAKE', 0, 5, NULL)"""
                )

                // 3. Medicine reminder (Daily, enabled, no mission)
                db.execSQL(
                    """INSERT INTO alarms (hour, minute, label, isEnabled, repeatDays, date, vibrationEnabled, ringtoneUri, volume, snoozeEnabled, snoozeInterval, snoozeRepeatCount, autoSilenceMinutes, currentSnoozeCount, snoozeNextTriggerTime, message, skippedAt, gradualVolumeDurationSeconds, lastTriggeredTime, useAlarmStream, overrideDndEnabled)
                    VALUES (12, 0, '💊 Uống thuốc', 1, 'MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY', NULL, 1, NULL, 70, 1, 10, 2, 5, 0, NULL, 'Nhớ uống thuốc đúng giờ để giữ sức khỏe nhé! 🩺', NULL, 20, NULL, 1, 0)"""
                )

                // 4. Reading time (Weekend, disabled, Typing mission)
                db.execSQL(
                    """INSERT INTO alarms (hour, minute, label, isEnabled, repeatDays, date, vibrationEnabled, ringtoneUri, volume, snoozeEnabled, snoozeInterval, snoozeRepeatCount, autoSilenceMinutes, currentSnoozeCount, snoozeNextTriggerTime, message, skippedAt, gradualVolumeDurationSeconds, lastTriggeredTime, useAlarmStream, overrideDndEnabled)
                    VALUES (21, 30, '📖 Đọc sách', 0, 'SATURDAY,SUNDAY', NULL, 0, NULL, 50, 0, 5, 1, 5, 0, NULL, 'Thư giãn với một cuốn sách hay trước khi ngủ 📚', NULL, 10, NULL, 0, 0)"""
                )
                val alarm4Id = getLastInsertId(db)
                db.execSQL(
                    """INSERT INTO missions (alarmId, type, missionOrder, repeatCount, configJson)
                    VALUES ($alarm4Id, 'TYPING', 0, 1, '{"selectedPhraseIds":[],"mode":"NORMAL"}')"""
                )

                // 5. Team meeting (One-time tomorrow, enabled, no mission)
                db.execSQL(
                    """INSERT INTO alarms (hour, minute, label, isEnabled, repeatDays, date, vibrationEnabled, ringtoneUri, volume, snoozeEnabled, snoozeInterval, snoozeRepeatCount, autoSilenceMinutes, currentSnoozeCount, snoozeNextTriggerTime, message, skippedAt, gradualVolumeDurationSeconds, lastTriggeredTime, useAlarmStream, overrideDndEnabled)
                    VALUES (9, 0, '🎯 Họp team', 1, '', '$tomorrow', 1, NULL, 85, 1, 5, 2, 5, 0, NULL, 'Chuẩn bị nội dung và tham gia họp đúng giờ! 📋', NULL, 20, NULL, 1, 1)"""
                )

                db.setTransactionSuccessful()
                Log.i(TAG, "Database seeding: completed successfully (5 alarms + 3 missions)")
            } finally {
                db.endTransaction()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to seed DB on open", e)
        }
    }

    private fun getLastInsertId(db: SupportSQLiteDatabase): Long {
        val cursor = db.query("SELECT last_insert_rowid()")
        cursor.moveToFirst()
        val id = cursor.getLong(0)
        cursor.close()
        return id
    }
}

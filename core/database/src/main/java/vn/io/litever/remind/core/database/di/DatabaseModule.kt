package vn.io.litever.remind.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.database.RemindDatabase
import vn.io.litever.remind.core.database.dao.AlarmDao
import vn.io.litever.remind.core.database.dao.MissedAlarmDao
import vn.io.litever.remind.core.database.dao.MissionDao
import vn.io.litever.remind.core.database.dao.PhraseDao
import vn.io.litever.remind.core.database.dao.ReminderDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): RemindDatabase {
        return Room.databaseBuilder(
            context,
            RemindDatabase::class.java,
            "remind_database"
        ).addMigrations(
            RemindDatabase.MIGRATION_1_2,
            RemindDatabase.MIGRATION_2_3,
            RemindDatabase.MIGRATION_3_4,
            RemindDatabase.MIGRATION_4_5,
            RemindDatabase.MIGRATION_5_6,
            RemindDatabase.MIGRATION_6_7,
            RemindDatabase.MIGRATION_7_8,
            RemindDatabase.MIGRATION_8_9
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    fun provideAlarmDao(database: RemindDatabase): AlarmDao {
        return database.alarmDao()
    }

    @Provides
    fun provideMissionDao(database: RemindDatabase): MissionDao {
        return database.missionDao()
    }

    @Provides
    fun providePhraseDao(database: RemindDatabase): PhraseDao {
        return database.phraseDao()
    }

    @Provides
    fun provideMissedAlarmDao(database: RemindDatabase): MissedAlarmDao {
        return database.missedAlarmDao()
    }

    @Provides
    fun provideReminderDao(database: RemindDatabase): ReminderDao {
        return database.reminderDao()
    }
}

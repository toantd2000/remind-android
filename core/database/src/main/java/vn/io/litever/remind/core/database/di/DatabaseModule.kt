package vn.io.litever.remind.core.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.database.RemindDatabase
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
        ).fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideReminderDao(database: RemindDatabase): ReminderDao {
        return database.reminderDao()
    }
}

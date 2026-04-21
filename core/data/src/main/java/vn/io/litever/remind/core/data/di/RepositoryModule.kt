package vn.io.litever.remind.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.io.litever.remind.core.data.repository.MissionRepositoryImpl
import vn.io.litever.remind.core.data.repository.ReminderRepositoryImpl
import vn.io.litever.remind.core.domain.repository.MissionRepository
import vn.io.litever.remind.core.domain.repository.ReminderRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        reminderRepositoryImpl: ReminderRepositoryImpl
    ): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindMissionRepository(
        missionRepositoryImpl: MissionRepositoryImpl
    ): MissionRepository
}

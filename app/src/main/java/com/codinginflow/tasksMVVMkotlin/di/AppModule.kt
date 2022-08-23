package com.codinginflow.tasksMVVMkotlin.di

import android.app.Application
import androidx.room.Room
import com.codinginflow.tasksMVVMkotlin.data.TaskDataBase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providerDatabase(
        app:Application,
        callback:TaskDataBase.Callback
    ) = Room.databaseBuilder(app,TaskDataBase::class.java,"task_database")
            .fallbackToDestructiveMigration()
        .addCallback(callback)
            .build()
    @Provides
    fun providerTaskDao(db:TaskDataBase) = db.taskDao()

    @ApplicationScope
    @Provides
    @Singleton
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Retention(AnnotationRetention.RUNTIME)
    @Qualifier
    annotation class ApplicationScope

}
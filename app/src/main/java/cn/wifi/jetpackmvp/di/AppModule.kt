package cn.wifi.jetpackmvp.di

import android.content.Context
import androidx.room.Room
import cn.wifi.jetpackmvp.data.source.DefaultTasksRepository
import cn.wifi.jetpackmvp.data.source.TasksDataSource
import cn.wifi.jetpackmvp.data.source.TasksRepository
import cn.wifi.jetpackmvp.data.source.local.TasksDao
import cn.wifi.jetpackmvp.data.source.local.TasksLocalDataSource
import cn.wifi.jetpackmvp.data.source.local.ToDoDatabase
import cn.wifi.jetpackmvp.data.source.remote.TasksRemoteDataSource
import cn.wifi.jetpackmvp.data.source.remote.TasksService
import cn.wifi.jetpackmvp.util.ApiResponseCallAdapterFactory
import cn.wifi.jetpackmvp.util.LiveDataCallAdapter
import cn.wifi.jetpackmvp.util.LiveDataCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Module to tell Hilt how to provide instances of types that cannot be constructor-injected.
 *
 * As these types are scoped to the application lifecycle using @Singleton, they're installed
 * in Hilt's ApplicationComponent.
 */
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTasksService(): TasksService {
        return Retrofit.Builder()
            .baseUrl("")
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addCallAdapterFactory(ApiResponseCallAdapterFactory())
            .build()
            .create(TasksService::class.java)
    }

    @Singleton
    @Provides
    fun provideTasksDao(db: ToDoDatabase): TasksDao {
        return db.taskDao()
    }

    @Singleton
    @Provides
    fun provideDataBase(@ApplicationContext context: Context): ToDoDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            ToDoDatabase::class.java,
            "Tasks.db"
        ).build()
    }
}
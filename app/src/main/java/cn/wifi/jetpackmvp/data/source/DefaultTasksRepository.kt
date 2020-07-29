/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.wifi.jetpackmvp.data.source

import androidx.lifecycle.LiveData
import cn.wifi.jetpackmvp.data.model.Task
import cn.wifi.jetpackmvp.data.Result
import cn.wifi.jetpackmvp.data.Result.Success
import cn.wifi.jetpackmvp.data.source.local.TasksDao
import cn.wifi.jetpackmvp.data.source.remote.TasksService
import cn.wifi.jetpackmvp.util.wrapEspressoIdlingResource
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Default implementation of [TasksRepository]. Single entry point for managing tasks' data.
 */
class DefaultTasksRepository @Inject constructor(
    private val tasksDao: TasksDao,
    private val tasksService: TasksService
) : TasksRepository {

    override suspend fun getTasks(forceUpdate: Boolean): Result<List<Task>> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {

            if (forceUpdate) {
                try {
                    updateTasksFromRemoteDataSource()
                } catch (ex: Exception) {
                    return Result.Error(ex)
                }
            }
            return tasksLocalDataSource.getTasks()
        }
    }

    override suspend fun refreshTasks() {
        updateTasksFromRemoteDataSource()
    }

    override fun observeTasks(): LiveData<Result<List<Task>>> {
        return tasksLocalDataSource.observeTasks()
    }

    override suspend fun refreshTask(taskId: String) {
        updateTaskFromRemoteDataSource(taskId)
    }

    private suspend fun updateTasksFromRemoteDataSource() {
        val remoteTasks = tasksRemoteDataSource.getTasks()

        if (remoteTasks is Success) {
            // Real apps might want to do a proper sync, deleting, modifying or adding each task.
            tasksLocalDataSource.deleteAllTasks()
            remoteTasks.data.forEach { task ->
                tasksLocalDataSource.saveTask(task)
            }
        } else if (remoteTasks is Result.Error) {
            throw remoteTasks.exception
        }
    }

    override fun observeTask(taskId: String): LiveData<Result<Task>> {
        return tasksLocalDataSource.observeTask(taskId)
    }

    private suspend fun updateTaskFromRemoteDataSource(taskId: String) {
        val remoteTask = tasksRemoteDataSource.getTask(taskId)

        if (remoteTask is Success) {
            tasksLocalDataSource.saveTask(remoteTask.data)
        }
    }

    /**
     * Relies on [getTasks] to fetch data and picks the task with the same ID.
     */
    override suspend fun getTask(taskId: String, forceUpdate: Boolean): Result<Task> {
        // Set app as busy while this function executes.
        wrapEspressoIdlingResource {
            if (forceUpdate) {
                updateTaskFromRemoteDataSource(taskId)
            }
            return tasksLocalDataSource.getTask(taskId)
        }
    }

    override suspend fun saveTask(task: Task) {
        coroutineScope {
            launch { tasksRemoteDataSource.saveTask(task) }
            launch { tasksLocalDataSource.saveTask(task) }
        }
    }

    override suspend fun completeTask(task: Task) {
        coroutineScope {
            launch { tasksRemoteDataSource.completeTask(task) }
            launch { tasksLocalDataSource.completeTask(task) }
        }
    }

    override suspend fun completeTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Success)?.let { it ->
                completeTask(it.data)
            }
        }
    }

    override suspend fun activateTask(task: Task) = withContext<Unit>(ioDispatcher) {
        coroutineScope {
            launch { tasksRemoteDataSource.activateTask(task) }
            launch { tasksLocalDataSource.activateTask(task) }
        }
    }

    override suspend fun activateTask(taskId: String) {
        withContext(ioDispatcher) {
            (getTaskWithId(taskId) as? Success)?.let { it ->
                activateTask(it.data)
            }
        }
    }

    override suspend fun clearCompletedTasks() {
        coroutineScope {
            launch { tasksRemoteDataSource.clearCompletedTasks() }
            launch { tasksLocalDataSource.clearCompletedTasks() }
        }
    }

    override suspend fun deleteAllTasks() {
        withContext(ioDispatcher) {
            coroutineScope {
                launch { tasksRemoteDataSource.deleteAllTasks() }
                launch { tasksLocalDataSource.deleteAllTasks() }
            }
        }
    }

    override suspend fun deleteTask(taskId: String) {
        coroutineScope {
            launch { tasksRemoteDataSource.deleteTask(taskId) }
            launch { tasksLocalDataSource.deleteTask(taskId) }
        }
    }

    private suspend fun getTaskWithId(id: String): Result<Task> {
        return tasksLocalDataSource.getTask(id)
    }
}
package cn.wifi.jetpackmvp.data

import androidx.annotation.MainThread
import androidx.lifecycle.*
import kotlinx.coroutines.*

fun <ResultType, RequestType> networkBoundResource(
    saveCallResult: suspend (RequestType) -> Unit,
    shouldFetch: (ResultType) -> Boolean = { true },
    loadFromDb: () -> LiveData<ResultType>,
    fetch: suspend () -> ApiResponse<RequestType>,
    processResponse: (suspend (ApiSuccessResponse<RequestType>) -> RequestType) = { it.body },
    onFetchFailed: ((ApiErrorResponse<RequestType>) -> Unit)? = null
): LiveData<Result<ResultType>> {
    return NetworkBoundResource(
        saveCallResult = saveCallResult,
        shouldFetch = shouldFetch,
        loadFromDb = loadFromDb,
        fetch = fetch,
        processResponse = processResponse,
        onFetchFailed = onFetchFailed
    ).asLiveData().distinctUntilChanged()
}

private class NetworkBoundResource<ResultType, RequestType>
@MainThread constructor(
    private val saveCallResult: suspend (RequestType) -> Unit,
    private val shouldFetch: (ResultType) -> Boolean = { true },
    private val loadFromDb: () -> LiveData<ResultType>,
    private val fetch: suspend () -> ApiResponse<RequestType>,
    private val processResponse: suspend (ApiSuccessResponse<RequestType>) -> RequestType,
    private val onFetchFailed: ((ApiErrorResponse<RequestType>) -> Unit)?
) {
    @ExperimentalCoroutinesApi
    private val result = liveData<Result<ResultType>> {
        if (initialValue?.status != Result.Success()) {
            emit(Result.Loading)
        }
        val dbSource = loadFromDb()
        val initialValue = dbSource.await()
        val willFetch = initialValue == null || shouldFetch(initialValue)
        if (!willFetch) {
            // if we won't fetch, just emit existing db values as success
            emitSource(dbSource.map {
                Result.Success(it)
            })
        } else {
            doFetch(dbSource, this)
        }
    }

    private suspend fun doFetch(
        dbSource: LiveData<ResultType>,
        liveDataScope: LiveDataScope<Result<ResultType>>
    ) {
        // emit existing values as loading while we fetch
        val initialSource = liveDataScope.emitSource(dbSource.map {
            Result.Loading
        })
        val response = fetchCatching()
        when (response) {
            is ApiSuccessResponse, is ApiEmptyResponse -> {
                if (response is ApiSuccessResponse) {
                    val processed = processResponse(response)
                    initialSource.dispose()
                    // before saving it, disconnect it so that new values comes w/ success
                    saveCallResult(processed)
                }
                liveDataScope.emitSource(loadFromDb().map {
                    Result.Success(it)
                })
            }
            is ApiErrorResponse -> {
                onFetchFailed?.invoke(response)
                liveDataScope.emitSource(dbSource.map {
                    Result.Error(response.errorMessage)
                })
            }
        }
    }

    fun asLiveData() = result as LiveData<Result<ResultType>>

    private suspend fun fetchCatching(): ApiResponse<RequestType> {
        return try {
            fetch()
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Throwable) {
            ApiResponse.create(ex)
        }
    }

    private suspend fun <T> LiveData<T>.await() = withContext(Dispatchers.Main) {
        val receivedValue = CompletableDeferred<T?>()
        val observer = Observer<T> {
            if (receivedValue.isActive) {
                receivedValue.complete(it)
            }
        }
        try {
            observeForever(observer)
            return@withContext receivedValue.await()
        } finally {
            removeObserver(observer)
        }
    }
}
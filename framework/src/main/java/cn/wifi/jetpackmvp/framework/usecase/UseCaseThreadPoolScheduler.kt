package cn.wifi.jetpackmvp.framework.usecase

import android.os.Handler
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Executes asynchronous tasks using a {@link ThreadPoolExecutor}
 * <p>
 * See also {@link Executors} for a list of factory methods to create common
 * {@link java.util.concurrent.ExecutorServices} for different scenarios.
 */
open class UseCaseThreadPoolScheduler : UseCaseScheduler {

    private val mHandler = Handler()

    companion object {

        val POOL_SIZE = 2

        val MAX_POOL_SIZE = 4

        val TIMEOUT = 30
    }

    lateinit var mThreadPoolExecutor: ThreadPoolExecutor

    init {
        mThreadPoolExecutor = ThreadPoolExecutor(POOL_SIZE, MAX_POOL_SIZE, TIMEOUT.toLong(),
                TimeUnit.SECONDS, ArrayBlockingQueue<Runnable>(POOL_SIZE))
    }

    override fun execute(runnable: Runnable) {
        mThreadPoolExecutor.execute(runnable)
    }

    override fun <V : UseCase.ResponseValue> notifyResponse(
        response: V,
        useCaseCallback: UseCase.UseCaseCallback<V>
    ) {
        mHandler.post {
            useCaseCallback.onSuccess(response)
        }
    }

    override fun <V : UseCase.ResponseValue> onError(
        useCaseCallback: UseCase.UseCaseCallback<V>
    ) {
        mHandler.post {
            useCaseCallback.onError()
        }
    }


}
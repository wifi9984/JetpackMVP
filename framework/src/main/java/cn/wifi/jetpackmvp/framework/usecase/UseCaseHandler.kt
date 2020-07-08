package cn.wifi.jetpackmvp.framework.usecase

import cn.wifi.jetpackmvp.framework.usecase.UseCase.ResponseValue
import cn.wifi.jetpackmvp.framework.usecase.UseCase.UseCaseCallback

/**
 * Runs {@link UseCase}s using a {@link UseCaseScheduler}
 */
open class UseCaseHandler private constructor(useCaseScheduler: UseCaseScheduler) {

    companion object {
        var INSTANCE: UseCaseHandler? = null

        fun getInstance(): UseCaseHandler {
            if (INSTANCE == null) {
                INSTANCE = UseCaseHandler(UseCaseThreadPoolScheduler())
            }
            return INSTANCE!!
        }

        private class UiCallbackWrapper<V : ResponseValue>(
            callback: UseCaseCallback<V>,
            useCaseHandler: UseCaseHandler
        ) : UseCaseCallback<V> {
            var mCallback: UseCaseCallback<V> = callback
            var mUseCaseHandler: UseCaseHandler = useCaseHandler

            override fun onSuccess(response: V) {
                mUseCaseHandler.notifyResponse(response, mCallback)
            }

            override fun onError() {
                mUseCaseHandler.notifyError(mCallback)
            }

        }
    }

    private var mUseCaseScheduler: UseCaseScheduler = useCaseScheduler

    open fun <T : UseCase.RequestValues, R : UseCase.ResponseValue> execute(
        useCase: UseCase<T, R>, values: T, callback: UseCase.UseCaseCallback<R>
    ) {
        useCase.mRequestValues = values
        useCase.mUseCaseCallback = UiCallbackWrapper(callback, this)

        mUseCaseScheduler.execute(Runnable {
            useCase.run()
        })
    }

    private fun <V : UseCase.ResponseValue> notifyResponse(
        response: V,
        useCaseCallback: UseCaseCallback<V>
    ) {
        mUseCaseScheduler.notifyResponse(response, useCaseCallback)
    }

    private fun <V : ResponseValue> notifyError(
        useCaseCallback: UseCaseCallback<V>
    ) {
        mUseCaseScheduler.onError(useCaseCallback)
    }
}
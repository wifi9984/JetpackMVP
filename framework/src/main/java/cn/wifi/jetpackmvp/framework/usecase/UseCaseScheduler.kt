package cn.wifi.jetpackmvp.framework.usecase

import cn.wifi.jetpackmvp.framework.usecase.UseCase.ResponseValue
import cn.wifi.jetpackmvp.framework.usecase.UseCase.UseCaseCallback

/**
 * Interface for schedulers, see {@link UseCaseThreadPoolScheduler}
 */
interface UseCaseScheduler {

    fun execute(runnable: Runnable)

    fun <V : ResponseValue> notifyResponse(
        response: V,
        useCaseCallback: UseCaseCallback<V>
    )

    fun <V : ResponseValue> onError(
        useCaseCallback: UseCaseCallback<V>
    )
}
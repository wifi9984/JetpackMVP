package cn.wifi.jetpackmvp.framework.usecase

/**
 * Use cases are the entry point to the domain layer.
 *
 * @param <Q> the request type
 * @param <P> the response type
 *
 * @author wifi9984
 * @date 2020/7/8
 */
abstract class UseCase<Q : UseCase.RequestValues, P : UseCase.ResponseValue> {

    var mRequestValues: Q? = null

    var mUseCaseCallback: UseCaseCallback<P>? = null

    fun run() {
        executeUseCase(mRequestValues!!)
    }

    protected abstract fun executeUseCase(requestValues: Q)

    /**
     * Data passed to a request
     */
    interface RequestValues {}

    /**
     * Data received from a request
     */
    interface ResponseValue {}

    interface UseCaseCallback<R> {
        fun onSuccess(response: R)
        fun onError()
    }
}
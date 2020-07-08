package cn.wifi.jetpackmvp.base

interface BaseView<T : BasePresenter> {

    fun setPresenter(presenter: T)
}
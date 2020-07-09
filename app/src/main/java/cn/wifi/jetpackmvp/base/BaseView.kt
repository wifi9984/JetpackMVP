package cn.wifi.jetpackmvp.base

interface BaseView<T : BasePresenter> {

    var presenter: T
}
package cn.wifi.jetpackmvp.framework.annotation

import cn.wifi.jetpackmvp.framework.base.BaseViewModel
import cn.wifi.jetpackmvp.framework.viewmodel.IViewModel
import kotlin.reflect.KClass

/**
 * Activity或Fragment使用此注解绑定对应的ViewModel
 * 适用于MVVM模式
 *
 * @author wifi9984
 * @date 2020/7/7
 */
annotation class BindViewModel (val value: KClass<out BaseViewModel<in IViewModel>>)
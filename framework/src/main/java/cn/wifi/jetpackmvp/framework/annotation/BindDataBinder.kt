package cn.wifi.jetpackmvp.framework.annotation

import cn.wifi.jetpackmvp.framework.databinder.IDataBinder
import cn.wifi.jetpackmvp.framework.view.IViewDelegate
import cn.wifi.jetpackmvp.framework.viewmodel.IViewModel
import kotlin.reflect.KClass

/**
 * ViewModel层和Presenter层使用此注解绑定对应的DataBinder。
 * 在ViewModel变化时可以通过此注解对应的dataBinder找到关联的dataBinder从而更新数据.
 * 适用于MVP模式
 *
 * @author wifi9984
 * @date 2020/7/7
 */
annotation class BindDataBinder (vararg val dataBinder : KClass<out IDataBinder<out IViewDelegate, out IViewModel>>)

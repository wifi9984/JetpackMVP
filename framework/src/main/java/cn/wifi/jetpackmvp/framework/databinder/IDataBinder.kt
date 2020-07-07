package cn.wifi.jetpackmvp.framework.databinder

import cn.wifi.jetpackmvp.framework.view.IViewDelegate
import cn.wifi.jetpackmvp.framework.viewmodel.IViewModel

/**
 * @author wifi9984
 * @date 2020/7/7
 *
 * 数据绑定接口
 */
interface IDataBinder<V : IViewDelegate, M : IViewModel> {

    /**
     * 提供数据和View的绑定能力，当数据变化时回调此方法来修改ViewModel
     *
     * @param viewDelegate 视图层代理
     * @param data ViewModel对象
     */
    fun notifyViewModelChanged(viewDelegate: V, data: M)
}
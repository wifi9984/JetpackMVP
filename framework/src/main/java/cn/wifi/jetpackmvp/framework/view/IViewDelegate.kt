package cn.wifi.jetpackmvp.framework.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar

/**
 * 视图层代理接口
 *
 * @author wifi9984
 * @date 2020/7/7
 */
interface IViewDelegate {

    /**
     * 加载布局，创建rootView
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    fun create(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)

    /**
     * 获取根布局
     */
    fun getRootView(): View

    /**
     * 返回选项菜单布局文件
     * @return optionsMenuId
     */
    fun getOptionsMenuId(): Int

    /**
     * 获取 ToolBar
     * @return toolbar
     */
    fun getToolbar(): Toolbar?

    /**
     * 初始化视图
     */
    fun initView()
}
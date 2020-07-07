package cn.wifi.jetpackmvp.framework.view

import android.app.Activity
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar

/**
 * 视图代理的抽象类定义，一般上层只需要实现此类即可。
 * 如果需要高度自定义可自行实现@{@link IViewDelegate}
 *
 * @author wifi9984
 * @date 2020/7/7
 */
abstract class AppViewDelegate : IViewDelegate {

    //缓存所有view
    protected val mViews = SparseArray<View>()

    //根view
    protected lateinit var root: View

    /**
     * 获取布局文件资源
     * @return layoutResId
     */
    abstract fun getRootLayoutId(): Int

    override fun create(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) {
        val rootLayoutId = getRootLayoutId()
        root = inflater.inflate(rootLayoutId, container, false)
    }

    override fun getOptionsMenuId(): Int {
        return 0
    }

    override fun getToolbar(): Toolbar? {
        return null
    }

    override fun getRootView(): View {
        return root
    }

    abstract override fun initView()

    /**
     * 视图绑定
     * @param id 视图ID
     */
    open fun <T : View?> bindView(id: Int): T? {
        var view: T? = mViews[id] as T
        if (view == null) {
            view = root.findViewById(id)
            mViews.put(id, view)
        }
        return view
    }

    /**
     * 通过id获取View的引用
     * @param id 视图ID
     */
    open fun <T : View?> get(id: Int): T? {
        return bindView(id)
    }

    /**
     * 设置点击监听，可以同时为多个视图绑定监听器
     * @param listener 点击监听器
     * @param ids 视图ID
     */
    open fun setOnClickListener(listener: View.OnClickListener, vararg ids: Int) {
        for (id in ids) {
            get<View>(id)!!.setOnClickListener(listener)
        }
    }

    /**
     * 获取Activity引用
     */
    open fun <T : Activity?> getActivity(): T {
        return root.context as T
    }

    /**
     * 弹出Toast
     * @param msg 消息内容
     */
    open fun toast(msg: String) {
        Toast.makeText(root.context.applicationContext, msg, Toast.LENGTH_SHORT).show()
    }
}
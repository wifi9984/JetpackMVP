package cn.wifi.jetpackmvp.framework.presenter

import android.os.Bundle
import android.util.ArrayMap
import android.view.Menu
import android.view.MotionEvent
import androidx.fragment.app.FragmentActivity
import cn.wifi.jetpackmvp.framework.annotation.BindDataBinder
import cn.wifi.jetpackmvp.framework.databinder.IDataBinder
import cn.wifi.jetpackmvp.framework.view.IViewDelegate
import cn.wifi.jetpackmvp.framework.viewmodel.IViewModel

abstract class ActivityPresenter<out V : IViewDelegate>() : FragmentActivity() {

    protected var viewDelegate: T = null

    protected val dataBinderMap: ArrayMap<String, IDataBinder<out IViewDelegate, out IViewModel>> = ArrayMap()

    private var shouldHideInput = true

    /**
     * 获取视图代理类
     */
    abstract fun getViewDelegateClass(): Class<V>

    /**
     * Presenter准备完成回调
     */
    abstract fun onPresenterCreated(savedInstanceState: Bundle)

    init {
        initDataBinderAndViewDelegate()
    }

    /**
     * 初始化dataBinder和viewDelegate
     */
    private fun initDataBinderAndViewDelegate() {
        try {
            val delegate: Class<V> = getViewDelegateClass()
            viewDelegate = delegate.newInstance()
            val bindDataBinderAnnotation = javaClass.getAnnotation(BindDataBinder::class.java)
            if (bindDataBinderAnnotation != null) {
                //初始化并缓存所有指定的dataBinder
                for (clazz in bindDataBinderAnnotation.dataBinder) {
                    val dataBinder = clazz.java.newInstance()
                    dataBinderMap[clazz.simpleName] = dataBinder
                }
            }
        } catch (iae: IllegalAccessException) {
            iae.printStackTrace()
        } catch (ie: InstantiationException) {
            ie.printStackTrace()
        }
    }

    /**
     * 获取ViewModel绑定的DataBinder对象（根据ViewModel的注解查找）
     */
    protected fun getDataBinder(viewModel: IViewModel): IDataBinder<out IViewDelegate, out IViewModel> {
        val bindDataBinder = viewModel.javaClass.getAnnotation(BindDataBinder::class.java)
        if (bindDataBinder == null) {
            throw RuntimeException("Not find BindDataBinder")
        }
        return dataBinderMap[bindDataBinder.dataBinder[0].simpleName]!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewDelegate!!.create(layoutInflater, null, savedInstanceState)
        setContentView(viewDelegate!!.getRootView())
    }

    open fun bindEventListener() {

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (viewDelegate == null || dataBinderMap.size == 0) {
            initDataBinderAndViewDelegate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (viewDelegate!!.getOptionsMenuId() != 0) {
            menuInflater.inflate(viewDelegate!!.getOptionsMenuId(), menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        viewDelegate = null
        dataBinderMap.clear()
        super.onDestroy()
    }

    open fun notifyViewModelChange(viewModel: IViewModel) {
        val dataBinder = getDataBinder(viewModel)
            ?: throw RuntimeException("Can not find DataBinder, just check your Presenter's annotation")
        dataBinder.notifyViewModelChanged(viewDelegate!!, viewModel)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null) {
            if (ev.action == MotionEvent.ACTION_DOWN) {
                var v = currentFocus
                // 键盘事件
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 是否开启点击输入法之外的地方自动隐藏软键盘的功能。
     * 默认开启
     *
     * @param hide 是否隐藏
     */
    open fun setShouldHideInput(hide: Boolean) {
        shouldHideInput = hide
    }
}
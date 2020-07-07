package cn.wifi.jetpackmvp.framework.base

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import cn.wifi.jetpackmvp.framework.annotation.BindViewModel
import cn.wifi.jetpackmvp.framework.viewmodel.IViewModel

abstract class BaseActivity<out M : IViewModel, out VM : BaseViewModel<out M>> : FragmentActivity() {

    private val bindViewModel: BindViewModel = javaClass.getAnnotation(BindViewModel::class.java)
        ?: throw RuntimeException("Not bind ViewModel")

    protected val viewModel by lazy {
        ViewModelProvider(this, SavedStateViewModelFactory(application, this)).get(bindViewModel.value.java)
    }

    /**
     * 是否可以隐藏软键盘
     */
    private var shouldHideInput = true

    /**
     * 返回根布局ID
     * @return 布局ID
     */
    abstract fun getLayoutId(): Int

    /**
     * 初始化准备完成回调
     */
    abstract fun onInitViews(savedInstanceState: Bundle?)

    /**
     * ViewModel数据变化时的回调
     */
    abstract fun onViewModelChanged(m: M)

    private fun init(savedInstanceState: Bundle?) {
        val layoutId = getLayoutId()
        if (layoutId == 0) {
            throw RuntimeException("RootView layout cannot be null");
        }
        setContentView(layoutId)
        initToolbar()
        initViewModel()
        onInitViews(savedInstanceState)
        bindEventListener()
    }

    private fun initViewModel() {
        viewModel.getModelLiveData().observe(this, Observer<M>{ m -> onViewModelChanged(m) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (viewModel == null) {
            init(savedInstanceState)
        }
    }

    open fun bindEventListener() {

    }

    protected fun initToolbar() {

    }

    open fun setOnClickListener(listener: View.OnClickListener, vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id).setOnClickListener(listener)
        }
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
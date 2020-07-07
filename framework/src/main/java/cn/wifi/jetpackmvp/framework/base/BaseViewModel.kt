package cn.wifi.jetpackmvp.framework.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import cn.wifi.jetpackmvp.framework.viewmodel.IViewModel

/**
 * ViewModel抽象类，负责Model与View直接的通信
 *
 * @author wifi9984
 * @date 2020/7/7
 */
class BaseViewModel<M : IViewModel>(application: Application) : AndroidViewModel(application) {

    private var modelLiveData: MutableLiveData<M> = MutableLiveData()

    fun notifyDataChanged(data: M) {
        modelLiveData.postValue(data)
    }

    override fun onCleared() {
        super.onCleared()
    }

    fun getModelLiveData(): LiveData<M> {
        return modelLiveData
    }
}
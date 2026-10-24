package com.ncertbooks.enginea.view.list

import androidx.lifecycle.MutableLiveData
import com.ncertbooks.enginea.bean.InstalledAppBean
import com.ncertbooks.enginea.data.AppsRepository
import com.ncertbooks.enginea.view.base.BaseViewModel


class ListViewModel(private val repo: AppsRepository) : BaseViewModel() {

    val appsLiveData = MutableLiveData<List<InstalledAppBean>>()

    val loadingLiveData = MutableLiveData<Boolean>()

    fun previewInstalledList() {
        launchOnUI { repo.previewInstallList() }
    }

    fun getInstallAppList(userID: Int) {
        launchOnUI { repo.getInstalledAppList(userID, loadingLiveData, appsLiveData) }
    }
}

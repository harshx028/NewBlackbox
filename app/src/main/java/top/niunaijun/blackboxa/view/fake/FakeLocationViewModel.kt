package com.ncertbooks.enginea.view.fake

import androidx.lifecycle.MutableLiveData
import com.ncertbooks.engine.entity.location.BLocation
import com.ncertbooks.enginea.bean.FakeLocationBean
import com.ncertbooks.enginea.data.FakeLocationRepository
import com.ncertbooks.enginea.view.base.BaseViewModel


class FakeLocationViewModel(private val mRepo: FakeLocationRepository) : BaseViewModel() {

    val appsLiveData = MutableLiveData<List<FakeLocationBean>>()


    fun getInstallAppList(userID: Int) {
        launchOnUI {
            mRepo.getInstalledAppList(userID, appsLiveData)
        }
    }

    fun setPattern(userId: Int, pkg: String, pattern: Int) {
        launchOnUI {
            mRepo.setPattern(userId, pkg, pattern)
        }
    }

    fun setLocation(userId: Int, pkg: String, location: BLocation) {
        launchOnUI {
            mRepo.setLocation(userId, pkg, location)
        }
    }

}
package com.ncertbooks.enginea.view.gms

import androidx.lifecycle.MutableLiveData
import com.ncertbooks.enginea.bean.GmsBean
import com.ncertbooks.enginea.bean.GmsInstallBean
import com.ncertbooks.enginea.data.GmsRepository
import com.ncertbooks.enginea.view.base.BaseViewModel


class GmsViewModel(private val mRepo: GmsRepository) : BaseViewModel() {

    val mInstalledLiveData = MutableLiveData<List<GmsBean>>()

    val mUpdateInstalledLiveData = MutableLiveData<GmsInstallBean>()

    fun getInstalledUser() {
        launchOnUI {
            mRepo.getGmsInstalledList(mInstalledLiveData)
        }
    }

    fun installGms(userID: Int) {
        launchOnUI {
            mRepo.installGms(userID,mUpdateInstalledLiveData)
        }
    }

    fun uninstallGms(userID: Int) {
        launchOnUI {
            mRepo.uninstallGms(userID,mUpdateInstalledLiveData)
        }
    }
}
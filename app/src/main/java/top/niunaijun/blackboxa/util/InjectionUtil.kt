package com.ncertbooks.enginea.util

import com.ncertbooks.enginea.data.AppsRepository
import com.ncertbooks.enginea.data.FakeLocationRepository
import com.ncertbooks.enginea.data.GmsRepository

import com.ncertbooks.enginea.view.apps.AppsFactory
import com.ncertbooks.enginea.view.fake.FakeLocationFactory
import com.ncertbooks.enginea.view.gms.GmsFactory
import com.ncertbooks.enginea.view.list.ListFactory



object InjectionUtil {

    private val appsRepository = AppsRepository()



    private val gmsRepository = GmsRepository()

    private val fakeLocationRepository = FakeLocationRepository()

    fun getAppsFactory() : AppsFactory {
        return AppsFactory(appsRepository)
    }

    fun getListFactory(): ListFactory {
        return ListFactory(appsRepository)
    }


    fun getGmsFactory():GmsFactory{
        return GmsFactory(gmsRepository)
    }

    fun getFakeLocationFactory():FakeLocationFactory{
        return FakeLocationFactory(fakeLocationRepository)
    }
}
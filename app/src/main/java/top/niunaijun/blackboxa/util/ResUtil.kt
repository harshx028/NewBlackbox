package com.ncertbooks.enginea.util

import androidx.annotation.StringRes
import com.ncertbooks.enginea.app.App


fun getString(@StringRes id:Int,vararg arg:String):String{
    if(arg.isEmpty()){
        return App.getContext().getString(id)
    }
    return App.getContext().getString(id,*arg)
}


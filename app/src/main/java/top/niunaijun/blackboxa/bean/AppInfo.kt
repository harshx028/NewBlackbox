package com.ncertbooks.enginea.bean

import android.graphics.drawable.Drawable


data class AppInfo(val name:String,val icon:Drawable?,val packageName:String,val sourceDir:String,val isXpModule:Boolean)
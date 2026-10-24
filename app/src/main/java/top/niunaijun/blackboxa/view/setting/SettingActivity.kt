package com.ncertbooks.enginea.view.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.ncertbooks.enginea.R
import com.ncertbooks.enginea.databinding.ActivitySettingBinding
import com.ncertbooks.enginea.util.inflate
import com.ncertbooks.enginea.view.base.BaseActivity

class SettingActivity : BaseActivity() {

    private val viewBinding: ActivitySettingBinding by inflate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        initToolbar(viewBinding.toolbarLayout.toolbar, R.string.setting, true)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, SettingFragment())
                .commit()
    }

    companion object{
        fun start(context: Context){
            val intent = Intent(context,SettingActivity::class.java)
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            context.startActivity(intent)
        }
    }

}
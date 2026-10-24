package com.ncertbooks.enginea.view.gms

import android.view.View
import android.view.ViewGroup
import cbfg.rvadapter.RVHolder
import cbfg.rvadapter.RVHolderFactory
import com.ncertbooks.enginea.R
import com.ncertbooks.enginea.bean.GmsBean
import com.ncertbooks.enginea.databinding.ItemGmsBinding


class GmsAdapter : RVHolderFactory() {

    override fun createViewHolder(parent: ViewGroup?, viewType: Int, item: Any): RVHolder<out Any> {
        return GmsVH(inflate(R.layout.item_gms,parent))
    }


    class GmsVH(itemView:View):RVHolder<GmsBean>(itemView){

        private val binding = ItemGmsBinding.bind(itemView)
        override fun setContent(item: GmsBean, isSelected: Boolean, payload: Any?) {
            binding.tvTitle.text = item.userName
            binding.checkbox.isChecked = item.isInstalledGms
            binding.checkbox.setOnCheckedChangeListener  { buttonView, _ ->
                if(buttonView.isPressed){
                    binding.root.performClick()
                }
            }
        }
    }
}
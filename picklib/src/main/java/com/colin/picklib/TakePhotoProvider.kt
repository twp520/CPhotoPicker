package com.colin.picklib

import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.provider.BaseItemProvider

class TakePhotoProvider : BaseItemProvider<ImageTakePhoto, BaseViewHolder>() {
    override fun layout(): Int {
        return R.layout.item_picker_tp
    }

    override fun viewType(): Int {
        return 1
    }

    override fun convert(helper: BaseViewHolder?, data: ImageTakePhoto?, position: Int) {

    }
}
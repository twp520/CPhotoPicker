package com.colin.picklib

import android.widget.ImageView
import com.bumptech.glide.load.DecodeFormat
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.provider.BaseItemProvider

class ImageProvider : BaseItemProvider<Image, BaseViewHolder>() {
    override fun layout(): Int {
        return R.layout.item_picker
    }

    override fun viewType(): Int {
        return 0
    }

    override fun convert(helper: BaseViewHolder, item: Image, position: Int) {
        val photo = helper.getView<ImageView>(R.id.item_picker_photo)
        GlideApp.with(mContext)
                .load(item.imagePath)
                .thumbnail(0.25f)
                .format(DecodeFormat.PREFER_RGB_565)
                .into(photo)
        helper.setChecked(R.id.item_picker_ckb, item.isChecked)
        helper.addOnClickListener(R.id.item_picker_ckb)
    }
}
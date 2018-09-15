package com.colin.picklib

import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.MultipleItemRvAdapter

class ImageAdapterM(data: MutableList<MyItemType>) : MultipleItemRvAdapter<MyItemType, BaseViewHolder>(data) {

    //图片类型是 0，加号类型是1
    private var mAllAlbum: Album? = null

    init {
        finishInitialize()
    }

    override fun registerItemProvider() {
        mProviderDelegate.registerProvider(ImageProvider())
        mProviderDelegate.registerProvider(TakePhotoProvider())
    }

    override fun getViewType(t: MyItemType): Int {
        return t.getItemType()
    }

    override fun replaceData(data: MutableCollection<out MyItemType>) {
        // 不是同一个引用才清空列表
        if (data !== mData) {
            mData.removeAll {
                it.getItemType() == 0
            }
            mData.addAll(data)
        }
        notifyDataSetChanged()
    }

    fun setAllAlbum(album: Album) {
        this.mAllAlbum = album
    }

    fun checkedInAll(item: Image) {
        val index = mAllAlbum?.images?.indexOf(item)
        if (index != null && index != -1) {
            mAllAlbum?.images?.get(index)?.isChecked = item.isChecked
        }
    }

    fun getCheckedCount(): Int {
        return mAllAlbum?.images?.count {
            it.isChecked
        } ?: 0
    }

    fun getCheckedImage(): ArrayList<Image> {
        val result: ArrayList<Image> = arrayListOf()
        mAllAlbum?.images?.forEach {
            if (it.isChecked)
                result.add(it)
        }
        return result
    }
}
package com.colin.picklib

import android.os.Parcel
import android.os.Parcelable

/**
 *create by colin 2018/9/14
 *
 * 图片实体
 */
data class Image(var id: Int) : Parcelable, MyItemType {

    var imagePath: String = ""
    var imageFile: String = ""
    var isChecked: Boolean = false

    constructor(parcel: Parcel) : this(parcel.readInt()) {
        imagePath = parcel.readString()
        imageFile = parcel.readString()
        isChecked = parcel.readByte() != 0.toByte()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(imagePath)
        parcel.writeString(imageFile)
        parcel.writeByte(if (isChecked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Image> {
        override fun createFromParcel(parcel: Parcel): Image {
            return Image(parcel)
        }

        override fun newArray(size: Int): Array<Image?> {
            return arrayOfNulls(size)
        }
    }

    override fun getItemType(): Int {
        return 0
    }
}
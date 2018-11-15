package com.colin.picklib

import android.app.Activity
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment

/**
 *create by colin 2018/9/14
 */
class ImagePicker private constructor(private var parameters: PickParameters) {

    fun start() {
        if (parameters.activity == null && parameters.fragment == null)
            return
        val intent = if (parameters.activity != null) {
            Intent(parameters.activity, ImagePickerAct::class.java)
        } else {
            Intent(parameters.fragment?.activity, ImagePickerAct::class.java)
        }
        intent.putExtra("parameter", parameters)
        when {
            parameters.activity != null -> parameters.activity?.startActivityForResult(intent, parameters.requestCode)
            parameters.fragment != null -> parameters.fragment?.startActivityForResult(intent, parameters.requestCode)
            else -> {
                throw NullPointerException("没有启动项")
            }
        }
    }

    data class PickParameters(var id: Int) : Parcelable {
        var activity: Activity? = null
        var fragment: Fragment? = null
        var requestCode = -1
        //        var colorRes: Int = -1
        var maxCount: Int = 9
        var needCrop: Boolean = false
        var singleModel: Boolean = false
        var needLarge: Boolean = true

        constructor(parcel: Parcel) : this(parcel.readInt()) {
//            activity = parcel.
            requestCode = parcel.readInt()
//            colorRes = parcel.readInt()
            maxCount = parcel.readInt()
            needCrop = parcel.readByte() != 0.toByte()
            singleModel = parcel.readByte() != 0.toByte()
            needLarge = parcel.readByte() != 0.toByte()
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeInt(requestCode)
//            parcel.writeInt(colorRes)
            parcel.writeInt(maxCount)
            parcel.writeByte(if (needCrop) 1 else 0)
            parcel.writeByte(if (singleModel) 1 else 0)
            parcel.writeByte(if (needLarge) 1 else 0)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<PickParameters> {
            override fun createFromParcel(parcel: Parcel): PickParameters {
                return PickParameters(parcel)
            }

            override fun newArray(size: Int): Array<PickParameters?> {
                return arrayOfNulls(size)
            }
        }
    }

    class Builder {
        private var mParameter: PickParameters = PickParameters(0)

        fun with(activity: Activity): Builder {
            mParameter.activity = activity
            return this
        }

        fun with(fragment: Fragment): Builder {
            mParameter.fragment = fragment
            return this
        }

        fun requestCode(requestCode: Int): Builder {
            mParameter.requestCode = requestCode
            return this
        }

        fun maxCount(maxCount: Int): Builder {
            mParameter.maxCount = maxCount
            return this
        }

        fun needCrop(needCrop: Boolean): Builder {
            mParameter.needCrop = needCrop
            return this
        }

        fun isSingleModel(isSingle: Boolean): Builder {
            mParameter.singleModel = isSingle
            return this
        }

        fun needLarge(needLarge: Boolean): Builder {
            mParameter.needLarge = needLarge
            return this
        }

        fun build(): ImagePicker {
            if (mParameter.activity == null && mParameter.fragment == null) {
                throw NullPointerException("没有启动项，请先调用with方法")
            }
            if (mParameter.requestCode == 0) {
                throw NullPointerException("请调用requestCode方法指定code")
            }
            return ImagePicker(mParameter)
        }
    }
}
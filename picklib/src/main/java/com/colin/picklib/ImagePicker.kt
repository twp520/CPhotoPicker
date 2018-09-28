package com.colin.picklib

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment

/**
 *create by colin 2018/9/14
 */
class ImagePicker private constructor(var parameters: PickParameters) {

    fun start() {
        val intent = if (parameters.activity != null) {
            Intent(parameters.activity, ImagePickerAct::class.java)
        } else {
            Intent(parameters.fragment?.activity, ImagePickerAct::class.java)
        }
        intent.putExtra("maxCount", parameters.maxCount)
        intent.putExtra("needCrop", parameters.needCrop)
        intent.putExtra("single", parameters.singleModel)
        if (parameters.colorRes != -1) {
            intent.putExtra("color", parameters.colorRes)
        }
        when {
            parameters.activity != null -> parameters.activity?.startActivityForResult(intent, parameters.requestCode)
            parameters.fragment != null -> parameters.fragment?.startActivityForResult(intent, parameters.requestCode)
            else -> {
                throw NullPointerException("没有启动项")
            }
        }
    }

    private data class PickParameters(var id: Int) {
        var activity: Activity? = null
        var fragment: Fragment? = null
        var requestCode = -1
        var colorRes: Int = -1
        var maxCount: Int = 9
        var needCrop: Boolean = false
        var singleModel: Boolean = false
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

        /*fun themColor(colorRes: Int): Builder {
            this.colorRes = colorRes
            return this
        }*/

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
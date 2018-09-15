package com.colin.picklib

import android.app.Activity
import android.content.Intent
import android.support.annotation.ColorRes
import android.support.v4.app.Fragment

/**
 *create by colin 2018/9/14
 */
class ImagePicker {

    private var activity: Activity? = null
    private var fragment: Fragment? = null
    private var requestCode = -1
    private var colorRes: Int = -1
    private var maxCount: Int = 9
    private var needCrop: Boolean = false

    constructor(activity: Activity, requestCode: Int, @ColorRes colorRes: Int, maxCount: Int = 9, needCrop: Boolean = false) {
        this.activity = activity
        this.requestCode = requestCode
        this.colorRes = colorRes
        this.maxCount = maxCount
        this.needCrop = needCrop
    }

    constructor(fragment: Fragment, requestCode: Int, @ColorRes colorRes: Int, maxCount: Int = 9, needCrop: Boolean = false) {
        this.fragment = fragment
        this.requestCode = requestCode
        this.colorRes = colorRes
        this.maxCount = maxCount
        this.needCrop = needCrop
    }

    fun start() {
        if (activity != null) {
            val intent = Intent(activity, ImagePickerAct::class.java)
            intent.putExtra("maxCount", this.maxCount)
            intent.putExtra("needCrop", this.needCrop)
            if (colorRes != -1) {
                intent.putExtra("color", colorRes)
            }
            activity?.startActivityForResult(intent, requestCode)
            return
        }

        if (fragment != null) {
            val intent = Intent(fragment?.activity, ImagePickerAct::class.java)
            intent.putExtra("maxCount", this.maxCount)
            intent.putExtra("needCrop", this.needCrop)
            if (colorRes != -1) {
                intent.putExtra("color", colorRes)
            }
            fragment?.startActivityForResult(intent, requestCode)
            return
        }

        throw NullPointerException("没有启动选项")
    }


    class Builder {
        private var activity: Activity? = null
        private var fragment: Fragment? = null
        private var requestCode = -1
        private var colorRes: Int = -1
        private var maxCount: Int = 9
        private var needCrop: Boolean = false

        fun with(activity: Activity): Builder {
            this.activity = activity
            return this
        }

        fun with(fragment: Fragment): Builder {
            this.fragment = fragment
            return this
        }

        fun requestCode(requestCode: Int): Builder {
            this.requestCode = requestCode
            return this
        }

        fun maxCount(maxCount: Int): Builder {
            this.maxCount = maxCount
            return this
        }

        fun needCrop(needCrop: Boolean): Builder {
            this.needCrop = needCrop
            return this
        }

        /*fun themColor(colorRes: Int): Builder {
            this.colorRes = colorRes
            return this
        }*/

        fun build(): ImagePicker {
            return if (activity != null) {
                ImagePicker(activity!!, requestCode, colorRes, maxCount, needCrop)
            } else {
                ImagePicker(fragment!!, requestCode, colorRes, maxCount, needCrop)
            }
        }
    }
}
package com.colin.picklib

/**
 *create by colin 2018/9/14
 *
 * 相册实体
 */
data class Album(var name: String = "") {

    var images: ArrayList<Image> = arrayListOf()

    override fun toString(): String {
        return "$name (${images.size})"
    }
}
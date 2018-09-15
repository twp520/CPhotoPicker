package com.colin.picklib

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View

class GalleryItemDecoration(private val context: Context, private val left: Int, private val right: Int, private val top: Int, private val bottom: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.top = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, top.toFloat(), context.resources.displayMetrics).toInt()
        outRect.bottom = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottom.toFloat(), context.resources.displayMetrics).toInt()
        outRect.left = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, left.toFloat(), context.resources.displayMetrics).toInt()
        outRect.right = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, right.toFloat(), context.resources.displayMetrics).toInt()

    }
}

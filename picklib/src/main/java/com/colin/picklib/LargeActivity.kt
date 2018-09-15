package com.colin.picklib

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_large.*

class LargeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        setContentView(R.layout.activity_large)
        if (intent.extras == null)
            return
        val path = intent.extras.getString("path")
        GlideApp.with(this).load(path).into(large_photo)
    }
}

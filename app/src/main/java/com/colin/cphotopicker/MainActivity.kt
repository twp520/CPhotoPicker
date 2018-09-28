package com.colin.cphotopicker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.colin.picklib.Image
import com.colin.picklib.ImagePicker
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun test(view: View) {
        ImagePicker.Builder().with(this)
                .requestCode(200)
                .maxCount(9)
                .isSingleModel(true)
                .needCrop(true)
                .build()
                .start()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 200 && resultCode == Activity.RESULT_OK && data != null) {
            val images = data.getParcelableArrayListExtra<Image>("images")
            val builder = StringBuilder()
            images.forEach {
                builder.append(it.imagePath).append('\n')
            }
            main_tv.text = builder
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}

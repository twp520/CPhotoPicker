package com.colin.picklib

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter.ALPHAIN
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_image_picke.*


class ImagePickerAct : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var mAdapter: ImageAdapterM? = null
    private var spinnerAdapter: ArrayAdapter<Album>? = null
    private lateinit var scanner: ImageScanner
    private var maxChecked = 9
    private var needCrop = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picke)
        window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        maxChecked = intent.getIntExtra("maxCount", 9)
        needCrop = intent.getBooleanExtra("needCrop", false)


        initToolbar()
        initImageList()
        scanner = ImageScanner(contentResolver)
        val permission = RxPermissions(this)
        permission.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .subscribe { hasPermission ->
                    if (hasPermission) {
                        scanImage()
                    }
                }
        initEvent()
    }

    private fun initToolbar() {
        setSupportActionBar(picker_toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        picker_toolbar.setNavigationOnClickListener {
            finish()
        }
        picker_tv_count.text = getString(R.string.picker_done, 0, maxChecked)
    }

    private fun initImageList() {
        mAdapter = ImageAdapterM(mutableListOf())
        mAdapter?.addData(ImageTakePhoto())
        mAdapter?.openLoadAnimation(ALPHAIN)
        picker_list.adapter = mAdapter
        picker_list.addItemDecoration(GalleryItemDecoration(this, 2, 2, 2, 2))
    }

    private fun scanImage() {
        scanner.getImageAlbum().observe({ lifecycle }, {
            it?.let { albumList: MutableList<Album> ->
                mAdapter?.setAllAlbum(albumList[0])
                initSpinner(albumList)
            }
        })
    }

    private fun initSpinner(albumList: MutableList<Album>) {
        spinnerAdapter = ArrayAdapter(this, R.layout.layout_spinner_withe, albumList)
        spinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        picker_spinner.adapter = spinnerAdapter
        picker_spinner.onItemSelectedListener = this
    }

    private fun initEvent() {
        mAdapter?.setOnItemClickListener { _, _, position ->
            //跳转到大图页面查看大图
            val item = mAdapter!!.data[position]
            if (item.getItemType() == 0 && item is Image) {
                val bundle = Bundle()
                bundle.putString("path", item.imagePath)
                jumpActivity(LargeActivity::class.java, bundle)
            } else if (item.getItemType() == 1 && item is ImageTakePhoto) {
                //TODO 拍照，并接受拍照结果，并设置为 Image 类型回传给调用者
                takePhoto()
            }
        }
        mAdapter?.setOnItemChildClickListener { _, view, position ->
            when (view.id) {
                R.id.item_picker_ckb -> {
                    if (maxChecked == 0) {
                        return@setOnItemChildClickListener
                    }
                    val item = mAdapter!!.data[position]
                    if (item is Image) {
                        item.isChecked = !item.isChecked
                        mAdapter?.notifyItemChanged(position)
                        mAdapter?.checkedInAll(item)
                        //改变按钮上的数字加
                        picker_tv_count.text = getString(R.string.picker_done,
                                mAdapter?.getCheckedCount()
                                        ?: 0, maxChecked)
                    }
                }
            }
        }
    }

    private fun takePhoto() {
        val imageFile = scanner.generateCameraFile()
        if (imageFile == null) {
            Toast.makeText(this, "Please check if the storage space is sufficient", Toast.LENGTH_LONG).show()
            return
        }
        val uri = scanner.getUriFromFile(this, imageFile)
        val captureIntent = scanner.getCaptureIntent(uri)
        startActivityForResult(captureIntent, 100)
    }

    private fun handleCropIntent(imageUri: Uri) {
        val outUri = Uri.parse(scanner.generateCropFilePath())
        val cropIntent = scanner.getCropIntent(imageUri, outUri)
        startActivityForResult(cropIntent, 200)
    }

    private fun takePhotoResultSuccess(path: String) {
        val photo = Image(0)
        photo.isChecked = false
        photo.imageFile = path
        photo.imagePath = "file:///$path"
        val intent = Intent()
        intent.putParcelableArrayListExtra("images", arrayListOf(photo))
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //拍照成功
            val photoPath = scanner.generateCameraFilePath()
            if (TextUtils.isEmpty(photoPath)) {
                Toast.makeText(this, "Please check if the storage space is sufficient", Toast.LENGTH_LONG).show()
                return
            }
            if (needCrop) {//去裁剪
                val file = scanner.generateCameraFile() ?: return
                handleCropIntent(scanner.getUriFromFile(this, file))
            } else {
                //不需要裁剪，直接返回
                takePhotoResultSuccess(scanner.generateCameraFilePath())
            }
        } else if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            //裁剪成功
            takePhotoResultSuccess(scanner.getCropFilePath())
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //没有任何选中
        Log.d("picker", "没有任何选中")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        //切换相册源
        val album = spinnerAdapter?.getItem(position)
        mAdapter?.replaceData(album?.images ?: mutableListOf())
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_picker, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // 设置返回值，结束当前Activity
        val result = mAdapter?.getCheckedImage()
        if (result == null || result.isEmpty()) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }
        val intent = Intent()
        intent.putParcelableArrayListExtra("images", result)
        setResult(Activity.RESULT_OK, intent)
        finish()
        return true
    }

}
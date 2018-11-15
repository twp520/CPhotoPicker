package com.colin.picklib

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter.ALPHAIN
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.cpicker_activity_image_picke.*
import java.io.File


class ImagePickerAct : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var mAdapter: ImageAdapterM? = null
    private var spinnerAdapter: ArrayAdapter<Album>? = null
    private lateinit var scanner: ImageScanner
    private var maxChecked = 9
    private var needCrop = false //是否需要裁剪
    private var singleModel = false
    private var needLarge = true
    private var permission: RxPermissions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cpicker_activity_image_picke)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }

        val parameter = intent.getParcelableExtra<ImagePicker.PickParameters>("parameter")
        maxChecked = parameter.maxCount
        needCrop = parameter.needCrop
        singleModel = parameter.singleModel
        needLarge = parameter.needLarge
        initToolbar()
        initImageList()
        scanner = ImageScanner(this,  intent.component.packageName)
        permission = RxPermissions(this)
        permission?.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                ?.subscribe { hasPermission ->
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
        picker_list.layoutManager = GridLayoutManager(this, 3)
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
        spinnerAdapter = ArrayAdapter(this, R.layout.cpicker_layout_spinner_withe, albumList)
        spinnerAdapter?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        picker_spinner.adapter = spinnerAdapter
        picker_spinner.onItemSelectedListener = this
    }

    private fun initEvent() {
        mAdapter?.setOnItemClickListener { _, view, position ->
            //跳转到大图页面查看大图
            val item = mAdapter!!.data[position]
            if (item.getItemType() == 0 && item is Image) {
                if (needLarge) {
                    val bundle = Bundle()
                    bundle.putString("path", item.imagePath)
                    jumpActivity(LargeActivity::class.java, bundle)
                } else {
                    val ckb = view.findViewById<CheckBox>(R.id.item_picker_ckb)
                    handItemClick(ckb, position)
                }
            } else if (item.getItemType() == 1 && item is ImageTakePhoto) {
                //拍照，并接受拍照结果，并设置为 Image 类型回传给调用者
                permission?.request(Manifest.permission.CAMERA)
                        ?.subscribe {
                            if (it) takePhoto()
                        }
            }
        }
        mAdapter?.setOnItemChildClickListener { _, view, position ->
            handItemClick(view, position)
        }
    }

    private fun handItemClick(view: View, position: Int) {
        if (view.id == R.id.item_picker_ckb) {
            if (maxChecked == 0) {
                return
            }
            val item = mAdapter!!.data[position]
            if (item is Image) {
                if (singleModel) {//判断是否是单选模式
                    //单选状态下，选择一张就回传，如果需要裁剪
                    if (needCrop) {
                        handleCropIntent(scanner.getUriFromFile( File(item.imageFile)))
                    } else {
                        setListResult(arrayListOf(item))
                    }
                } else {
                    val checked = mAdapter?.getCheckedCount() ?: 0
                    if (checked < maxChecked) {
                        item.isChecked = !item.isChecked
                        mAdapter?.notifyItemChanged(position)
                        mAdapter?.checkedInAll(item)
                        //改变按钮上的数字加
                        picker_tv_count.text = getString(R.string.picker_done, mAdapter?.getCheckedCount()
                                ?: 0, maxChecked)
                    } else {
                        val result = mAdapter?.getCheckedImage()
                        if (result == null || result.isEmpty()) {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                            return
                        }
                        setListResult(result)
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
        val uri = scanner.getUriFromFile(imageFile)
        val captureIntent = scanner.getCaptureIntent(uri)
        startActivityForResult(captureIntent, 100)
    }

    /**
     * 去裁剪
     * @param imageUri 图片uri
     */
    private fun handleCropIntent(imageUri: Uri) {
        val outUri = Uri.parse(scanner.generateCropFilePath())
        val cropIntent = scanner.getCropIntent(imageUri, outUri)
        startActivityForResult(cropIntent, 200)
    }

    /**
     * 拍照成功
     * @param path 最终图片路径
     */
    private fun takePhotoResultSuccess(path: String) {
        val photo = Image(0)
        photo.isChecked = false
        photo.imageFile = path
        photo.imagePath = "file:///$path"
        setListResult(arrayListOf(photo))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            //拍照成功
            /* val photoPath = scanner.generateCameraFile()?.absolutePath
             if (TextUtils.isEmpty(photoPath)) {
                 Toast.makeText(this, "Please check if the storage space is sufficient", Toast.LENGTH_LONG).show()
                 return
             }*/
            if (needCrop) {//去裁剪
                val file = scanner.generateCameraFile() ?: return
                handleCropIntent(scanner.getUriFromFile(file))
            } else {
                //不需要裁剪，直接返回
                takePhotoResultSuccess(scanner.generateCameraFile()?.absolutePath ?: "")
            }
        } else if (requestCode == 200) {
            //裁剪成功
            if (resultCode == Activity.RESULT_OK){
                takePhotoResultSuccess(scanner.getCropFilePath())
            }else{
                //清空选中状态
                mAdapter?.clear()
            }
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
        menuInflater.inflate(R.menu.cpicker_menu_picker, menu)
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
        setListResult(result)
        return true
    }

    private fun setListResult(result: ArrayList<Image>) {
        val intent = Intent()
        intent.putParcelableArrayListExtra("images", result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

}

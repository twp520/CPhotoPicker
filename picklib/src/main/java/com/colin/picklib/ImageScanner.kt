package com.colin.picklib

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import java.io.File


/**
 *create by colin 2018/9/14
 *
 * 图片扫描器
 */

//查询的uri
private val EXTERNAL_IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
//查询的字段
private val ALBUM_PROJECTION = arrayOf(MediaStore.Images.Media.BUCKET_ID, //相册ID
        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,//相册名字
        MediaStore.Images.Media._ID,//图片ID
        MediaStore.Images.Media.DATA)//图片地址

//各自列的序号
private const val BUCKET_ID = 0
private const val BUCKET_DISPLAY_NAME = 1
private const val IMAGE_ID = 2
private const val DATA = 3

//大小阈值
private const val ALBUM_SELECTION = "_size> 20000"

val photoCompressDirPath = Environment.getExternalStorageDirectory().absolutePath + "/CPhotoPickerCache"

fun Context.jumpActivity(clazz: Class<*>, args: Bundle? = null) {
    val intent = Intent(this, clazz)
    args?.let {
        intent.putExtras(it)
    }
    startActivity(intent)
}


class ImageScanner(private var mContentResolver: ContentResolver) {

    fun getImageAlbum(): LiveData<MutableList<Album>> {
        val liveData = MutableLiveData<MutableList<Album>>()
        Thread {
            val albumList = mutableListOf<Album>()
            val albumKes: HashSet<String> = hashSetOf()
            val cursor = mContentResolver.query(EXTERNAL_IMAGES_URI,
                    ALBUM_PROJECTION, ALBUM_SELECTION, null, null)
            val allAlbum = Album("所有图片")
            albumList.add(allAlbum)
            var album: Album? = null
            var bucketId: String?
            var image: Image?
            try {
                while (cursor.moveToNext()) {
                    image = Image(cursor.getInt(IMAGE_ID))
                    image.imageFile = cursor.getString(DATA)
                    image.imagePath = "file://" + cursor.getString(DATA)//供图片加载框架使用的路径
                    bucketId = cursor.getString(BUCKET_ID)
                    allAlbum.images.add(image)
                    if (!albumKes.contains(bucketId)) { //新的相册
                        album = Album()
                        album.name = cursor.getString(BUCKET_DISPLAY_NAME)
                        album.images.add(image)
                        albumList.add(album)
                        albumKes.add(bucketId)
                    } else { //同一个相册
                        album?.images?.add(image)
                    }
                }
            } finally {
                cursor.close()
                liveData.postValue(albumList)
            }
        }.start()
        return liveData
    }

    fun getCaptureIntent(outUri: Uri): Intent {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }


    fun getCropIntent(imgUri: Uri, outUri: Uri): Intent {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(imgUri, "image/*")
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true")
        //该参数可以不设定用来规定裁剪区的宽高比
        //        intent.putExtra("aspectX", 2);
        //        intent.putExtra("aspectY", 1);
        //该参数设定为你的imageView的大小
//        intent.putExtra("outputX", 600)
//        intent.putExtra("outputY", 500)
        intent.putExtra("scale", true)
        //是否返回bitmap对象
        intent.putExtra("return-data", false)
        //        intent.setData(outUri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())//输出图片的格式
        intent.putExtra("noFaceDetection", true) // 头像识别
        return intent
    }

    fun generateCameraFilePath(): String {
        return android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DCIM)
                .absolutePath + "/JPEG_CTravel_head.jpg"
    }

    fun generateCameraFile(): File? {
        return File(generateCameraFilePath())
    }

    fun generateCropFilePath(): String {
        return "file:///$photoCompressDirPath/JPEG_CTravel_head_crop.jpg"
    }

    fun getCropFilePath(): String {
        return "$photoCompressDirPath/JPEG_CTravel_head_crop.jpg"
    }

    fun getUriFromFile(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            FileProvider.getUriForFile(context, "com.colin.cpp.FileProvider", file)
        else
            Uri.fromFile(file)
    }

}
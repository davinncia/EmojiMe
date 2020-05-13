package com.davinciapp.emojime

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class MainViewModel(private val application: Application, private val bitmapUtils: BitmapUtils)
    : ViewModel() {

    private val photoMutable = MutableLiveData<Bitmap>()
    val photo: LiveData<Bitmap> = photoMutable

    private var tempPhotoPath = "" //TODO nullable ?
    private var resultBitmap: Bitmap? = null

    fun saveImage() {
        // Delete the temporary image file
        bitmapUtils.deleteImageFile(application, tempPhotoPath)
        // Save the image
        resultBitmap?.let { bitmapUtils.saveImage(application, it) }
    }

    fun deleteImageFile() {
        bitmapUtils.deleteImageFile(application, tempPhotoPath)
    }

    //fun resamplePic(): Bitmap = bitmapUtils.resamplePic(application, tempPhotoPath)
    fun processPic() {
        photoMutable.value = bitmapUtils.resamplePic(application, tempPhotoPath)
    }

    fun createTempFile(): File? = bitmapUtils.createTempImageFile(application)

    fun setTempPhotoPath(path: String) {
        tempPhotoPath = path
    }

    fun getTempPhotoPath() = tempPhotoPath

}
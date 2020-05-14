package com.davinciapp.emojime

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.log

class MainViewModel(private val application: Application,
                    private val bitmapUtils: BitmapUtils,
                    private val emojifier: Emojifier)
    : ViewModel() {

    private val photoMutable = MutableLiveData<Bitmap>()
    val photo: LiveData<Bitmap> = photoMutable

    private val facesNbr = MutableLiveData<Int>()
    val faces = facesNbr

    private var tempPhotoPath: String? = null

    fun saveImage() {
        // Delete the temporary image file
        Log.d("debuglog", "DELETING...")
        deleteImageFile()
        Log.d("debuglog", "DELETED")
        // Save the image
        photo.value?.let {
            Log.d("debuglog", "SAVING...")
            viewModelScope.launch(Dispatchers.Default) { bitmapUtils.saveImageInMediaStore(it, application) }
            Log.d("debuglog", "SAVED")
        }
    }


    fun deleteImageFile() {
        tempPhotoPath?.let { bitmapUtils.deleteImageFile(application, it) }
    }

    //fun resamplePic(): Bitmap = bitmapUtils.resamplePic(application, tempPhotoPath)
    fun processPic() {
        tempPhotoPath?.let { photoMutable.value = bitmapUtils.resamplePic(application, it) }
    }

    fun createTempFile(): File? {
        val file = bitmapUtils.createTempImageFile(application)
        tempPhotoPath = file?.absolutePath
        return file
    }

    fun getTempPhotoPath() = tempPhotoPath

    //--------------------------------------------------------------------------------------------//
    //                                       F A C E S   D E T E C T O R
    //--------------------------------------------------------------------------------------------//
    fun detectFaces() {
        viewModelScope.launch(Dispatchers.Default) {
            val nbr = emojifier.detectFaces(photo.value!!)
            withContext(Dispatchers.Main) {
                facesNbr.value = nbr
            }
        }
    }

}
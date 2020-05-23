package com.davinciapp.emojime

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
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

    private val emojiPhotoMutable = MutableLiveData<Bitmap>()
    val emojiPhoto: LiveData<Bitmap> = emojiPhotoMutable

    val isLoading = MutableLiveData<Boolean>()

    private var tempPhotoPath: String? = null

    fun saveImage() {
        isLoading.value = true
        // Delete the temporary image file
        deleteImageFile()
        // Save the image
        emojiPhoto.value?.let {
            viewModelScope.launch { bitmapUtils.saveImageInMediaStore(it, application) }
        }
        isLoading.value = false
    }

    fun deleteImageFile() {
        tempPhotoPath?.let { bitmapUtils.deleteImageFile(application, it) }
    }

    fun processPic() {
        tempPhotoPath?.let { photoMutable.value = bitmapUtils.resamplePic(application, it) }
    }

    fun createTempFile(): File? {
        val file = bitmapUtils.createTempImageFile(application)
        tempPhotoPath = file?.absolutePath
        return file
    }

    //--------------------------------------------------------------------------------------------//
    //                                       F A C E S   D E T E C T O R
    //--------------------------------------------------------------------------------------------//
    fun detectFaces() {
        isLoading.value = true

        viewModelScope.launch(Dispatchers.Default) {
            val treatedPhoto = emojifier.detectFaces(photo.value!!)
            withContext(Dispatchers.Main) {
                isLoading.value = false
                emojiPhotoMutable.value = treatedPhoto
            }
        }
    }

}
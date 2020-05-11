package com.davinciapp.emojime

import android.content.Context
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

//Creates the temporary image file in the cache directory.
@Throws(IOException::class)
fun createTempImageFile(context: Context): File? {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        .format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir: File? = context.externalCacheDir
    return File.createTempFile(
        imageFileName,  /* prefix */
        ".jpg",  /* suffix */
        storageDir /* directory */
    )
}

class BitmapUtils {

    companion object {
        const val FILE_PROVIDER_AUTHORITY = "com.davinciapp.fileprovider"
    }


}
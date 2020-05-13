package com.davinciapp.emojime

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


//TODO inject into VM
class BitmapUtils {

    companion object {
        const val FILE_PROVIDER_AUTHORITY = "com.davinciapp.fileprovider"
    }

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



    //Resamples the captured photo to fit the screen for better memory usage.
    fun resamplePic(context: Context, imagePath: String): Bitmap {

        // Get device screen size information
        val metrics = DisplayMetrics()
        val manager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        manager.defaultDisplay.getMetrics(metrics)
        val targetH = metrics.heightPixels
        val targetW = metrics.widthPixels

        // Get the dimensions of the original bitmap
        val bmOptions = BitmapFactory.Options()
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, bmOptions)
        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight

        // Determine how much to scale down the image
        val scaleFactor = Math.min(photoW / targetW, photoH / targetH)

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        return BitmapFactory.decodeFile(imagePath)
    }

    //Deletes image file for a given path.
    fun deleteImageFile(context: Context, imagePath: String): Boolean {
        // Get the file
        val imageFile = File(imagePath)

        // Delete the image
        val deleted = imageFile.delete()

        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            val errorMessage = context.getString(R.string.delete_error)
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
        return deleted
    }

    //Helper method for saving the image
    fun saveImage(context: Context, image: Bitmap): String? {
        //TODO: MediaStore
        var savedImagePath: String? = null

        // Create the new file in the external storage
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        val storageDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .toString() + "/Emojify"
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }

        // Save the new Bitmap
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Add the image to the system gallery
            galleryAddPic(context, savedImagePath)

            // Show a Toast with the save location
            val savedMessage = context.getString(R.string.saved_message)
            Toast.makeText(context, "$savedMessage \n$savedImagePath", Toast.LENGTH_SHORT).show()
        }
        return savedImagePath
    }

    //Helper method for adding the photo to the system photo gallery so it can be accessed from other apps
    //TODO: MediaStore again
    private fun galleryAddPic(
        context: Context,
        imagePath: String
    ) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri: Uri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

}
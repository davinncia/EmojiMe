package com.davinciapp.emojime

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.davinciapp.emojime.BitmapUtils.Companion.FILE_PROVIDER_AUTHORITY
import java.io.File
import java.io.IOException
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkStoragePermission()

    }

    private fun launchCamera() {
        Toast.makeText(this, "Yep", Toast.LENGTH_SHORT).show()


        //Creates a temporary image file and captures a picture to store in it.

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the temporary File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createTempImageFile(this)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                val mTempPhotoPath = photoFile.absolutePath;

                // Get the content URI for the image file
                val photoURI: Uri = FileProvider.getUriForFile (this, FILE_PROVIDER_AUTHORITY, photoFile)

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    //--------------------------------------------------------------------------------------------//
    //                                     P E R M I S S I O N
    //--------------------------------------------------------------------------------------------//
    private fun checkStoragePermission() {
        //Check permission
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //Not granted
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_RC)
        } else {
            //Granted
            launchCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            STORAGE_RC -> {
                //If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Granted
                    launchCamera()
                } else {
                    //Denied, boo!
                    //TODO: View
                }
                return
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }

    }

    companion object {
        private const val STORAGE_RC = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }

}

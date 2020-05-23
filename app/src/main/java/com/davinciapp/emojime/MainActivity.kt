package com.davinciapp.emojime

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.davinciapp.emojime.BitmapUtils.Companion.FILE_PROVIDER_AUTHORITY
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val imageView by bind<ImageView>(R.id.iv_picture)
    private val clearFab by bind<FloatingActionButton>(R.id.clear_button)
    private val saveFab by bind<FloatingActionButton>(R.id.save_button)
    private val progressCircle by bind<ProgressBar>(R.id.progress_circle)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this,
            ViewModelFactory.getInstance(application))[MainViewModel::class.java]

        viewModel.photo.observe(this, Observer {
            // Set the new bitmap to the ImageView
            imageView.setImageBitmap(it)
            Toast.makeText(this, "Inspecting image...", Toast.LENGTH_LONG).show()
            viewModel.detectFaces()

        })

        viewModel.emojiPhoto.observe(this, Observer {
            imageView.setImageBitmap(it)
        })

        viewModel.isLoading.observe(this, Observer { loading ->
            if (loading)
                progressCircle.visibility = View.VISIBLE
            else
                progressCircle.visibility = View.GONE
        })

        clearFab.setOnClickListener {
            clearImageFromView()
            // Delete the temporary image file
            viewModel.deleteImageFile()
        }

        saveFab.setOnClickListener {
            viewModel.saveImage()
            clearImageFromView()
        }

        checkStoragePermission()
    }

    //--------------------------------------------------------------------------------------------//
    //                                          C A M E R A
    //--------------------------------------------------------------------------------------------//
    private fun launchCamera() {
        //Creates a temporary image file and captures a picture to store in it.
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            // Create the temporary File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = viewModel.createTempFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                ex.printStackTrace()
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                // Get the content URI for the image file
                val photoURI: Uri =
                    FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, photoFile)

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage()
        } else {
            // Otherwise, delete the temporary image file
            viewModel.deleteImageFile()
        }
    }

    //--------------------------------------------------------------------------------------------//
    //                                              U I
    //--------------------------------------------------------------------------------------------//

    private fun processAndSetImage() {
        // Toggle Visibility of the views
        saveFab.visibility = View.VISIBLE;
        clearFab.visibility = View.VISIBLE;

        // Resample the saved image to fit the ImageView
        viewModel.processPic()
    }


    private fun clearImageFromView() {
        // Clear the image and toggle the view visibility
        imageView.setImageResource(0)
        clearFab.visibility = View.GONE
        saveFab.visibility = View.GONE
        checkStoragePermission() //Launches camera if granted
    }

    //--------------------------------------------------------------------------------------------//
    //                                     P E R M I S S I O N
    //--------------------------------------------------------------------------------------------//
    private fun checkStoragePermission() {
        //Check permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            //Not granted
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                STORAGE_RC
            )
        } else {
            //Granted
            launchCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            STORAGE_RC -> {
                //If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //Granted
                    launchCamera()
                } else {
                    //Denied, boo!
                    finish()
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

    fun <T : View> Activity.bind(@IdRes id: Int): Lazy<T> {
        return lazy { findViewById<T>(id) }
    }

}

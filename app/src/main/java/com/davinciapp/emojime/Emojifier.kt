package com.davinciapp.emojime

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.util.SparseArray
import android.widget.Toast
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector

class Emojifier(private val context: Context) {

    //Method for detecting faces in a bitmap, and drawing emoji depending on the facial expression.
    suspend fun detectFaces(photo: Bitmap): Int {

        // Create the face detector, disable tracking and enable classifications
        val detector = FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build()

        //Build the frame
        val frame = Frame.Builder().setBitmap(photo).build()

        //Detect the faces
        val faces: SparseArray<Face> = detector.detect(frame)
        return faces.size()
    }

    companion object {
        private const val EMOJI_SCALE_FACTOR = .9f
        private const val SMILING_PROB_THRESHOLD = .15
        private const val EYE_OPEN_PROB_THRESHOLD = .5

        //SINGLETON
        private var INSTANCE: Emojifier? = null

        fun getInstance(context: Context): Emojifier {
            if (INSTANCE == null) {
                synchronized(Emojifier) {
                    if (INSTANCE == null) {
                        INSTANCE = Emojifier(context)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}
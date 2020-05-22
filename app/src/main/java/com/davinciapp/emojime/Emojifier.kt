package com.davinciapp.emojime

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.util.SparseArray
import androidx.annotation.IdRes
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector

class Emojifier(private val context: Context) {

    //Method for detecting faces in a bitmap, and drawing emoji depending on the facial expression.
    suspend fun detectFaces(photo: Bitmap): Bitmap {

        // Create the face detector, disable tracking and enable classifications
        val detector = FaceDetector.Builder(context)
            .setTrackingEnabled(false)
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
            .build()
        //Build the frame
        val frame = Frame.Builder().setBitmap(photo).build()
        //Detect the faces
        val faces: SparseArray<Face> = detector.detect(frame)

        // Initialize result bitmap to original picture
        // Initialize result bitmap to original picture
        var resultBitmap: Bitmap = photo

        for (i in 0 until faces.size()) {

            //Get emoji bitmap
            val emojiBitmap = getBitmap(whichEmoji(faces.valueAt(i)), context)

            //Add the emojiBitmap to the proper position in the original image
            resultBitmap = addBitmapToFace (resultBitmap, emojiBitmap, faces.valueAt(i))
        }

        detector.release()

        return resultBitmap
    }

    @IdRes
    private fun whichEmoji(face: Face): Int {
        var mouth = -1
        var eyes = -1

        //MOUTH
        Log.d("debuglog", "Smilling prob= ${face.isSmilingProbability}")
        mouth = when {
            face.isSmilingProbability >= .5 -> SMILING
            face.isSmilingProbability in .05..0.5 -> NEUTRAL
            else -> FROWN
        }

        //EYES
        Log.d("debuglog", "RightEye open prob= ${face.isRightEyeOpenProbability}")
        Log.d("debuglog", "LeftEye open prob= ${face.isLeftEyeOpenProbability}")

        val leftOpen = face.isLeftEyeOpenProbability
        val rightOpen = face.isRightEyeOpenProbability
        eyes = when {
            leftOpen > .5 && rightOpen > .5 -> OPEN
            leftOpen <= .5 && rightOpen <= .5 -> CLOSED
            else -> WINK
        }

        //FACE
        return when {
            mouth == SMILING && eyes == OPEN -> R.drawable.smile_open
            mouth == SMILING && eyes == CLOSED -> R.drawable.smile_closed
            mouth == NEUTRAL && eyes == OPEN -> R.drawable.neutral_open
            mouth == NEUTRAL && eyes == CLOSED -> R.drawable.neutral_closed
            mouth == FROWN && eyes == OPEN -> R.drawable.frown_open
            mouth == FROWN && eyes == CLOSED -> R.drawable.frown_closed
            eyes == WINK -> R.drawable.wink
            else -> R.drawable.alien
        }
    }

    //Combines the original picture with the emoji bitmaps
    private fun addBitmapToFace(backgroundBitmap: Bitmap, emojiBitmap: Bitmap, face: Face): Bitmap {

        // Initialize the results bitmap to be a mutable copy of the original image
        var emojiBitmap = emojiBitmap
        val resultBitmap = Bitmap.createBitmap(
            backgroundBitmap.width, backgroundBitmap.height, backgroundBitmap.config)

        // Scale the emoji so it looks better on the face
        val scaleFactor = EMOJI_SCALE_FACTOR

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        val newEmojiWidth = (face.width * scaleFactor).toInt()
        val newEmojiHeight = (emojiBitmap.height *
                newEmojiWidth / emojiBitmap.width * scaleFactor).toInt()


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false)

        // Determine the emoji position so it best lines up with the face
        val emojiPositionX = face.position.x + face.width / 2 - emojiBitmap.width / 2
        val emojiPositionY = face.position.y + face.height / 2 - emojiBitmap.height / 3

        // Create the canvas and draw the bitmaps to it
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(backgroundBitmap, 0F, 0F, null)
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null)
        return resultBitmap
    }


    companion object {
        private const val EMOJI_SCALE_FACTOR = .9f

        //MOUTH
        private const val SMILING_PROB_THRESHOLD = .15
        private const val SMILING = 0
        private const val NEUTRAL = 1
        private const val FROWN = 2

        //EYES
        private const val EYE_OPEN_PROB_THRESHOLD = .5
        private const val OPEN = 0
        private const val CLOSED = 1
        private const val WINK = 2


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
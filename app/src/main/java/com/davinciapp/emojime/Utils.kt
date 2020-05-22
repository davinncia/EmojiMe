package com.davinciapp.emojime

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat

fun getBitmap(svgId: Int, context: Context): Bitmap {
    val vectorDrawable = ContextCompat.getDrawable(context, svgId) ?: throw IllegalArgumentException()
    val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return bitmap
}

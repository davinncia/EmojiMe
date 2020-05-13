package com.davinciapp.emojime

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ViewModelFactory private constructor(private val application: Application): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                return MainViewModel(application, BitmapUtils()) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

    companion object {
        private var INSTANCE: ViewModelFactory? = null

        fun getInstance(application: Application): ViewModelFactory {
            if (INSTANCE == null) {
                synchronized(ViewModelFactory) {
                    if (INSTANCE == null) {
                        INSTANCE = ViewModelFactory(application)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}
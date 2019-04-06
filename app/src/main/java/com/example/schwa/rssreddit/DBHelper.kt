package com.example.schwa.rssreddit

import android.content.Context
import com.example.schwa.rssreddit.feed.MyObjectBox
import io.objectbox.BoxStore
import java.util.logging.Level
import java.util.logging.Logger

object DBHelper {

    lateinit var boxStore: BoxStore
        private set

    fun getBoxStore(context: Context) : BoxStore {
        if (!DBHelper.isInitialized()) {
            Logger.getGlobal().log(Level.INFO, "Reinitialized BoxStore to avoid NPE")
            build(context)
        }
        return boxStore
    }

    fun isInitialized(): Boolean {
        return ::boxStore.isInitialized
    }

    fun build(context: Context) {
        // This is the minimal setup required on Android
        boxStore = MyObjectBox.builder().androidContext(context.applicationContext).build()

        // Example how you could use a custom dir in "external storage"
        // (Android 6+ note: give the app storage permission in app info settings)
//        val directory = File(Environment.getExternalStorageDirectory(), "objectbox-notes");
//        boxStore = MyObjectBox.builder().androidContext(context.applicationContext)
//                .directory(directory)
//                .build()
    }
}
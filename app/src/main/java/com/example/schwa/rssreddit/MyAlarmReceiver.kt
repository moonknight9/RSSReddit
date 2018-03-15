package com.example.schwa.rssreddit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


class MyAlarmReceiver : BroadcastReceiver() {

    // Triggered by the Alarm periodically (starts the service to run task)
    override fun onReceive(context: Context, intent: Intent) {
        //val i = Intent(context, NotificationService::class.java)
        val i = Intent("NotificationUpdate")
        NotificationService.enqueueWork(context, i)
    }

    companion object {
        const val REQUEST_CODE = 12345
    }
}

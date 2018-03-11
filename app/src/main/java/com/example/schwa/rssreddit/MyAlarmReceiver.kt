package com.example.schwa.rssreddit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle



class MyAlarmReceiver : BroadcastReceiver() {

    // Triggered by the Alarm periodically (starts the service to run task)
    override fun onReceive(context: Context, intent: Intent) {
        val i = Intent(context, NotificationService::class.java)
        //val bundle = Bundle()
        //bundle.putSerializable("jsonReaderList", seenSubRedditList)
        //intent.putExtras(bundle)
        //i.put("jsonReaderList", seenSubRedditList)
        context.startService(i)
    }

    companion object {
        const val REQUEST_CODE = 12345
    }
}

package com.example.schwa.rssreddit

import android.app.IntentService
import android.content.Intent

class NotificationService : IntentService("NotiService") {

    override fun onHandleIntent(intent: Intent) {
        //val rootView = (applicationContext as Activity).window.decorView.findViewById<View>(android.R.id.content)
        //val recycle = rootView.findViewById<RecyclerView>(R.id.my_recycler_view)
        JSONFactory.getJSONReader()!!.execute("https://www.reddit.com/r/NintendoSwitch/.json?limit=10")
    }
}

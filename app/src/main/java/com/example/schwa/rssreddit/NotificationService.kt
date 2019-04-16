package com.example.schwa.rssreddit

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService

class NotificationService : JobIntentService() {
    companion object {
        private const val JOB_ID = 20190402

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, NotificationService::class.java, JOB_ID, work)
        }

    }

    override fun onHandleWork(intent: Intent) {
        RedditJSONUtils.pullSubReddit(applicationContext)
    }
}

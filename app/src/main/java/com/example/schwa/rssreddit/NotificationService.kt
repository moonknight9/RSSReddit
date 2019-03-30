package com.example.schwa.rssreddit

import android.content.Context
import android.content.Intent
import android.support.v4.app.JobIntentService
import com.example.schwa.rssreddit.feed.SubReddit

class NotificationService : JobIntentService() {
    companion object {
        private const val JOB_ID = 20180311

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, NotificationService::class.java, JOB_ID, work)
        }

    }

    override fun onHandleWork(intent: Intent) {
        JSONReader(applicationContext).execute(*SubReddit.box().all.map { "https://www.reddit.com/r/${it.name}/.json?limit=${it.maxPostNum}" }.toTypedArray())
    }
}

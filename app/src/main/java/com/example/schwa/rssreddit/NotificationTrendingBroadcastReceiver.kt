package com.example.schwa.rssreddit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.schwa.rssreddit.feed.RedditPost
import java.util.logging.Level
import java.util.logging.Logger

class NotificationTrendingBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // maybe use this for cancel and click event.. should check intent.action in that case
        intent?.let {
            if (Intent.ACTION_VIEW == intent.action) {
                //TODO onCLick
            } else {
                notificationDismissAction(it, context)
            }
        }
    }

    private fun notificationDismissAction(intent: Intent, context: Context?) {
        val id = intent.getStringExtra(NotificationService.POST_ID)
        if (id != null && context != null) {
            Logger.getGlobal().log(Level.INFO, "$id marked as read")
            RedditPost.findPostById(id, context)?.let { post ->
                post.isRead = true
                post.save(context)
            }
        }
    }
}
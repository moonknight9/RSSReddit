package com.example.schwa.rssreddit

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.schwa.rssreddit.feed.RedditPost
import java.util.logging.Level
import java.util.logging.Logger

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        intent?.let {
            val id = it.getStringExtra(NotificationService.POST_ID)
            if (id != null && context != null) {
                Logger.getGlobal().log(Level.INFO, "$id marked as read")
                RedditPost.findPostById(id, context)?.let { post ->
                    post.isRead = true
                    post.save(context)
                }
            }
        }
    }
}
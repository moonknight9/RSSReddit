package com.example.schwa.rssreddit.feed

import android.app.NotificationManager
import android.content.Context
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import java.util.logging.Level
import java.util.logging.Logger

class SubRedditGroupHolder(subReddit: SubReddit, context: Context)
    : ExpandableGroup<RedditPostGroupHolder>(subReddit.name, subReddit.posts.map { RedditPostGroupHolder(it) }) {

    // Put any action that should happen on expand in here, like marking SubReddit as read
    var onExpand: () -> Unit = {}

    init {
        onExpand = {
            // TODO workaround to not mix UI and DB code, is there a better solution?
            subReddit.save(context)
            Logger.getGlobal().log(Level.INFO, "${subReddit.name} marked as read")

            cancelExpandedSubNotifications(subReddit, context)
        }
    }

    private fun cancelExpandedSubNotifications(subReddit: SubReddit, context: Context) {
        val notificationManager = (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
        subReddit.posts.forEach { notificationManager.cancel(it.hashCode()) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
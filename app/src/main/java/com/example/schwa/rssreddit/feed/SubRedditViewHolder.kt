package com.example.schwa.rssreddit.feed

import android.os.Parcelable
import android.view.View
import android.widget.TextView
import com.example.schwa.rssreddit.R
import com.example.schwa.rssreddit.subreddit.SubRedditCreationView
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder

class SubRedditViewHolder(view: View) : GroupViewHolder(view) {
    private var name: TextView = view.findViewById(R.id.sub_reddit_name)
    private var notiCount: TextView = view.findViewById(R.id.sub_notification_count)
    private var onExpand: () -> Unit = {}

    fun setSubRedditView(group: ExpandableGroup<Parcelable>) {
        setName(group)
        setNotificationNumber(group)
        setSubRedditCreationViewClickListener()
        setOnExpandAction(group as SubRedditGroupHolder)
    }

    private fun setName(group: ExpandableGroup<Parcelable>) {
        name.text = group.title
    }

    private fun setNotificationNumber(group: ExpandableGroup<Parcelable>) {
        notiCount.text = group.items
                .map { it as RedditPostGroupHolder }
                .filterNot { it.isRead }
                .count()
                .toString()
    }

    private fun setSubRedditCreationViewClickListener() {
        itemView.setOnLongClickListener {
            if (name.text.isNullOrBlank()) {
                false
            } else {
                SubRedditCreationView.start(itemView.context, name.text.toString())
                true
            }
        }
    }

    private fun setOnExpandAction(holder: SubRedditGroupHolder) {
        onExpand = holder.onExpand
    }

    override fun expand() {
        super.expand()
        onExpand()
    }
}
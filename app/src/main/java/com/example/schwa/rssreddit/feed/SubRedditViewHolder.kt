package com.example.schwa.rssreddit.feed

import android.os.Parcelable
import android.view.View
import android.widget.TextView
import com.example.schwa.rssreddit.R
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import java.util.logging.Level
import java.util.logging.Logger

class SubRedditViewHolder(view: View) : GroupViewHolder(view) {
    private var name: TextView = view.findViewById(R.id.sub_reddit_name)
    private var onExpand : () -> Unit = {}

    fun setSubRedditView(group: ExpandableGroup<Parcelable>) {
        setName(group)
        setSubRedditCreationViewClickListener()
        setOnExpandAction(group as SubRedditGroupHolder)
    }

    private fun setName(group: ExpandableGroup<Parcelable>) {
        name.text = group.title
    }

    private fun setSubRedditCreationViewClickListener() {
        itemView.setOnLongClickListener {
            if (name.text.isNullOrBlank()) {
                false
            } else {
                Feeds.getInstance().getSubRedditCreationForm(name.text.toString()).show()
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
        Logger.getGlobal().log(Level.INFO, "Expand called")
    }
}
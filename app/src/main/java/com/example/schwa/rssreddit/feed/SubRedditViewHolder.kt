package com.example.schwa.rssreddit.feed

import android.os.Parcelable
import android.view.View
import android.widget.TextView
import com.example.schwa.rssreddit.R
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder

class SubRedditViewHolder(view: View) : GroupViewHolder(view) {
    private var name: TextView = view.findViewById(R.id.sub_reddit_name)

    fun setSubRedditView(group: ExpandableGroup<Parcelable>) {
        setName(group)
        setSubRedditCreationViewClickListener()
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
}
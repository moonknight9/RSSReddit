package com.example.schwa.rssreddit.feed

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.schwa.rssreddit.R
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup


class FeedRecycleAdapter(feed: List<ExpandableGroup<RedditPostGroupHolder>>)
    : ExpandableRecyclerViewAdapter<SubRedditViewHolder, RedditPostViewHolder>(feed) {

    override fun onCreateGroupViewHolder(parent: ViewGroup, viewType: Int): SubRedditViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.subreddit_group_view, parent, false)
        return SubRedditViewHolder(view)
    }

    override fun onBindGroupViewHolder(holder: SubRedditViewHolder, flatPosition: Int, group: ExpandableGroup<Parcelable>) {
        holder.setSubRedditView(group)
    }

    override fun onCreateChildViewHolder(parent: ViewGroup, viewType: Int): RedditPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.subreddit_feeds, parent, false)
        return RedditPostViewHolder(parent.context, view)
    }

    override fun onBindChildViewHolder(holder: RedditPostViewHolder, flatPosition: Int, group: ExpandableGroup<Parcelable>, childIndex: Int) {
        holder.addThreadView(group.items[childIndex] as RedditPostGroupHolder)
    }

    //TODO only pull on expand? Loading is getting slow really quick
}

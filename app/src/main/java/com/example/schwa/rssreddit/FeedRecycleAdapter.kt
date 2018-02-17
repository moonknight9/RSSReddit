package com.example.schwa.rssreddit

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import java.util.*


class FeedRecycleAdapter(private val feed: ArrayList<SubReddit>) : RecyclerView.Adapter<FeedRecycleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.subreddit_feeds, parent, false) as RelativeLayout
        // set the view's size, margins, paddings and layout parameters
        //v.setPadding(0,10,0,10)
        return ViewHolder(parent.context, v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        feed[i].post.withIndex().forEach { addThreadView(viewHolder, it) }
        viewHolder.feedEntry.setPadding(20, 0, 20, 20)
    }

    private fun addThreadView(viewHolder: ViewHolder, it: IndexedValue<RedditPost>) {
        //Create TextView
        val thread = it.value
        val textView = TextView(viewHolder.parent)
        val id = it.index + 1
        textView.id = id
        textView.setBackgroundColor(if (id % 2 == 0) Color.WHITE else Color.LTGRAY)
        textView.text = """${thread.title}
            ${thread.ups} | ${thread.numComments}""".trimMargin()
        val defaultColor = (textView.background as ColorDrawable).color
        textView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    textView.setBackgroundColor(Color.DKGRAY)
                    viewHolder.parent.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(thread.url)))
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_MOVE ->
                    textView.setBackgroundColor(defaultColor)
            }
            true
        }
        //Add Layout
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.BELOW, id - 1)
        //Add View to Layout
        viewHolder.feedEntry.addView(textView, layoutParams)
    }

    override fun getItemCount(): Int {
        return feed.size
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(var parent: Context, var feedEntry: RelativeLayout) : RecyclerView.ViewHolder(feedEntry)
}

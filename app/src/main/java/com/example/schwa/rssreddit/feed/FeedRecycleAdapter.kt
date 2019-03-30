package com.example.schwa.rssreddit.feed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.squareup.picasso.Picasso
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


class FeedRecycleAdapter(private val feed: ArrayList<RedditPost>) : RecyclerView.Adapter<FeedRecycleAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create a new view
        val v = LayoutInflater.from(parent.context).inflate(R.layout.subreddit_feeds, parent, false) as LinearLayout
        // set the view's size, margins, paddings and layout parameters
        //v.setPadding(0,10,0,10)

        return ViewHolder(parent.context, v)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, i: Int) {
        //feed[i].posts.withIndex().forEach { addThreadView(viewHolder, it) }
        addThreadView(viewHolder, IndexedValue(i, feed[i]))
        //viewHolder.feedEntry.setPadding(20, 0, 20, 20)
    }

    private fun addThreadView(viewHolder: ViewHolder, it: IndexedValue<RedditPost>) {
        //Create TextView
        val thread = it.value

        val imgView = viewHolder.feedEntry.findViewById<View>(R.id.redditThumbnail) as ImageView
        if (thread.thumbnail.equals("self")) {
            Picasso.with(viewHolder.itemView.context)
                    .load(R.drawable.placeholder)
                    //.resize(metrics.widthPixels, 0)
                    .resize(140, 140)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(imgView)
            imgView.visibility = View.VISIBLE
        } else {
            setThumbnail(viewHolder, thread, imgView)
        }

        val textView = viewHolder.feedEntry.findViewById<View>(R.id.redditPostTitle) as TextView
        textView.text = """${thread.title}
            ${thread.ups} | ${thread.numComments}""".trimMargin()

        textView.setOnClickListener { _ ->
            viewHolder.parent.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(thread.permalink)))
        }
    }

    private fun setThumbnail(viewHolder: ViewHolder, thread: RedditPost, imgView: ImageView) {
        try {
            val metrics = DisplayMetrics()
            val wm = viewHolder.parent.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            Picasso.with(viewHolder.itemView.context)
                    .load(thread.thumbnail)
                    //.resize(metrics.widthPixels, 0)
                    .resize(140, 140)
                    .into(imgView)
            imgView.visibility = View.VISIBLE
            imgView.setOnClickListener { _ ->
                viewHolder.parent.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(thread.url)))
            }
        } catch (e: Exception) {
            Logger.getGlobal().log(Level.WARNING, """Thumnail ${thread.thumbnail} could not be loaded""")
        }
    }

    override fun getItemCount(): Int {
        return feed.size
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(var parent: Context, var feedEntry: LinearLayout) : RecyclerView.ViewHolder(feedEntry)
}

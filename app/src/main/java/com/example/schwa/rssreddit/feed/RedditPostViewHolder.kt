package com.example.schwa.rssreddit.feed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.example.schwa.rssreddit.R
import com.squareup.picasso.Picasso
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import java.util.logging.Level
import java.util.logging.Logger

class RedditPostViewHolder(var parent: Context, var view: View) : ChildViewHolder(view) {

    fun addThreadView(post: RedditPostGroupHolder) {

        val imgView = view.findViewById<View>(R.id.redditThumbnail) as ImageView
        if (post.thumbnail.equals("self")) {
            Picasso.with(view.context)
                    .load(R.drawable.placeholder)
                    //.resize(metrics.widthPixels, 0)
                    .resize(140, 140)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(imgView)
            imgView.visibility = View.VISIBLE
        } else {
            setThumbnail(post, imgView)
        }

        val textView = view.findViewById<View>(R.id.redditPostTitle) as TextView
        textView.text = """${post.title}
            ${post.ups} | ${post.numComments}""".trimMargin()

        textView.setOnClickListener {
            parent.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.permalink)))
        }
    }

    private fun setThumbnail(post: RedditPostGroupHolder, imgView: ImageView) {
        try {
            val metrics = DisplayMetrics()
            val wm = parent.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            Picasso.with(view.context)
                    .load(post.thumbnail)
                    //.resize(metrics.widthPixels, 0)
                    .resize(140, 140)
                    .into(imgView)
            imgView.visibility = View.VISIBLE
            imgView.setOnClickListener {
                parent.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.url)))
            }
        } catch (e: Exception) {
            Logger.getGlobal().log(Level.WARNING, """Thumnail ${post.thumbnail} could not be loaded""")
        }
    }
}
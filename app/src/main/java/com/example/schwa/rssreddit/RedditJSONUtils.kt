package com.example.schwa.rssreddit

import android.content.Context
import com.example.schwa.rssreddit.feed.RedditPost
import com.example.schwa.rssreddit.feed.SubReddit
import com.example.schwa.rssreddit.feed.ViewContainer
import org.json.JSONObject

object RedditJSONUtils {

    fun pullSubReddit(applicationContext: Context, viewContainer: ViewContainer? = null) {
        JSONReader(applicationContext, viewContainer)
                .execute(*SubReddit.box(applicationContext).all
                        .map { "https://www.reddit.com/r/${it.name}/.json?limit=${it.maxPostNum}" }
                        .toTypedArray())
    }

    fun getPostFromJSON(jsonObj: JSONObject): RedditPost {
        val post = RedditPost()
        post.id = jsonObj.getString("id")
        post.title = jsonObj.getString("title")
        post.numComments = jsonObj.getLong("num_comments")
        post.text = jsonObj.getString("selftext")
        post.ups = jsonObj.getLong("ups")
        post.url = jsonObj.getString("url")
        post.html = jsonObj.getString("selftext_html")
        post.permalink = "https://www.reddit.com" + jsonObj.getString("permalink")
        post.thumbnail = jsonObj.getString("thumbnail")
        return post
    }
}
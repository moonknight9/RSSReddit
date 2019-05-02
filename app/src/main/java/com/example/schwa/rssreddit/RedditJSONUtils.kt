package com.example.schwa.rssreddit

import android.content.Context
import android.os.AsyncTask
import com.example.schwa.rssreddit.feed.RedditPost
import com.example.schwa.rssreddit.feed.SubReddit
import com.example.schwa.rssreddit.feed.ViewContainer
import org.json.JSONArray
import org.json.JSONObject

object RedditJSONUtils {

    fun pullSubReddit(applicationContext: Context, viewContainer: ViewContainer? = null)
            : AsyncTask<String, Void, List<JSONObject>> {
        return JSONReader(applicationContext, viewContainer).execute(*generateRedditRequest(applicationContext))
    }

    private fun generateRedditRequest(applicationContext: Context): Array<String> {
        return SubReddit.box(applicationContext).all
                .map { "https://www.reddit.com/r/${it.name}/.json?limit=${it.maxPostNum}" }
                .toTypedArray()
    }

    fun getPostsFromJSONSub(jsonSubReddit: JSONObject): List<RedditPost> {
        val jsonPosts = getJSONPostsFromJSONSub(jsonSubReddit)

        return (0..(jsonPosts.length() - 1))
                .map { getJSONPostFromJSONPosts(jsonPosts, it) }
                //No daily question threads
                .filterNot { "AutoModerator" == it.getString("author") }
                .map { getPostFromJSON(it) }
                .toList()
    }

    private fun getJSONPostsFromJSONSub(jsonSubReddit: JSONObject) =
            jsonSubReddit.getJSONObject("data").getJSONArray("children")

    private fun getJSONPostFromJSONPosts(jsonPosts: JSONArray, i: Int) =
            jsonPosts.getJSONObject(i).getJSONObject("data")

    fun getSubNameFromJSONSub(jsonSubReddit: JSONObject): String {
        val posts = getJSONPostsFromJSONSub(jsonSubReddit)
        return if (posts.length() > 0) {
            getJSONPostFromJSONPosts(posts, 0).getString("subreddit")
        } else {
            ""
        }
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
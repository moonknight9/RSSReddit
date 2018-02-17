package com.example.schwa.rssreddit

import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger


class JSONReader(list: RecyclerView) : AsyncTask<String, Void, ArrayList<JSONObject>>() {

    private val listView = list

    override fun doInBackground(vararg params: String?): ArrayList<JSONObject> {
        val jsonArr = ArrayList<JSONObject>()
        var subName = ""
        params.map {
            subName = it.orEmpty()
            URL(it).readText()
        }.forEach {
                    try {
                        jsonArr.add(JSONObject(it))
                    } catch (e: JSONException) {
                        Toast.makeText(listView.context, """SubReddit $subName not found. Please check your connection.""", Toast.LENGTH_LONG).show()
                        Logger.getGlobal().log(Level.WARNING, e.message)
                    }
                }
        return jsonArr
    }

    override fun onPostExecute(resultList: ArrayList<JSONObject>) {
        super.onPostExecute(resultList)
        val subRedditList = ArrayList<SubReddit>()
        for (result in resultList) {
            val postList = ArrayList<RedditPost>()
            val posts = result.getJSONObject("data").getJSONArray("children")
            (0..(posts.length() - 1))
                    .map { posts.getJSONObject(it).getJSONObject("data") }
                    //No daily question threads
                    .filterNot { "AutoModerator" == it.getString("author") }
                    .mapTo(postList) { getRedditFromJSON(it) }
            subRedditList.add(SubReddit(postList))
        }
        listView.adapter = FeedRecycleAdapter(subRedditList)
    }

    private fun getRedditFromJSON(jsonObj: JSONObject): RedditPost {
        val post = RedditPost()
        post.title = jsonObj.getString("title")
        post.numComments = jsonObj.getLong("num_comments")
        post.text = jsonObj.getString("selftext")
        post.ups = jsonObj.getLong("ups")
        post.url = jsonObj.getString("url")
        post.html = jsonObj.getString("selftext_html")
        return post
    }
}

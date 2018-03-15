package com.example.schwa.rssreddit

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger


class JSONReader(list: RecyclerView, context: Context) : AsyncTask<String, Void, ArrayList<JSONObject>>() {

    val listView = list
    val appContext = context
    var seenSubRedditList: ArrayList<SubReddit>? = null

    override fun doInBackground(vararg params: String?): ArrayList<JSONObject> {
        Logger.getGlobal().log(Level.INFO, "<AsyncTask> Pulling new information from Reddit")
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

        val tmpSubRedditList = seenSubRedditList
        seenSubRedditList = ArrayList<SubReddit>()

        for (result in resultList) {
            val postList = ArrayList<RedditPost>()
            val posts = result.getJSONObject("data").getJSONArray("children")
            (0..(posts.length() - 1))
                    .map { posts.getJSONObject(it).getJSONObject("data") }
                    //No daily question threads
                    .filterNot { "AutoModerator" == it.getString("author") }
                    .mapTo(postList) { getRedditFromJSON(it) }
            seenSubRedditList!!.add(SubReddit(postList))
        }
        if (listView.isShown) {
            listView.adapter = FeedRecycleAdapter(seenSubRedditList!!)
        } else {
            //new List containing the updates
            val newList = seenSubRedditList
            //old list gets restored
            seenSubRedditList = tmpSubRedditList

            if (newList != null) {
                if (seenSubRedditList == null) {
                    seenSubRedditList = ArrayList<SubReddit>()
                }
                (0 until (newList.size)).forEach {
                    val newSubReddit = newList[it]
                    val oldSubReddit = seenSubRedditList!![it]
                    newSubReddit.post
                            .filterNot { oldSubReddit.post.contains(it) }
                            .filter { it.ups >= 800 }
                            .forEach { sendNotification(it) }
                }
            }
        }
        //sendNotification(seenSubRedditList!![0].post[0])
    }

    private fun sendNotification(post: RedditPost) {
        Logger.getGlobal().log(Level.INFO, "Notification send")
        val mBuilder = NotificationCompat.Builder(appContext, "CHANNELID")
                .setSmallIcon(R.drawable.stat_sys_warning)
                .setContentTitle(post.title)
                .setContentText(post.text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(post.text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("CHANNELID", "WhatEver", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(post.id!!.hashCode(), mBuilder.build())
    }

    private fun getRedditFromJSON(jsonObj: JSONObject): RedditPost {
        val post = RedditPost()
        post.id = jsonObj.getString("id")
        post.title = jsonObj.getString("title")
        post.numComments = jsonObj.getLong("num_comments")
        post.text = jsonObj.getString("selftext")
        post.ups = jsonObj.getLong("ups")
        post.url = jsonObj.getString("url")
        post.html = jsonObj.getString("selftext_html")
        post.permalink = "https://www.reddit.com" + jsonObj.getString("permalink")
        return post
    }
}

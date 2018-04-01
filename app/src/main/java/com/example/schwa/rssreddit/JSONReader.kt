package com.example.schwa.rssreddit

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.example.schwa.rssreddit.Feed.*
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger


class JSONReader(viewContainer: ViewContainer?, val context: Context) : AsyncTask<String, Void, ArrayList<JSONObject>>() {

    val container = viewContainer

    override fun doInBackground(vararg params: String?): ArrayList<JSONObject> {
        Logger.getGlobal().log(Level.INFO, "<AsyncTask> Pulling new information from Reddit")
        if (container != null && container.list.isShown) {
            Feeds.instance!!.runOnUiThread { Feeds.instance!!.swipeContainer!!.setRefreshing(true) }
        }
        val jsonArr = ArrayList<JSONObject>()
        var subName = ""
        params.map {
            subName = it.orEmpty()
            URL(it).readText()
        }.forEach {
            try {
                jsonArr.add(JSONObject(it))
            } catch (e: JSONException) {
                Toast.makeText(context,
                        """SubReddit $subName not found. Please check your connection.""",
                        Toast.LENGTH_LONG).show()
                Logger.getGlobal().log(Level.WARNING, e.message)
            }
        }
        return jsonArr
    }

    override fun onPostExecute(resultList: ArrayList<JSONObject>) {
        super.onPostExecute(resultList)

        val seenSubRedditList = getResultList(resultList)

        if (container != null && container.list.isShown) {
            //save new seen list
            container.seenSubRedditList = seenSubRedditList
            container.list.adapter = FeedRecycleAdapter(seenSubRedditList)
            Feeds.instance!!.runOnUiThread { Feeds.instance!!.swipeContainer!!.setRefreshing(false) }
        } else {
            if (container?.seenSubRedditList != null && container.seenSubRedditList!!.isEmpty()) {
                container.seenSubRedditList = seenSubRedditList
            } else {
                generateNotifications(seenSubRedditList)
            }
        }
    }

    private fun getResultList(resultList: ArrayList<JSONObject>): ArrayList<SubReddit> {
        val seenSubRedditList = ArrayList<SubReddit>()
        for (result in resultList) {
            val postList = ArrayList<RedditPost>()
            val posts = result.getJSONObject("data").getJSONArray("children")
            (0..(posts.length() - 1))
                    .map { posts.getJSONObject(it).getJSONObject("data") }
                    //No daily question threads
                    .filterNot { "AutoModerator" == it.getString("author") }
                    .mapTo(postList) { getRedditFromJSON(it) }
            seenSubRedditList.add(SubReddit(postList))
        }
        return seenSubRedditList
    }

    private fun generateNotifications(seenSubRedditList: ArrayList<SubReddit>) {
        (0 until (seenSubRedditList.size)).forEach {
            val newSubReddit = seenSubRedditList[it]
            val oldSubReddit = if (container?.seenSubRedditList != null) container.seenSubRedditList!![it] else SubReddit(ArrayList())
            newSubReddit.post
                    .filterNot { oldSubReddit.post.contains(it) }
                    .filter { it.ups >= 1000 }
                    .forEach { sendNotification(it) }
        }
    }

    private fun sendNotification(post: RedditPost) {
        Logger.getGlobal().log(Level.INFO, "Notification send")

        val intent = Intent(context, Feeds::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val mBuilder = NotificationCompat.Builder(context, "CHANNELID")
                .setSmallIcon(R.drawable.ic_menu_more)
                .setContentTitle(post.title)
                .setContentText("""Upvotes: ${post.ups}
                     ${post.text}""".trimMargin())
                .setStyle(NotificationCompat.BigTextStyle().bigText(post.text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
        //.setContentIntent(pendingIntent)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

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

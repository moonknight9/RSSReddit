package com.example.schwa.rssreddit

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.widget.Toast
import com.example.schwa.rssreddit.Feed.*
import org.json.JSONObject
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger


class JSONReader(val context: Context, viewContainer: ViewContainer? = null) : AsyncTask<String, Void, ArrayList<JSONObject>>() {

    val container = viewContainer

    override fun doInBackground(vararg params: String?): ArrayList<JSONObject> {
        Logger.getGlobal().log(Level.INFO, "<AsyncTask> Pulling new information from Reddit")
        if (container != null && container.list.isShown) {
            Feeds.instance!!.runOnUiThread { Feeds.instance!!.swipeContainer!!.setRefreshing(true) }
        }
        val jsonArr = ArrayList<JSONObject>()
        var subName = ""
        try {
            params.map {
                subName = it.orEmpty()
                URL(it).readText()
            }.forEach { jsonArr.add(JSONObject(it)) }
        } catch (e: Exception) {
            Feeds.instance!!.runOnUiThread {
                Toast.makeText(context,
                        """SubReddit $subName not found. Please check your connection.""",
                        Toast.LENGTH_LONG).show()
            }
            Logger.getGlobal().log(Level.WARNING, e.message)
        }
        return jsonArr
    }

    override fun onPostExecute(resultList: ArrayList<JSONObject>) {
        super.onPostExecute(resultList)

        val seenSubRedditList = getResultList(resultList)

        val subBox = SubReddit.box()
        if (container != null && container.list.isShown) {
            //clean already seen Reddits and save new
            Feeds.instance!!.boxStore.boxFor(RedditPost::class.java).removeAll()
            subBox.removeAll()
            subBox.put(seenSubRedditList)

            //refresh layout with loaded list
            val list = ArrayList<RedditPost>()
            seenSubRedditList[0].posts.forEach { it -> list.add(it) }
            container.list.adapter = FeedRecycleAdapter(list)

            //cancel all notifications when app is loaded
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancelAll()
            Feeds.instance!!.runOnUiThread { Feeds.instance!!.swipeContainer!!.setRefreshing(false) }
        } else {
            val subList = subBox.all
            if (subList.isEmpty()) {
                Feeds.instance!!.boxStore.boxFor(RedditPost::class.java).removeAll()
                subBox.removeAll()
                SubReddit.box().put(seenSubRedditList)
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
            var subName = ""
            (0..(posts.length() - 1))
                    .map { posts.getJSONObject(it).getJSONObject("data") }
                    //No daily question threads
                    .filterNot { "AutoModerator" == it.getString("author") }
                    .mapTo(postList) {
                        subName = it.getString("subreddit")
                        getRedditFromJSON(it)
                    }
            seenSubRedditList.add(SubReddit(postList, subName))
        }
        return seenSubRedditList
    }

    private fun generateNotifications(seenSubRedditList: ArrayList<SubReddit>) {
        (0 until (seenSubRedditList.size)).forEach {
            val newSubReddit = seenSubRedditList[it]
            val oldSubReddit = SubReddit.box().query().equal(SubReddit_.name, newSubReddit.name).build().findFirst()

            newSubReddit.posts
                    .filterNot { oldSubReddit?.posts?.contains(it) ?: false }
                    .filter { it.ups >= 1500 }
                    .forEach { sendNotification(it) }
        }
    }

    private fun sendNotification(post: RedditPost) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.permalink))
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
                .setContentIntent(pendingIntent)
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
        post.thumbnail = jsonObj.getString("thumbnail")
        return post
    }
}

package com.example.schwa.rssreddit

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
import com.example.schwa.rssreddit.feed.*
import io.objectbox.Box
import org.json.JSONObject
import java.net.URL
import java.util.logging.Level
import java.util.logging.Logger


class JSONReader(val context: Context, viewContainer: ViewContainer? = null) : AsyncTask<String, Void, ArrayList<JSONObject>>() {

    val container = viewContainer

    override fun doInBackground(vararg params: String?): ArrayList<JSONObject> {
        Logger.getGlobal().log(Level.INFO, "<AsyncTask> Pulling new information from Reddit")
        if (container != null && container.list.isShown) {
            Feeds.getInstance().runOnUiThread { Feeds.getInstance().swipeContainer!!.isRefreshing = true }
        }
        val jsonArr = ArrayList<JSONObject>()
        var subName = ""
        try {
            params.map {
                subName = it.orEmpty()
                URL(it).readText()
            }.forEach { jsonArr.add(JSONObject(it)) }
        } catch (e: Exception) {
            Feeds.getInstance().runOnUiThread {
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

        val subBox = SubReddit.box(context)
        val seenSubRedditList = getResultList(resultList, subBox)

        if (container != null && container.list.isShown) {
            //refresh layout with loaded list
            container.list.adapter = FeedRecycleAdapter(seenSubRedditList.map {
                val subHolder = SubRedditGroupHolder(it)
                subHolder.onExpand = {
                    // TODO workaround to not mix UI and DB code, is there a better solution?
                    subBox.put(it)
                    Logger.getGlobal().log(Level.INFO, "${it.name} marked as read")
                }
                subHolder
            })

            //cancel all notifications when app is loaded
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancelAll()
            Feeds.getInstance().runOnUiThread { Feeds.getInstance().swipeContainer!!.isRefreshing = false }
        } else {
            val subList = subBox.all
            if (subList != null && subList.isEmpty()) {
                subBox.put(seenSubRedditList)
            } else {
                generateNotifications(seenSubRedditList)
            }
        }
    }

    private fun getResultList(resultList: ArrayList<JSONObject>, subBox: Box<SubReddit>): ArrayList<SubReddit> {
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
                        RedditJSONUtils.getPostFromJSON(it)
                    }
            // find DB object to be able to update it instead of creating a new SubReddit
            val subReddit = subBox.query().equal(SubReddit_.name, subName).build().findFirst()
            //TODO Fix post lateinit issue
            subReddit?.posts?.setRemoveFromTargetBox(true)
            // Setting RemoveFromTarget should auto delete old RedditPosts that are not in posts anymore
            subReddit?.posts?.clear()
            subReddit?.posts?.addAll(postList)
            // fallback to creating a new SubReddit if something goes wrong, "should" not happen
            seenSubRedditList.add(subReddit ?: SubReddit(postList, subName))
        }
        return seenSubRedditList
    }

    private fun generateNotifications(seenSubRedditList: ArrayList<SubReddit>) {
        (0 until (seenSubRedditList.size)).forEach {
            val newSubReddit = seenSubRedditList[it]
            val oldSubReddit = SubReddit.box(context).query().equal(SubReddit_.name, newSubReddit.name).build().findFirst()

            newSubReddit.posts
                    .filterNot { oldSubReddit?.posts?.contains(it) ?: false }
                    .filter { it.ups >= newSubReddit.reqUpVotes }
                    .forEach {
                        sendNotification(it)
                        // mark as "read"
                        RedditPost.box(context).put(it)
                    }
        }
    }

    private fun sendNotification(post: RedditPost) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.permalink))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

        val mBuilder = NotificationCompat.Builder(context, "CHANNELID")
                .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                .setContentTitle(post.title)
                .setContentText("""Upvotes: ${post.ups}
                     ${post.text}""".trimMargin())
                .setStyle(NotificationCompat.BigTextStyle().bigText(post.text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("CHANNELID", "Trending Post", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(post.id!!.hashCode(), mBuilder.build())
    }
}

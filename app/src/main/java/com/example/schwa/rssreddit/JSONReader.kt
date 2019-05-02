package com.example.schwa.rssreddit

import android.app.*
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
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger


class JSONReader(val context: Context, viewContainer: ViewContainer? = null) : AsyncTask<String, Void, List<JSONObject>>() {

    val container = viewContainer

    override fun doInBackground(vararg params: String?): List<JSONObject> {
        Logger.getGlobal().log(Level.INFO, "<AsyncTask> Pulling new information from Reddit")

        try {
            return params.map { URL(it).readText() }.map { JSONObject(it) }.toList()
        } catch (e: Exception) {
            Logger.getGlobal().log(Level.INFO, e.message)
        }

        return emptyList()
    }

    override fun onPostExecute(jsonSubReddits: List<JSONObject>) {
        super.onPostExecute(jsonSubReddits)

        if (jsonSubReddits.isEmpty()) {
            val noSubFound = "No SubReddit found. Please make sure you have a SubReddit and connection"
            Toast.makeText(context, noSubFound, Toast.LENGTH_LONG).show()
            return
        }

        val subReddits = getSubReddits(jsonSubReddits)

        if (container != null && container.list.isShown) {
            //refresh layout with loaded list
            container.list.adapter = FeedRecycleAdapter(subReddits.map { SubRedditGroupHolder(it, context) })

        } else {
            val subBox = SubReddit.box(context)
            val subList = subBox.all
            if (subList != null && subList.isEmpty()) {
                subBox.put(subReddits)
            } else {
                sendNotificationIfRequired(subReddits)
            }
        }
    }

    private fun getSubReddits(jsonSubReddits: List<JSONObject>): List<SubReddit> {
        return jsonSubReddits.map { getSubRedditFromJSON(it) }.toList()
    }

    private fun getSubRedditFromJSON(jsonSub: JSONObject): SubReddit {
        val postList = RedditJSONUtils.getPostsFromJSONSub(jsonSub)
        // find DB object to not create duplicates
        postList.forEach { updatePostDBID(RedditPost.box(context), it) }

        val subName = RedditJSONUtils.getSubNameFromJSONSub(jsonSub)
        // find DB object to be able to update it instead of creating a new SubReddit
        val subReddit = SubReddit.box(context).query().equal(SubReddit_.name, subName).build().findFirst()
        //TODO Fix post lateinit issue
        subReddit?.posts?.setRemoveFromTargetBox(true)
        // Setting RemoveFromTarget should auto delete old RedditPosts that are not in posts anymore
        subReddit?.posts?.clear()
        subReddit?.posts?.addAll(postList)
        // fallback to creating a new SubReddit if something goes wrong, "should" not happen
        return subReddit ?: SubReddit(postList, subName)
    }

    private fun updatePostDBID(box: Box<RedditPost>, post: RedditPost) {
        box.query().equal(RedditPost_.id, post.id!!).build().findFirst()?.let { post.dbId = it.dbId }
    }

    private fun sendNotificationIfRequired(seenSubRedditList: List<SubReddit>) {
        seenSubRedditList.filter { it.notiEnabled }.forEach { newSubReddit ->
            val oldSubReddit = SubReddit.box(context).query().equal(SubReddit_.name, newSubReddit.name).build().findFirst()

            newSubReddit.posts
                    .filterNot { oldSubReddit?.posts?.contains(it) ?: false }
                    .filter { it.ups >= newSubReddit.reqUpVotes }
                    .forEach { sendNotification(it) }
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

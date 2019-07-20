package com.example.schwa.rssreddit

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.JobIntentService
import android.support.v4.app.NotificationCompat
import com.example.schwa.rssreddit.feed.RedditPost

class NotificationService : JobIntentService() {
    companion object {
        private const val JOB_ID = 20190402
        const val POST_ID = "DELETE_INTENT_POST_ID"

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, NotificationService::class.java, JOB_ID, work)
        }

        fun sendNotification(context: Context, post: RedditPost) {
            val contentText = "Upvotes: ${post.ups}\n${post.text}".trimMargin()
            val mBuilder = NotificationCompat.Builder(context, "CHANNELID")
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(post.title)
                    .setContentText(contentText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(post.text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setContentIntent(getOpenURIPIntent(post.url, context))
                    .setDeleteIntent(getMarkPostAsReadPIntent(post.id, context))
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel("CHANNELID", "Trending Post", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = mBuilder.build()
            // Oreo and above https://stackoverflow.com/questions/14671453/catch-on-swipe-to-dismiss-event
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL

            notificationManager.notify(post.id.hashCode(), notification)
        }

        private fun getOpenURIPIntent(uri: String?, context: Context): PendingIntent? {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            return PendingIntent.getActivity(context, 0, intent, 0)
        }

        private fun getMarkPostAsReadPIntent(id: String?, context: Context): PendingIntent? {
            val intent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            intent.putExtra(POST_ID, id)
            return PendingIntent.getBroadcast(context, id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onHandleWork(intent: Intent) {
        try {
            RedditJSONUtils.pullSubReddit(applicationContext)
        } catch (e: Exception) {
            // already logged.. do nothing and just try later
        }
    }
}

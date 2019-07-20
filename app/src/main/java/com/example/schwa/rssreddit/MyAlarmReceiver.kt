package com.example.schwa.rssreddit

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.Toast
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger


class MyAlarmReceiver : BroadcastReceiver() {

    // Triggered by the Alarm periodically (starts the service to run task)
    override fun onReceive(context: Context, intent: Intent) {
        NotificationService.enqueueWork(context, intent)
    }

    companion object {
        private const val REQUEST_CODE = 12345

        fun scheduleAlarm(applicationContext: Context, alarm: AlarmManager, debug: Boolean) {
            val pIntent = getPendingNotificationIntent(applicationContext)
            // Setup periodic alarm every every half hour from this point onwards
            val interval: Long = TimeUnit.MINUTES.toMillis(
                    PreferenceManager.getDefaultSharedPreferences(applicationContext)
                            .getString("sync_frequency", "30")!!
                            .toLong())
            Logger.getGlobal().log(Level.INFO, "Refresh interval: $interval")
            if (debug) {
                Toast.makeText(applicationContext,
                        "Interval set to ${TimeUnit.MILLISECONDS.toMinutes(interval)}",
                        Toast.LENGTH_LONG).show()
            }

            val firstMillis = System.currentTimeMillis() // alarm is set right away
            // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
            // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
            alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, interval, pIntent)
        }

        private fun getPendingNotificationIntent(applicationContext: Context): PendingIntent {
            val intent = Intent(applicationContext, MyAlarmReceiver::class.java)
            return PendingIntent.getBroadcast(applicationContext, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        fun stopAlarm(applicationContext: Context, alarm: AlarmManager) {
            alarm.cancel(getPendingNotificationIntent(applicationContext))
        }
    }
}

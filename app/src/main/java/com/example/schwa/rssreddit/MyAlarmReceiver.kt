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
        //val i = Intent(context, NotificationService::class.java)
        //val i = Intent("NotificationUpdate")
        NotificationService.enqueueWork(context, intent)
    }

    companion object {
        const val REQUEST_CODE = 12345

        fun scheduleAlarm(applicationContext: Context, alarm: AlarmManager, debug: Boolean) {
            // Construct an intent that will execute the AlarmReceiver
            val intent = Intent(applicationContext, MyAlarmReceiver::class.java)
            // Create a PendingIntent to be triggered when the alarm goes off
            //val pIntent = PendingIntent.getService(this, MyAlarmReceiver.REQUEST_CODE,
            //        intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val pIntent = PendingIntent.getBroadcast(applicationContext, MyAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
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
    }
}

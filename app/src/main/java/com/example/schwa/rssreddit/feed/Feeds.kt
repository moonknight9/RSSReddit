package com.example.schwa.rssreddit.feed

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import com.example.schwa.rssreddit.DBHelper
import com.example.schwa.rssreddit.JSONReader
import com.example.schwa.rssreddit.MyAlarmReceiver
import com.example.schwa.rssreddit.R
import com.example.schwa.rssreddit.settings.SettingsActivity
import io.objectbox.android.AndroidObjectBrowser
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger


class Feeds : AppCompatActivity() {

    var swipeContainer: SwipeRefreshLayout? = null
    var DEBUG = true

    companion object {
        private var thisInstance: Feeds? = null
        fun getInstance(): Feeds {
            if (thisInstance == null) {
                thisInstance = Feeds()
            }
            return thisInstance!!
        }
    }

    init {
        thisInstance = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!DBHelper.isInitialized()) {
            DBHelper.build(this)
        }

        DEBUG = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("debug_switch", true)

        if (DEBUG) {
            val started = AndroidObjectBrowser(DBHelper.boxStore).start(this)
            Logger.getGlobal().log(Level.INFO, "ObjectBrowser", "Started: $started")
        }
        setContentView(R.layout.activity_feeds)

        //Toolbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Plus btn
        addSubButtonInit()

        swipeContainer = findViewById(R.id.swipeRecycler)
        // Configure the refreshing colors
        swipeContainer!!.setColorSchemeColors(
                ContextCompat.getColor(applicationContext, android.R.color.holo_blue_bright),
                ContextCompat.getColor(applicationContext, android.R.color.holo_green_light),
                ContextCompat.getColor(applicationContext, android.R.color.holo_orange_light),
                ContextCompat.getColor(applicationContext, android.R.color.holo_red_light)
        )

        swipeContainer!!.setOnRefreshListener {
            loadRList()
        }
        loadRList()
    }

    private fun addSubButtonInit() {
        val fab = findViewById<View>(R.id.add_sub_fab) as FloatingActionButton
        val inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val formElementsView = inflater.inflate(R.layout.create_sub, null, false)
        val subName = formElementsView.findViewById(R.id.sub_name) as EditText
        val subPosts = formElementsView.findViewById(R.id.sub_post_num) as EditText
        val subVotes = formElementsView.findViewById(R.id.sub_votes_num) as EditText
        val noti = formElementsView.findViewById(R.id.sub_notification_switch) as Switch
        val builder = AlertDialog.Builder(this)
                .setView(formElementsView)
                .setTitle("Add Student")
                .setPositiveButton("Add") { _: DialogInterface, _:Int ->
                    if (TextUtils.isEmpty(subName.text) || TextUtils.isEmpty(subVotes.text) || TextUtils.isEmpty(subPosts.text)) {
                        //TODO add check if subName is valid / already there etc
                        Toast.makeText(applicationContext, "Subreddit incomplete - not saved", Toast.LENGTH_SHORT).show()
                    } else {
                        val sub = SubReddit(subName.text.toString(),
                                Integer.parseInt(subPosts.text.toString()),
                                Integer.parseInt(subVotes.text.toString()),
                                noti.isEnabled)
                        SubReddit.box().put(sub)
                        Toast.makeText(applicationContext, "${subName.text} added", Toast.LENGTH_SHORT).show()
                        loadRList()
                    }
                }
                .setNegativeButton("Discard") { _: DialogInterface, _:Int ->
                    Toast.makeText(applicationContext, "Changes not saved", Toast.LENGTH_SHORT).show()
                }
                .create()
        fab.setOnClickListener { builder.show() }
    }

    private fun loadRList() {
        val feedView = findViewById<View>(R.id.my_recycler_view) as RecyclerView
        feedView.setHasFixedSize(true)
        feedView.layoutManager = LinearLayoutManager(this)
        JSONReader(applicationContext, ViewContainer(feedView)).execute(
                *SubReddit.box().all.map { "https://www.reddit.com/r/${it.name}/.json?limit=${it.maxPostNum}" }.toTypedArray()
                //"https://www.reddit.com/r/NintendoSwitch/.json?limit=10"
                //,"https://www.reddit.com/r/heroesofthestorm/.json?limit=5"
        )
        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("notifications_new_message", true)) {
            scheduleAlarm()
        } else if (DEBUG) {
            Toast.makeText(applicationContext, "Notification disabled", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_feeds, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        return if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingsActivity::class.java)
            startActivity(intent)
            true
        } else if (id == R.id.action_delete_subs) {
            SubReddit.box().removeAll()
            Toast.makeText(applicationContext, "All subs deleted", Toast.LENGTH_LONG).show()
            loadRList()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(applicationContext, MyAlarmReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        //val pIntent = PendingIntent.getService(this, MyAlarmReceiver.REQUEST_CODE,
        //        intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val pIntent = PendingIntent.getBroadcast(applicationContext, MyAlarmReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every every half hour from this point onwards
        val interval: Long = TimeUnit.MINUTES.toMillis(
                PreferenceManager.getDefaultSharedPreferences(applicationContext)
                        .getString("sync_frequency", "30").toLong())
        Logger.getGlobal().log(Level.INFO, "Refresh interval: $interval")
        if (DEBUG) {
            Toast.makeText(applicationContext,
                    "Interval set to ${TimeUnit.MILLISECONDS.toMinutes(interval)}",
                    Toast.LENGTH_LONG).show()
        }

        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, interval, pIntent)
    }


}

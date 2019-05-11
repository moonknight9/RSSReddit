package com.example.schwa.rssreddit.feed

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.example.schwa.rssreddit.DBHelper
import com.example.schwa.rssreddit.MyAlarmReceiver
import com.example.schwa.rssreddit.R
import com.example.schwa.rssreddit.RedditJSONUtils
import com.example.schwa.rssreddit.settings.SettingsActivity
import com.example.schwa.rssreddit.subreddit.SubRedditCreationView
import io.objectbox.android.AndroidObjectBrowser
import java.util.logging.Level
import java.util.logging.Logger


class Feeds : AppCompatActivity() {

    var debug = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!DBHelper.isInitialized()) {
            DBHelper.build(this)
        }

        debug = PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("debug_switch", true)

        if (debug) {
            val started = AndroidObjectBrowser(DBHelper.boxStore).start(this)
            Logger.getGlobal().log(Level.INFO, "ObjectBrowser", "Started: $started")
        }

        setContentView(R.layout.activity_feeds)

        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<FloatingActionButton>(R.id.add_sub_fab).setOnClickListener { SubRedditCreationView.start(this) }
        addRefreshLayout()

        loadRList()
    }

    private fun addRefreshLayout() {
        val swipeContainer = findViewById<SwipeRefreshLayout>(R.id.swipeRecycler)
        // Configure the refreshing colors
        swipeContainer.setColorSchemeColors(
                ContextCompat.getColor(applicationContext, android.R.color.holo_blue_bright),
                ContextCompat.getColor(applicationContext, android.R.color.holo_green_light),
                ContextCompat.getColor(applicationContext, android.R.color.holo_orange_light),
                ContextCompat.getColor(applicationContext, android.R.color.holo_red_light)
        )

        swipeContainer.setOnRefreshListener {
            loadRList()
        }
    }

    private fun loadRList() {
        val feedView = findViewById<View>(R.id.my_recycler_view) as RecyclerView
        feedView.setHasFixedSize(true)
        feedView.layoutManager = LinearLayoutManager(this)

        loadAndRefreshContainer(feedView)

        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("notifications_new_message", true)) {
            MyAlarmReceiver.scheduleAlarm(applicationContext, getSystemService(Context.ALARM_SERVICE) as AlarmManager, debug)
        } else if (debug) {
            Toast.makeText(applicationContext, "Notification disabled", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadAndRefreshContainer(feedView: RecyclerView) {
        // new thread to not freeze UI while waiting for pull
        val swipeContainer = findViewById<SwipeRefreshLayout>(R.id.swipeRecycler)
        Thread {
            swipeContainer.isRefreshing = true
            // call get to wait until pull is finished and turn refreshing off
            try {
                RedditJSONUtils.pullSubReddit(applicationContext, ViewContainer(feedView)).get()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            }
            swipeContainer.isRefreshing = false
        }.start()
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

        return when (id) {
            R.id.action_settings -> {
                val intent = Intent(applicationContext, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_delete_subs -> {
                SubReddit.box(applicationContext).removeAll()
                Toast.makeText(applicationContext, "All subs deleted", Toast.LENGTH_LONG).show()
                loadRList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}

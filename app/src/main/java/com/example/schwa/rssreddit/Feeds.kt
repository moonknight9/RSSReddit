package com.example.schwa.rssreddit

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View


class Feeds : AppCompatActivity() {

    var swipeContainer: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_feeds)

        //Toolbar
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        // Plus btn
        /*val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }*/
        swipeContainer = findViewById(R.id.swipeRecycler)
        // Configure the refreshing colors
        swipeContainer!!.setColorSchemeColors(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light
        )

        swipeContainer!!.setOnRefreshListener {
            loadRList()
        }
        loadRList()
    }

    private fun loadRList() {
        val feedView = findViewById<View>(R.id.my_recycler_view) as RecyclerView
        feedView.setHasFixedSize(true)
        feedView.layoutManager = LinearLayoutManager(this)
        val reader: JSONReader
        if (JSONFactory.reader != null) {
            reader = JSONFactory.getJSONReader()!!
        } else {
            reader = JSONReader(feedView, this)
            JSONFactory.reader = reader
        }
        reader.execute("https://www.reddit.com/r/NintendoSwitch/.json?limit=10"
                //        ,"https://www.reddit.com/r/heroesofthestorm/.json?limit=5"
        )
        scheduleAlarm()
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
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    // Setup a recurring alarm every half hour
    private fun scheduleAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        val intent = Intent(applicationContext, MyAlarmReceiver::class.java)
        // Create a PendingIntent to be triggered when the alarm goes off
        val pIntent = PendingIntent.getService(this, MyAlarmReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
        // Setup periodic alarm every every half hour from this point onwards
        val firstMillis = System.currentTimeMillis() // alarm is set right away
        val alarm = this.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_HALF_HOUR, pIntent)
    }
}

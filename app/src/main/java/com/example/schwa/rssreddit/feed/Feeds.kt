package com.example.schwa.rssreddit.feed

import android.app.AlarmManager
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
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import com.example.schwa.rssreddit.DBHelper
import com.example.schwa.rssreddit.MyAlarmReceiver
import com.example.schwa.rssreddit.R
import com.example.schwa.rssreddit.RedditJSONUtils
import com.example.schwa.rssreddit.settings.SettingsActivity
import io.objectbox.android.AndroidObjectBrowser
import java.util.logging.Level
import java.util.logging.Logger


class Feeds : AppCompatActivity() {

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

        addRefreshLayout()
        loadRList()
    }

    private fun addSubButtonInit() {
        val fab = findViewById<View>(R.id.add_sub_fab) as FloatingActionButton
        fab.setOnClickListener { getSubRedditCreationForm().show() }
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

    fun getSubRedditCreationForm(subRedditName: String? = null): AlertDialog {
        val inflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val formElementsView = inflater.inflate(R.layout.create_sub, null, false)

        val subName = formElementsView.findViewById(R.id.sub_name) as EditText
        val subPosts = formElementsView.findViewById(R.id.sub_post_num) as EditText
        val subVotes = formElementsView.findViewById(R.id.sub_votes_num) as EditText
        val noti = formElementsView.findViewById(R.id.sub_notification_switch) as Switch
        val delete = formElementsView.findViewById(R.id.delete_sub_button) as Button

        //Prefill Dialog if we got called with a name
        var subRedditID: Long? = null
        var subReddit: SubReddit? = null
        if (!subRedditName.isNullOrBlank()) {
            subReddit = SubReddit.box(applicationContext).query().equal(SubReddit_.name, subRedditName).build().findFirst()
            if (subReddit != null) {
                subRedditID = subReddit.id
                subName.setText(subReddit.name)
                subPosts.setText(subReddit.maxPostNum.toString())
                subVotes.setText(subReddit.reqUpVotes.toString())
                noti.isChecked = subReddit.notiEnabled
                delete.visibility = View.VISIBLE
            }
        }

        val subCreationDialog = AlertDialog.Builder(this)
                .setView(formElementsView)
                .setTitle("Add SubReddit")
                .setPositiveButton("Save") { _: DialogInterface, _: Int ->
                    if (TextUtils.isEmpty(subName.text) || TextUtils.isEmpty(subVotes.text) || TextUtils.isEmpty(subPosts.text)) {
                        //TODO add check if subName is valid / already there etc
                        Toast.makeText(applicationContext, "SubReddit incomplete - not saved", Toast.LENGTH_SHORT).show()
                    } else {
                        val sub = SubReddit(subName.text.toString(),
                                Integer.parseInt(subPosts.text.toString()),
                                Integer.parseInt(subVotes.text.toString()),
                                noti.isChecked)
                        if (subRedditID != null) {
                            sub.id = subRedditID
                        }
                        sub.posts.setRemoveFromTargetBox(true)
                        SubReddit.box(applicationContext).put(sub)
                        Toast.makeText(applicationContext, "${subName.text} added", Toast.LENGTH_SHORT).show()
                        loadRList()
                    }
                }
                .setNegativeButton("Discard") { _: DialogInterface, _: Int ->
                    Toast.makeText(applicationContext, "Changes not saved", Toast.LENGTH_SHORT).show()
                }
                .create()
        if (subReddit != null) {
            delete.setOnClickListener {
                subReddit.delete(applicationContext)
                Toast.makeText(applicationContext, "${subName.text} deleted", Toast.LENGTH_SHORT).show()
                loadRList()
                subCreationDialog.cancel()
            }
        }
        return subCreationDialog
    }

    private fun loadRList() {
        val feedView = findViewById<View>(R.id.my_recycler_view) as RecyclerView
        feedView.setHasFixedSize(true)
        feedView.layoutManager = LinearLayoutManager(this)

        loadAndRefreshContainer(feedView)

        if (PreferenceManager.getDefaultSharedPreferences(applicationContext).getBoolean("notifications_new_message", true)) {
            MyAlarmReceiver.scheduleAlarm(applicationContext, getSystemService(Context.ALARM_SERVICE) as AlarmManager, DEBUG)
        } else if (DEBUG) {
            Toast.makeText(applicationContext, "Notification disabled", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadAndRefreshContainer(feedView: RecyclerView) {
        // new thread to not freeze UI while waiting for pull
        val swipeContainer = findViewById<SwipeRefreshLayout>(R.id.swipeRecycler)
        Thread {
            swipeContainer.isRefreshing = true
            // call get to wait until pull is finished and turn refreshing off
            RedditJSONUtils.pullSubReddit(applicationContext, ViewContainer(feedView)).get()
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

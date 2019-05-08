package com.example.schwa.rssreddit.subreddit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import com.example.schwa.rssreddit.R
import com.example.schwa.rssreddit.feed.Feeds
import com.example.schwa.rssreddit.feed.SubReddit

class SubRedditCreationView : AppCompatActivity() {

    companion object {
        private const val NAME = "subRedditName"

        /**
         * Context needs to be an activity
         */
        fun start(context: Context, subName: String? = null) {
            val intent = Intent(context, SubRedditCreationView::class.java)
            subName?.let { intent.putExtra(SubRedditCreationView.NAME, subName) }
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.create_sub)

        preFillIfPossible()

        findViewById<Button>(R.id.sub_discard).setOnClickListener { goToFeedView() }
        findViewById<Button>(R.id.sub_save).setOnClickListener { onSaveClicked() }
    }

    private fun preFillIfPossible() {
        val subRedditName = intent.getStringExtra(NAME) ?: return

        val subReddit = SubReddit.findSubByName(subRedditName, applicationContext) ?: return

        findViewById<EditText>(R.id.sub_name).setText(subReddit.name)
        findViewById<EditText>(R.id.sub_post_num).setText(subReddit.maxPostNum.toString())
        findViewById<EditText>(R.id.sub_votes_num).setText(subReddit.reqUpVotes.toString())
        findViewById<Switch>(R.id.sub_notification_switch).isChecked = subReddit.notiEnabled

        val delete = findViewById<Button>(R.id.delete_sub_button)
        delete.visibility = View.VISIBLE
        delete.setOnClickListener {
            subReddit.delete(applicationContext)
            goToFeedView()
        }
    }

    private fun goToFeedView() {
        startActivity(Intent(applicationContext, Feeds::class.java))
    }

    private fun onSaveClicked() {
        val subName = findViewById<EditText>(R.id.sub_name).text.toString()
        val subPosts = findViewById<EditText>(R.id.sub_post_num).text.toString()
        val subVotes = findViewById<EditText>(R.id.sub_votes_num).text.toString()

        if (TextUtils.isEmpty(subName) || TextUtils.isEmpty(subVotes) || TextUtils.isEmpty(subPosts)) {
            Toast.makeText(applicationContext, "SubReddit incomplete - not saved", Toast.LENGTH_SHORT).show()
            return
        }
        val noti = findViewById<Switch>(R.id.sub_notification_switch)

        saveSubToDB(SubReddit(subName, Integer.parseInt(subPosts), Integer.parseInt(subVotes), noti.isChecked))

        Toast.makeText(applicationContext, "$subName saved", Toast.LENGTH_SHORT).show()

        goToFeedView()
    }

    private fun saveSubToDB(sub: SubReddit) {
        // update subID if this is an existing sub otherwise RemoveFromTargetBox does not work
        SubReddit.findSubByName(sub.name, applicationContext)?.let { sub.id = it.id }
        sub.posts.setRemoveFromTargetBox(true)
        SubReddit.box(applicationContext).put(sub)
    }
}
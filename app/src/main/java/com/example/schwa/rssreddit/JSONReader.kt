package com.example.schwa.rssreddit

import android.content.Context
import android.os.AsyncTask
import com.example.schwa.rssreddit.feed.*
import org.json.JSONObject
import java.net.URL
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
            throw RuntimeException("No SubReddit found. Please make sure you have a SubReddit and connection")
        }
    }

    override fun onPostExecute(jsonSubReddits: List<JSONObject>) {
        super.onPostExecute(jsonSubReddits)

        val subReddits = getSubReddits(jsonSubReddits)
        subReddits.forEach { it.save(context) }

        if (container != null && container.list.isShown) {
            //refresh layout with loaded list
            container.list.adapter = FeedRecycleAdapter(subReddits.map { SubRedditGroupHolder(it, context) })
        }

        sendNotificationIfRequired(subReddits)
    }

    private fun getSubReddits(jsonSubReddits: List<JSONObject>): List<SubReddit> {
        return jsonSubReddits.map { getSubRedditFromJSON(it) }.toList()
    }

    private fun getSubRedditFromJSON(jsonSub: JSONObject): SubReddit {
        val postList = RedditJSONUtils.getPostsFromJSONSub(jsonSub)
        // should this line be in JSONUtils?.. would mix up logic
        postList.forEach { it.updateFromDB(context) }

        val subName = RedditJSONUtils.getSubNameFromJSONSub(jsonSub)
        // find DB object to be able to update it instead of creating a new SubReddit
        val subReddit = SubReddit.box(context).query().equal(SubReddit_.name, subName).build().findFirst()
        // Setting RemoveFromTarget should auto delete old RedditPosts that are not in posts anymore
        subReddit?.posts?.clear()
        subReddit?.posts?.addAll(postList)
        // fallback to creating a new SubReddit if something goes wrong, "should" not happen
        return subReddit ?: SubReddit(postList, subName)
    }

    private fun sendNotificationIfRequired(pulledSubRedditList: List<SubReddit>) {
        pulledSubRedditList.filter { it.notiEnabled }.forEach { newSubReddit ->
            val oldSubReddit = SubReddit.box(context).query().equal(SubReddit_.name, newSubReddit.name).build().findFirst()

            newSubReddit.posts
                    .filterNot { oldSubReddit?.posts?.getById(it.dbId)?.isRead ?: false }
                    .filter { it.ups >= newSubReddit.reqUpVotes }
                    .forEach { NotificationService.sendNotification(context, it) }
        }
    }
}

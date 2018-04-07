package com.example.schwa.rssreddit

import android.content.Context
import android.support.v7.widget.RecyclerView
import com.example.schwa.rssreddit.Feed.ViewContainer

class JSONFactory {

    companion object {
        var container: ViewContainer? = null
        fun getJSONReader(context: Context, feedView: RecyclerView? = null): JSONReader {
            return JSONReader(getViewContainer(feedView), context)
        }

        private fun getViewContainer(feedView: RecyclerView?): ViewContainer? {
            return if (feedView != null) ViewContainer(feedView) else container
        }
    }
}

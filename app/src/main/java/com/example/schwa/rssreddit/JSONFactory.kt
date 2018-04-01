package com.example.schwa.rssreddit

import android.content.Context
import android.support.v7.widget.RecyclerView
import com.example.schwa.rssreddit.Feed.ViewContainer

class JSONFactory {

    companion object {
        private var reader: JSONReader? = null
        fun getJSONReader(context: Context, feedView: RecyclerView? = null): JSONReader {
            val newReader = JSONReader(getViewContainer(feedView), context)
            if (feedView != null) {
                reader = newReader
            }
            return newReader
        }

        private fun getViewContainer(feedView: RecyclerView? = null): ViewContainer? {
            return if (feedView != null) ViewContainer(feedView)
            else (if (reader != null) (reader as JSONReader).container else null)
        }
    }
}

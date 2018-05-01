package com.example.schwa.rssreddit

import com.example.schwa.rssreddit.Feed.Feeds
import io.objectbox.BoxStore

class DBHelper {
    companion object {
        fun box(): BoxStore? {
            return Feeds.getInstance().boxStore
        }
    }
}
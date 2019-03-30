package com.example.schwa.rssreddit.feed

import com.example.schwa.rssreddit.DBHelper
import io.objectbox.Box
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Transient
import io.objectbox.relation.ToOne

@Entity
class RedditPost {

    @Id
    var dbId: Long = 0
    lateinit var subreddit: ToOne<SubReddit>

    var id: String? = null
    var title: String? = null
    @Transient
    var text: String? = null
    @Transient
    var html: String? = null
    @Transient
    var ups: Long = 0
    @Transient
    var numComments: Long = 0
    var url: String? = null
    var permalink: String? = null
    var thumbnail: String? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RedditPost

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    companion object {
        fun box(): Box<RedditPost> {
            return DBHelper.boxStore.boxFor(RedditPost::class.java)
        }
    }
}

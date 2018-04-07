package com.example.schwa.rssreddit.Feed

class RedditPost {

    var id: String? = null
    var title: String? = null
    var text: String? = null
    var html: String? = null
    var ups: Long = 0
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


}

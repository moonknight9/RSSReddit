package com.example.schwa.rssreddit.feed

import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup

class SubRedditGroupHolder(subReddit: SubReddit) : ExpandableGroup<RedditPostGroupHolder>(subReddit.name, subReddit.posts.map { RedditPostGroupHolder(it) }) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        return true
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
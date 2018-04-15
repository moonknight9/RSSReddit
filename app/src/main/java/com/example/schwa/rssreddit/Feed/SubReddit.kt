package com.example.schwa.rssreddit.Feed

import io.objectbox.Box
import io.objectbox.annotation.Backlink
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

@Entity
class SubReddit {
    @Id
    var id: Long = 0
    var name: String
    @Backlink
    lateinit var posts: ToMany<RedditPost>

    // only used in ObjectBox db internally
    constructor() {
        name = ""
    }

    constructor(post: ArrayList<RedditPost>, subName: String) {
        name = subName
        posts.addAll(post)
    }

    companion object {
        fun box(): Box<SubReddit> {
            return Feeds.instance!!.boxStore.boxFor(SubReddit::class.java)
        }
    }

}

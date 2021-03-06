package com.example.schwa.rssreddit.feed

import android.content.Context
import com.example.schwa.rssreddit.DBHelper
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
    var maxPostNum: Int = 10
    var reqUpVotes: Int = 1500
    var notiEnabled: Boolean = true
    @Backlink
    lateinit var posts: ToMany<RedditPost>

    // only used in ObjectBox db internally
    constructor() {
        name = ""
    }

    constructor(post: List<RedditPost>, subName: String) {
        name = subName
        posts.addAll(post)
    }

    constructor(name: String, maxPostNum: Int, reqUpVotes: Int, notiEnabled: Boolean) {
        this.name = name
        this.maxPostNum = maxPostNum
        this.reqUpVotes = reqUpVotes
        this.notiEnabled = notiEnabled
    }

    companion object {
        fun box(context: Context): Box<SubReddit> {
            return DBHelper.getBoxStore(context).boxFor(SubReddit::class.java)
        }

        fun findSubByName(subRedditName: String, applicationContext: Context) = SubReddit.box(applicationContext)
                .query().equal(SubReddit_.name, subRedditName).build().findFirst()
    }

    fun delete(context: Context) {
        // ObjectBox does not support cascading delete yet, need to do it manually
        RedditPost.box(context).remove(posts)
        SubReddit.box(context).remove(this)
    }

    fun save(context: Context) {
        posts.setRemoveFromTargetBox(true)
        SubReddit.box(context).put(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SubReddit

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

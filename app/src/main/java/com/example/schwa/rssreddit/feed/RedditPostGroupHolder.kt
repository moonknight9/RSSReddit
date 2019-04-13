package com.example.schwa.rssreddit.feed

import android.os.Parcel
import android.os.Parcelable

class RedditPostGroupHolder : Parcelable {
    var title: String? = null
    var text: String? = null
    var ups: Long = 0
    var numComments: Long = 0
    var url: String? = null
    var permalink: String? = null
    var thumbnail: String? = null

    constructor(redditPost: RedditPost) {
        title = redditPost.title
        text = redditPost.text
        ups = redditPost.ups
        numComments = redditPost.numComments
        url = redditPost.url
        permalink = redditPost.permalink
        thumbnail = redditPost.thumbnail
    }

    constructor(parcel: Parcel) {
        title = parcel.readString()
        text = parcel.readString()
        ups = parcel.readLong()
        numComments = parcel.readLong()
        url = parcel.readString()
        permalink = parcel.readString()
        thumbnail = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(text)
        parcel.writeLong(ups)
        parcel.writeLong(numComments)
        parcel.writeString(url)
        parcel.writeString(permalink)
        parcel.writeString(thumbnail)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<RedditPostGroupHolder> {
        override fun createFromParcel(parcel: Parcel): RedditPostGroupHolder {
            return RedditPostGroupHolder(parcel)
        }

        override fun newArray(size: Int): Array<RedditPostGroupHolder?> {
            return arrayOfNulls(size)
        }
    }
}